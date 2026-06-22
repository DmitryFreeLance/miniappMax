package com.maxminiapp.service;

import com.maxminiapp.config.AppProperties;
import com.maxminiapp.dto.AdminAcceptOrderRequest;
import com.maxminiapp.dto.CreateOrderItemRequest;
import com.maxminiapp.dto.CreateOrderRequest;
import com.maxminiapp.dto.CreateOrderResponse;
import com.maxminiapp.dto.OrderItemResponse;
import com.maxminiapp.dto.OrderResponse;
import com.maxminiapp.enums.DeliveryMethod;
import com.maxminiapp.enums.OrderStatus;
import com.maxminiapp.enums.PaymentMethod;
import com.maxminiapp.enums.QuantityUnit;
import com.maxminiapp.exception.BadRequestException;
import com.maxminiapp.model.AppUser;
import com.maxminiapp.model.Order;
import com.maxminiapp.model.OrderItem;
import com.maxminiapp.model.Product;
import com.maxminiapp.repository.OrderItemRepository;
import com.maxminiapp.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final Duration DUPLICATE_ORDER_WINDOW = Duration.ofSeconds(30);

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;
    private final AppProperties appProperties;
    private final AppSettingsService appSettingsService;
    private final OrderNotificationService orderNotificationService;
    private final OrderItemRepository orderItemRepository;
    private final ConcurrentMap<Long, Object> orderCreationLocks = new ConcurrentHashMap<>();

    public OrderService(
            OrderRepository orderRepository,
            UserService userService,
            ProductService productService,
            AppProperties appProperties,
            AppSettingsService appSettingsService,
            OrderNotificationService orderNotificationService,
            OrderItemRepository orderItemRepository
    ) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.productService = productService;
        this.appProperties = appProperties;
        this.appSettingsService = appSettingsService;
        this.orderNotificationService = orderNotificationService;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request, Long headerUserId) {
        Long maxUserId = headerUserId != null ? headerUserId : request.getUserId();
        if (maxUserId == null) {
            throw new BadRequestException("Нужен ID пользователя MAX (X-User-Id или userId в запросе)");
        }

        String fullName = request.getFullName().trim();
        String phone = request.getPhone().trim();
        String address = request.getAddress() == null ? "" : request.getAddress().trim();

        if (fullName.isBlank() || phone.isBlank()) {
            throw new BadRequestException("Укажите ФИО и телефон");
        }

        if (request.getDeliveryMethod() == DeliveryMethod.CITY_DELIVERY && address.isBlank()) {
            throw new BadRequestException("Укажите адрес доставки");
        }

        if (request.getDeliveryMethod() == DeliveryMethod.PICKUP && address.isBlank()) {
            address = "Самовывоз";
        }

        if (request.getDeliveryMethod() == DeliveryMethod.OTHER && address.isBlank()) {
            address = "Другая доставка (обсуждается)";
        }

        PaymentMethod paymentMethod = request.getPaymentMethod();
        String paymentDetails = appSettingsService.getPaymentDetails().trim();
        if (paymentMethod == PaymentMethod.CARD_NOW && paymentDetails.isBlank()) {
            throw new BadRequestException("Реквизиты для оплаты пока не настроены администратором");
        }

        Object creationLock = orderCreationLocks.computeIfAbsent(maxUserId, ignored -> new Object());
        synchronized (creationLock) {
            return createOrderLocked(request, maxUserId, fullName, phone, address, paymentMethod, paymentDetails);
        }
    }

    private CreateOrderResponse createOrderLocked(
            CreateOrderRequest request,
            Long maxUserId,
            String fullName,
            String phone,
            String address,
            PaymentMethod paymentMethod,
            String paymentDetails
    ) {
        String requestId = normalizeRequestId(request.getRequestId());

        if (requestId != null) {
            Order existingByRequestId = orderRepository
                    .findFirstByUserMaxUserIdAndClientRequestIdOrderByCreatedAtDesc(maxUserId, requestId)
                    .orElse(null);
            if (existingByRequestId != null) {
                log.info("Suppress duplicate order by requestId: userId={}, requestId={}, orderId={}", maxUserId, requestId, existingByRequestId.getId());
                return buildCreateOrderResponse(existingByRequestId, true);
            }
        }

        AppUser user = userService.getOrCreateByMaxUserId(maxUserId);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);

        List<MergedOrderItem> mergedItems = mergeRequestedItems(request);
        Order recentDuplicate = findRecentDuplicateOrder(maxUserId, fullName, phone, address, request.getDeliveryMethod(), paymentMethod, mergedItems);
        if (recentDuplicate != null) {
            if (requestId != null && recentDuplicate.getClientRequestId() == null) {
                recentDuplicate.setClientRequestId(requestId);
                orderRepository.save(recentDuplicate);
            }
            log.info("Suppress duplicate order by fingerprint: userId={}, requestId={}, orderId={}", maxUserId, requestId, recentDuplicate.getId());
            return buildCreateOrderResponse(recentDuplicate, true);
        }

        List<ResolvedOrderItem> resolvedItems = resolveItemsForOrder(mergedItems);
        BigDecimal itemsTotal = resolvedItems.stream()
                .map(ResolvedOrderItem::lineTotal)
                .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal deliveryFee = resolveDeliveryFee(request.getDeliveryMethod());
        BigDecimal total = itemsTotal.add(deliveryFee).setScale(2, RoundingMode.HALF_UP);

        ResolvedOrderItem firstItem = resolvedItems.getFirst();

        Order order = new Order();
        order.setUser(user);
        order.setProduct(firstItem.product());
        order.setQuantity(firstItem.quantity());
        order.setQuantityUnit(firstItem.quantityUnit());
        order.setUnitPrice(firstItem.unitPrice());
        order.setItemsTotal(itemsTotal);
        order.setDeliveryFee(deliveryFee);
        order.setTotalPrice(total);
        order.setFullName(fullName);
        order.setPhone(phone);
        order.setAddress(address);
        order.setDeliveryMethod(request.getDeliveryMethod());
        order.setPaymentMethod(paymentMethod);
        order.setClientRequestId(requestId);

        if (paymentMethod == PaymentMethod.CARD_NOW) {
            order.setStatus(OrderStatus.PAID);
            order.setPaymentDetailsSnapshot(paymentDetails);
            order.setPaidAt(LocalDateTime.now());
        } else {
            order.setStatus(OrderStatus.CREATED);
            order.setPaymentDetailsSnapshot(null);
        }

        for (ResolvedOrderItem item : resolvedItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(item.product());
            orderItem.setProductName(item.product().getName());
            orderItem.setQuantity(item.quantity());
            orderItem.setQuantityUnit(item.quantityUnit());
            orderItem.setUnitPrice(item.unitPrice());
            orderItem.setLineTotal(item.lineTotal());
            order.getItems().add(orderItem);
        }

        order = orderRepository.save(order);
        scheduleAdminNotificationAfterCommit(order.getId());

        log.info("Order created: userId={}, orderId={}, requestId={}, itemsCount={}", maxUserId, order.getId(), requestId, resolvedItems.size());
        return buildCreateOrderResponse(order, false);
    }

    @Transactional
    public void acceptOrder(Long orderId, AdminAcceptOrderRequest request) {
        String eta = request.getEta() == null ? "" : request.getEta().trim();
        if (eta.isBlank()) {
            throw new BadRequestException("Укажите ориентировочное время доставки");
        }
        if (request.getEtaAt() == null) {
            throw new BadRequestException("Укажите срок заказа");
        }
        if (!request.getEtaAt().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Срок заказа должен быть позже текущего времени");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BadRequestException("Заказ не найден"));

        order.setAccepted(true);
        order.setAcceptedAt(LocalDateTime.now());
        order.setDeliveryEta(eta);
        order.setDeliveryEtaAt(request.getEtaAt());
        orderRepository.save(order);

        Long userMaxId = order.getUser() == null ? null : order.getUser().getMaxUserId();
        scheduleCustomerAcceptanceNotificationAfterCommit(userMaxId, eta);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForUser(Long maxUserId) {
        if (maxUserId == null) {
            throw new BadRequestException("Нужен ID пользователя MAX (X-User-Id)");
        }

        return orderRepository.findByUserMaxUserIdOrderByCreatedAtDesc(maxUserId).stream()
                .map(this::toResponse)
                .toList();
    }

    private OrderResponse toResponse(Order order) {
        Long productId = order.getProduct() == null ? null : order.getProduct().getId();
        String productName = order.getProduct() == null ? "Сборный заказ" : order.getProduct().getName();
        return new OrderResponse(
                order.getId(),
                order.getUser().getMaxUserId(),
                productId,
                productName,
                order.getQuantity(),
                order.getQuantityUnit(),
                order.getUnitPrice(),
                order.getItemsTotal(),
                order.getDeliveryFee(),
                order.getTotalPrice(),
                order.getDeliveryMethod(),
                order.getPaymentMethod(),
                order.getFullName(),
                order.getPhone(),
                order.getAddress(),
                order.getStatus(),
                order.getPaymentDetailsSnapshot(),
                order.isAccepted(),
                order.getDeliveryEta(),
                order.getDeliveryEtaAt(),
                order.getAcceptedAt(),
                isCompleted(order),
                resolveOrderItems(order),
                order.getCreatedAt(),
                order.getPaidAt()
        );
    }

    private boolean isCompleted(Order order) {
        return order.isAccepted()
                && order.getDeliveryEtaAt() != null
                && !LocalDateTime.now().isBefore(order.getDeliveryEtaAt());
    }

    private List<OrderItemResponse> resolveOrderItems(Order order) {
        List<OrderItem> rawItems = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId());
        if (!rawItems.isEmpty()) {
            return rawItems.stream()
                    .map(item -> new OrderItemResponse(
                            item.getId(),
                            item.getProduct() == null ? null : item.getProduct().getId(),
                            item.getProductName(),
                            item.getQuantity(),
                            item.getQuantityUnit(),
                            item.getUnitPrice(),
                            item.getLineTotal()
                    ))
                    .toList();
        }

        if (order.getProduct() == null) {
            return List.of();
        }

        return List.of(new OrderItemResponse(
                null,
                order.getProduct().getId(),
                order.getProduct().getName(),
                order.getQuantity(),
                order.getQuantityUnit(),
                order.getUnitPrice(),
                order.getQuantity().multiply(order.getUnitPrice()).setScale(2, RoundingMode.HALF_UP)
        ));
    }

    private BigDecimal resolveDeliveryFee(DeliveryMethod deliveryMethod) {
        if (deliveryMethod == DeliveryMethod.CITY_DELIVERY) {
            BigDecimal fee = appProperties.getCityDeliveryFee();
            if (fee == null) {
                return new BigDecimal("1000.00");
            }
            return fee.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private void scheduleAdminNotificationAfterCommit(Long orderId) {
        if (orderId == null) {
            return;
        }

        Runnable notifyTask = () -> {
            try {
                orderNotificationService.notifyAdminsAboutNewOrder(orderId);
            } catch (Exception ex) {
                log.warn("Failed to notify admins about order {}: {}", orderId, ex.getMessage());
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    notifyTask.run();
                }
            });
            return;
        }

        notifyTask.run();
    }

    private void scheduleCustomerAcceptanceNotificationAfterCommit(Long userMaxId, String eta) {
        if (userMaxId == null) {
            return;
        }

        Runnable notifyTask = () -> {
            try {
                orderNotificationService.notifyCustomerOrderAccepted(userMaxId, eta);
            } catch (Exception ex) {
                log.warn("Failed to notify customer {} about accepted order: {}", userMaxId, ex.getMessage());
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    notifyTask.run();
                }
            });
            return;
        }

        notifyTask.run();
    }

    private List<MergedOrderItem> mergeRequestedItems(CreateOrderRequest request) {
        List<CreateOrderItemRequest> requestItems = request.getItems();
        if (requestItems == null || requestItems.isEmpty()) {
            if (request.getProductId() == null || request.getQuantity() == null || request.getQuantityUnit() == null) {
                throw new BadRequestException("Корзина пуста");
            }

            CreateOrderItemRequest single = new CreateOrderItemRequest();
            single.setProductId(request.getProductId());
            single.setQuantity(request.getQuantity());
            single.setQuantityUnit(request.getQuantityUnit());
            requestItems = List.of(single);
        }

        Map<String, MergedOrderItem> merged = new LinkedHashMap<>();
        for (CreateOrderItemRequest item : requestItems) {
            if (item == null || item.getProductId() == null || item.getQuantity() == null || item.getQuantityUnit() == null) {
                throw new BadRequestException("Проверьте товары в корзине");
            }

            BigDecimal qty = item.getQuantity().setScale(3, RoundingMode.HALF_UP);
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Количество товара должно быть больше 0");
            }

            String key = item.getProductId() + ":" + item.getQuantityUnit().name();
            MergedOrderItem existing = merged.get(key);
            if (existing == null) {
                merged.put(key, new MergedOrderItem(item.getProductId(), item.getQuantityUnit(), qty));
            } else {
                existing.addQuantity(qty);
            }
        }

        List<MergedOrderItem> mergedItems = merged.values().stream()
                .sorted(Comparator.comparing(MergedOrderItem::productId)
                        .thenComparing(item -> item.unit().name()))
                .toList();

        if (mergedItems.isEmpty()) {
            throw new BadRequestException("Корзина пуста");
        }

        return mergedItems;
    }

    private List<ResolvedOrderItem> resolveItemsForOrder(List<MergedOrderItem> mergedItems) {
        List<ResolvedOrderItem> resolved = new ArrayList<>();
        for (MergedOrderItem mergedItem : mergedItems) {
            Product product = productService.getByIdOrThrow(mergedItem.productId());
            if (!product.isActive()) {
                throw new BadRequestException("Товар «" + product.getName() + "» недоступен для заказа");
            }

            BigDecimal unitPrice = productService.resolveUnitPrice(product, mergedItem.unit());
            productService.reserveStock(product, mergedItem.unit(), mergedItem.quantity());
            BigDecimal lineTotal = unitPrice.multiply(mergedItem.quantity()).setScale(2, RoundingMode.HALF_UP);

            resolved.add(new ResolvedOrderItem(
                    product,
                    mergedItem.quantity(),
                    mergedItem.unit(),
                    unitPrice,
                    lineTotal
            ));
        }

        if (resolved.isEmpty()) {
            throw new BadRequestException("Корзина пуста");
        }

        return resolved;
    }

    private Order findRecentDuplicateOrder(
            Long maxUserId,
            String fullName,
            String phone,
            String address,
            DeliveryMethod deliveryMethod,
            PaymentMethod paymentMethod,
            List<MergedOrderItem> mergedItems
    ) {
        LocalDateTime createdAfter = LocalDateTime.now().minus(DUPLICATE_ORDER_WINDOW);
        String fingerprint = buildRequestFingerprint(fullName, phone, address, deliveryMethod, paymentMethod, mergedItems);

        return orderRepository.findTop5ByUserMaxUserIdAndCreatedAtAfterOrderByCreatedAtDesc(maxUserId, createdAfter).stream()
                .filter(order -> Objects.equals(buildOrderFingerprint(order), fingerprint))
                .findFirst()
                .orElse(null);
    }

    private CreateOrderResponse buildCreateOrderResponse(Order order, boolean duplicate) {
        String message;
        if (duplicate) {
            message = "Заказ уже был создан ранее. Повторная отправка отменена.";
        } else if (order.getPaymentMethod() == PaymentMethod.CARD_NOW) {
            message = "Платеж отмечен как выполненный. С вами свяжется менеджер.";
        } else {
            message = "Заказ создан с оплатой при получении. С вами свяжется менеджер.";
        }

        return new CreateOrderResponse(
                order.getId(),
                order.getItemsTotal(),
                order.getDeliveryFee(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getPaymentMethod(),
                message
        );
    }

    private String buildRequestFingerprint(
            String fullName,
            String phone,
            String address,
            DeliveryMethod deliveryMethod,
            PaymentMethod paymentMethod,
            List<MergedOrderItem> mergedItems
    ) {
        String itemsPart = mergedItems.stream()
                .sorted(Comparator.comparing(MergedOrderItem::productId)
                        .thenComparing(item -> item.unit().name()))
                .map(item -> item.productId() + ":" + item.unit().name() + ":" + formatQuantity(item.quantity()))
                .collect(Collectors.joining(";"));

        return String.join("|",
                normalizeText(fullName),
                normalizePhone(phone),
                normalizeText(address),
                deliveryMethod == null ? "" : deliveryMethod.name(),
                paymentMethod == null ? "" : paymentMethod.name(),
                itemsPart
        );
    }

    private String buildOrderFingerprint(Order order) {
        List<OrderItemResponse> items = resolveOrderItems(order);
        String itemsPart = items.stream()
                .sorted(Comparator.comparing((OrderItemResponse item) -> item.productId() == null ? Long.MAX_VALUE : item.productId())
                        .thenComparing(item -> item.quantityUnit() == null ? "" : item.quantityUnit().name()))
                .map(item -> (item.productId() == null ? "0" : item.productId())
                        + ":" + (item.quantityUnit() == null ? "" : item.quantityUnit().name())
                        + ":" + formatQuantity(item.quantity()))
                .collect(Collectors.joining(";"));

        return String.join("|",
                normalizeText(order.getFullName()),
                normalizePhone(order.getPhone()),
                normalizeText(order.getAddress()),
                order.getDeliveryMethod() == null ? "" : order.getDeliveryMethod().name(),
                order.getPaymentMethod() == null ? "" : order.getPaymentMethod().name(),
                itemsPart
        );
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private String normalizePhone(String value) {
        return value == null ? "" : value.replaceAll("\\D+", "");
    }

    private String normalizeRequestId(String requestId) {
        if (requestId == null) {
            return null;
        }
        String normalized = requestId.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String formatQuantity(BigDecimal value) {
        return value == null
                ? "0.000"
                : value.setScale(3, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private record ResolvedOrderItem(
            Product product,
            BigDecimal quantity,
            QuantityUnit quantityUnit,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {
    }

    private static final class MergedOrderItem {
        private final Long productId;
        private final QuantityUnit unit;
        private BigDecimal quantity;

        private MergedOrderItem(Long productId, QuantityUnit unit, BigDecimal quantity) {
            this.productId = productId;
            this.unit = unit;
            this.quantity = quantity;
        }

        public Long productId() {
            return productId;
        }

        public QuantityUnit unit() {
            return unit;
        }

        public BigDecimal quantity() {
            return quantity;
        }

        public void addQuantity(BigDecimal delta) {
            this.quantity = this.quantity.add(delta).setScale(3, RoundingMode.HALF_UP);
        }
    }
}
