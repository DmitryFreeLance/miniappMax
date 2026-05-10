package com.maxminiapp.config;

import com.maxminiapp.enums.UnitMode;
import com.maxminiapp.model.AppUser;
import com.maxminiapp.model.InfoPost;
import com.maxminiapp.model.Product;
import com.maxminiapp.repository.AppUserRepository;
import com.maxminiapp.repository.InfoPostRepository;
import com.maxminiapp.repository.ProductRepository;
import com.maxminiapp.service.AppSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class BootstrapDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapDataInitializer.class);

    private final AppProperties appProperties;
    private final AppUserRepository appUserRepository;
    private final ProductRepository productRepository;
    private final InfoPostRepository infoPostRepository;
    private final AppSettingsService appSettingsService;
    private final DataSource dataSource;

    public BootstrapDataInitializer(
            AppProperties appProperties,
            AppUserRepository appUserRepository,
            ProductRepository productRepository,
            InfoPostRepository infoPostRepository,
            AppSettingsService appSettingsService,
            DataSource dataSource
    ) {
        this.appProperties = appProperties;
        this.appUserRepository = appUserRepository;
        this.productRepository = productRepository;
        this.infoPostRepository = infoPostRepository;
        this.appSettingsService = appSettingsService;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        migrateSchemaForLegacySqlite();
        Files.createDirectories(Path.of(appProperties.getUploadsDir()));
        normalizeExistingProducts();
        appSettingsService.seedPaymentDetailsIfEmpty(appProperties.getDefaultPaymentDetails());

        if (appProperties.getBootstrapAdminId() != null) {
            appUserRepository.findByMaxUserId(appProperties.getBootstrapAdminId())
                    .ifPresentOrElse(
                            user -> {
                                if (!user.isAdmin()) {
                                    user.setAdmin(true);
                                    appUserRepository.save(user);
                                }
                            },
                            () -> {
                                AppUser user = new AppUser();
                                user.setMaxUserId(appProperties.getBootstrapAdminId());
                                user.setAdmin(true);
                                appUserRepository.save(user);
                            }
                    );
            log.info("Bootstrap admin user id: {}", appProperties.getBootstrapAdminId());
        }

        if (productRepository.count() == 0) {
            Product concrete = new Product();
            concrete.setName("Бетон М300");
            concrete.setDescription("Товарный бетон для фундамента и монолитных работ.");
            concrete.setImageUrl("https://images.unsplash.com/photo-1617098474202-0d0d7f60e786?auto=format&fit=crop&w=1200&q=80");
            concrete.setPrice(new BigDecimal("6200.00"));
            concrete.setPriceCubicMeters(new BigDecimal("6200.00"));
            concrete.setStockCubicMeters(new BigDecimal("100.000"));
            concrete.setStockPcs(new BigDecimal("0.000"));
            concrete.setUnitMode(UnitMode.CUBIC_ONLY);
            concrete.setFixPrice(false);
            concrete.setActive(true);

            Product brick = new Product();
            brick.setName("Кирпич облицовочный");
            brick.setDescription("Качественный облицовочный кирпич, подходит для фасадных работ.");
            brick.setImageUrl("https://images.unsplash.com/photo-1599707367072-cd6ada2bc375?auto=format&fit=crop&w=1200&q=80");
            brick.setPrice(new BigDecimal("38.50"));
            brick.setPricePcs(new BigDecimal("38.50"));
            brick.setStockPcs(new BigDecimal("30000.000"));
            brick.setStockCubicMeters(new BigDecimal("0.000"));
            brick.setUnitMode(UnitMode.PCS_ONLY);
            brick.setFixPrice(false);
            brick.setActive(true);

            Product insulation = new Product();
            insulation.setName("Утеплитель XPS 50 мм");
            insulation.setDescription("Экструдированный пенополистирол для теплоизоляции фундамента и стен.");
            insulation.setImageUrl("https://images.unsplash.com/photo-1581092160562-40aa08e78837?auto=format&fit=crop&w=1200&q=80");
            insulation.setPrice(new BigDecimal("349.00"));
            insulation.setPricePcs(new BigDecimal("349.00"));
            insulation.setOldPrice(new BigDecimal("430.00"));
            insulation.setStockPcs(new BigDecimal("2500.000"));
            insulation.setStockCubicMeters(new BigDecimal("0.000"));
            insulation.setUnitMode(UnitMode.PCS_ONLY);
            insulation.setFixPrice(true);
            insulation.setActive(true);

            productRepository.save(concrete);
            productRepository.save(brick);
            productRepository.save(insulation);
            log.info("Seeded demo products");
        }

        if (infoPostRepository.count() == 0) {
            InfoPost post = new InfoPost();
            post.setTitle("Режим работы");
            post.setContent("Прием заказов ежедневно с 09:00 до 20:00. Доставка по городу и области.");
            post.setCreatedByAdminId(appProperties.getBootstrapAdminId());
            infoPostRepository.save(post);
            log.info("Seeded info post");
        }
    }

    private void normalizeExistingProducts() {
        List<Product> toUpdate = new ArrayList<>();
        for (Product product : productRepository.findAll()) {
            boolean changed = false;

            if (product.getFixPrice() == null) {
                product.setFixPrice(false);
                changed = true;
            }

            if (product.getUnitMode() == UnitMode.PCS_ONLY) {
                if (product.getPricePcs() == null && product.getPrice() != null) {
                    product.setPricePcs(product.getPrice());
                    changed = true;
                }
            } else if (product.getUnitMode() == UnitMode.CUBIC_ONLY) {
                if (product.getPriceCubicMeters() == null && product.getPrice() != null) {
                    product.setPriceCubicMeters(product.getPrice());
                    changed = true;
                }
            } else {
                if (product.getPricePcs() == null && product.getPrice() != null) {
                    product.setPricePcs(product.getPrice());
                    changed = true;
                }
                if (product.getPriceCubicMeters() == null && product.getPrice() != null) {
                    product.setPriceCubicMeters(product.getPrice());
                    changed = true;
                }
            }

            if (product.getPrice() == null) {
                if (product.getPricePcs() != null && product.getPriceCubicMeters() != null) {
                    product.setPrice(product.getPricePcs().min(product.getPriceCubicMeters()));
                    changed = true;
                } else if (product.getPricePcs() != null) {
                    product.setPrice(product.getPricePcs());
                    changed = true;
                } else if (product.getPriceCubicMeters() != null) {
                    product.setPrice(product.getPriceCubicMeters());
                    changed = true;
                }
            }

            if (changed) {
                toUpdate.add(product);
            }
        }
        if (!toUpdate.isEmpty()) {
            productRepository.saveAll(toUpdate);
        }
    }

    private void migrateSchemaForLegacySqlite() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            Set<String> orderColumns = readColumns(connection, "orders");
            if (orderColumns.isEmpty()) {
                return;
            }
            if (!orderColumns.contains("is_accepted")) {
                statement.execute("ALTER TABLE orders ADD COLUMN is_accepted INTEGER NOT NULL DEFAULT 0");
                log.info("Added missing column orders.is_accepted");
            }
            if (!orderColumns.contains("accepted_at")) {
                statement.execute("ALTER TABLE orders ADD COLUMN accepted_at TEXT");
                log.info("Added missing column orders.accepted_at");
            }
            if (!orderColumns.contains("delivery_eta")) {
                statement.execute("ALTER TABLE orders ADD COLUMN delivery_eta TEXT");
                log.info("Added missing column orders.delivery_eta");
            }

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS order_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        order_id INTEGER NOT NULL,
                        product_id INTEGER NOT NULL,
                        product_name TEXT NOT NULL,
                        quantity NUMERIC NOT NULL,
                        quantity_unit TEXT NOT NULL,
                        unit_price NUMERIC NOT NULL,
                        line_total NUMERIC NOT NULL,
                        FOREIGN KEY(order_id) REFERENCES orders(id),
                        FOREIGN KEY(product_id) REFERENCES products(id)
                    )
                    """);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items(product_id)");
        } catch (Exception ex) {
            log.warn("Legacy schema migration skipped: {}", ex.getMessage());
        }
    }

    private Set<String> readColumns(Connection connection, String table) throws Exception {
        Set<String> columns = new HashSet<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                String name = rs.getString("name");
                if (name != null) {
                    columns.add(name.toLowerCase(Locale.ROOT));
                }
            }
        }
        return columns;
    }
}
