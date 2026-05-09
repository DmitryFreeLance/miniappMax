package com.maxminiapp.service;

import com.maxminiapp.dto.AdminCreateProductRequest;
import com.maxminiapp.dto.ProductResponse;
import com.maxminiapp.enums.QuantityUnit;
import com.maxminiapp.enums.UnitMode;
import com.maxminiapp.exception.BadRequestException;
import com.maxminiapp.exception.NotFoundException;
import com.maxminiapp.model.Product;
import com.maxminiapp.repository.OrderRepository;
import com.maxminiapp.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ProductService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public List<ProductResponse> getCatalog() {
        return productRepository.findActiveCatalogOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ProductResponse> getFixPriceCatalog() {
        return productRepository.findActiveFixPriceOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse getOne(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Товар не найден"));
        return toResponse(product);
    }

    public List<ProductResponse> getAllForAdmin() {
        return productRepository.findByActiveTrueOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public Product getByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Товар не найден"));
    }

    @Transactional
    public ProductResponse create(AdminCreateProductRequest request) {
        Product product = new Product();
        applyRequest(product, request);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, AdminCreateProductRequest request) {
        Product product = getByIdOrThrow(id);
        applyRequest(product, request);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public String deleteForAdmin(Long id) {
        Product product = getByIdOrThrow(id);

        if (orderRepository.existsByProductId(product.getId())) {
            product.setActive(false);
            productRepository.save(product);
            return "У товара уже есть заказы. Товар скрыт из каталога и Fix Price.";
        }

        productRepository.delete(product);
        return "Товар удален.";
    }

    private void applyRequest(Product product, AdminCreateProductRequest request) {
        UnitMode unitMode = request.getUnitMode() == null ? UnitMode.BOTH : request.getUnitMode();
        boolean fixPrice = Boolean.TRUE.equals(request.getFixPrice());
        BigDecimal pricePcs = request.getPricePcs();
        BigDecimal priceCubicMeters = request.getPriceCubicMeters();

        if ((unitMode == UnitMode.PCS_ONLY || unitMode == UnitMode.BOTH)
                && request.getStockPcs() == null) {
            throw new BadRequestException("Для выбранного режима нужно указать остаток в штуках");
        }

        if ((unitMode == UnitMode.CUBIC_ONLY || unitMode == UnitMode.BOTH)
                && request.getStockCubicMeters() == null) {
            throw new BadRequestException("Для выбранного режима нужно указать остаток в кубометрах");
        }

        if ((unitMode == UnitMode.PCS_ONLY || unitMode == UnitMode.BOTH) && pricePcs == null) {
            throw new BadRequestException("Для выбранного режима нужно указать цену за штуку");
        }

        if ((unitMode == UnitMode.CUBIC_ONLY || unitMode == UnitMode.BOTH) && priceCubicMeters == null) {
            throw new BadRequestException("Для выбранного режима нужно указать цену за кубометр");
        }

        BigDecimal primaryPrice = selectPrimaryPrice(unitMode, pricePcs, priceCubicMeters);

        if (fixPrice) {
            if (request.getOldPrice() == null) {
                throw new BadRequestException("Для раздела Fix Price нужно указать старую цену");
            }

            if (unitMode == UnitMode.BOTH) {
                if (request.getOldPrice().compareTo(pricePcs) <= 0
                        || request.getOldPrice().compareTo(priceCubicMeters) <= 0) {
                    throw new BadRequestException("Старая цена должна быть больше цен за шт и куб.м");
                }
            } else if (request.getOldPrice().compareTo(primaryPrice) <= 0) {
                throw new BadRequestException("Старая цена должна быть больше текущей цены");
            }
        }

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription().trim());
        product.setImageUrl(request.getImageUrl().trim());
        product.setPrice(primaryPrice);
        product.setPricePcs(unitMode == UnitMode.CUBIC_ONLY ? null : pricePcs);
        product.setPriceCubicMeters(unitMode == UnitMode.PCS_ONLY ? null : priceCubicMeters);
        product.setOldPrice(fixPrice ? request.getOldPrice() : null);
        product.setStockPcs(unitMode == UnitMode.CUBIC_ONLY ? null : request.getStockPcs());
        product.setStockCubicMeters(unitMode == UnitMode.PCS_ONLY ? null : request.getStockCubicMeters());
        product.setUnitMode(unitMode);
        product.setFixPrice(fixPrice);
        product.setActive(request.getActive() == null || request.getActive());
    }

    public BigDecimal resolveUnitPrice(Product product, QuantityUnit unit) {
        if (unit == QuantityUnit.PCS) {
            if (product.getUnitMode() == UnitMode.CUBIC_ONLY) {
                throw new BadRequestException("Товар нельзя заказать в штуках");
            }
            BigDecimal price = product.getPricePcs() != null ? product.getPricePcs() : product.getPrice();
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Для товара не настроена цена за штуку");
            }
            return price;
        }

        if (product.getUnitMode() == UnitMode.PCS_ONLY) {
            throw new BadRequestException("Товар нельзя заказать в кубометрах");
        }
        BigDecimal price = product.getPriceCubicMeters() != null ? product.getPriceCubicMeters() : product.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Для товара не настроена цена за кубометр");
        }
        return price;
    }

    @Transactional
    public void reserveStock(Product product, QuantityUnit unit, BigDecimal quantity) {
        BigDecimal current;
        if (unit == QuantityUnit.PCS) {
            if (product.getUnitMode() == UnitMode.CUBIC_ONLY) {
                throw new BadRequestException("Товар нельзя заказать в штуках");
            }
            current = product.getStockPcs();
            if (current == null || current.compareTo(quantity) < 0) {
                throw new BadRequestException("Недостаточно товара на складе (шт)");
            }
            product.setStockPcs(current.subtract(quantity));
        } else {
            if (product.getUnitMode() == UnitMode.PCS_ONLY) {
                throw new BadRequestException("Товар нельзя заказать в кубометрах");
            }
            current = product.getStockCubicMeters();
            if (current == null || current.compareTo(quantity) < 0) {
                throw new BadRequestException("Недостаточно товара на складе (куб.м)");
            }
            product.setStockCubicMeters(current.subtract(quantity));
        }
        productRepository.save(product);
    }

    @Transactional
    public void restoreStock(Product product, QuantityUnit unit, BigDecimal quantity) {
        if (unit == QuantityUnit.PCS) {
            BigDecimal current = product.getStockPcs() == null ? BigDecimal.ZERO : product.getStockPcs();
            product.setStockPcs(current.add(quantity));
        } else {
            BigDecimal current = product.getStockCubicMeters() == null ? BigDecimal.ZERO : product.getStockCubicMeters();
            product.setStockCubicMeters(current.add(quantity));
        }
        productRepository.save(product);
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getImageUrl(),
                product.getPrice(),
                product.getPricePcs(),
                product.getPriceCubicMeters(),
                product.getOldPrice(),
                product.getStockPcs(),
                product.getStockCubicMeters(),
                product.getUnitMode(),
                Boolean.TRUE.equals(product.getFixPrice()),
                product.isActive()
        );
    }

    private BigDecimal selectPrimaryPrice(UnitMode unitMode, BigDecimal pricePcs, BigDecimal priceCubicMeters) {
        if (unitMode == UnitMode.PCS_ONLY) {
            return pricePcs;
        }
        if (unitMode == UnitMode.CUBIC_ONLY) {
            return priceCubicMeters;
        }
        return pricePcs.min(priceCubicMeters);
    }
}
