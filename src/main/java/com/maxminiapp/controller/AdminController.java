package com.maxminiapp.controller;

import com.maxminiapp.config.AppProperties;
import com.maxminiapp.dto.*;
import com.maxminiapp.exception.BadRequestException;
import com.maxminiapp.model.AppUser;
import com.maxminiapp.service.AppSettingsService;
import com.maxminiapp.service.InfoPostService;
import com.maxminiapp.service.OrderService;
import com.maxminiapp.service.ProductService;
import com.maxminiapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final InfoPostService infoPostService;
    private final AppSettingsService appSettingsService;
    private final AppProperties appProperties;

    public AdminController(
            UserService userService,
            ProductService productService,
            OrderService orderService,
            InfoPostService infoPostService,
            AppSettingsService appSettingsService,
            AppProperties appProperties
    ) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
        this.infoPostService = infoPostService;
        this.appSettingsService = appSettingsService;
        this.appProperties = appProperties;
    }

    @GetMapping("/users")
    public List<UserResponse> users(@RequestHeader(name = "X-User-Id", required = false) Long adminUserId) {
        requireAdmin(adminUserId);
        return userService.findAll().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @GetMapping("/orders")
    public List<OrderResponse> orders(@RequestHeader(name = "X-User-Id", required = false) Long adminUserId) {
        requireAdmin(adminUserId);
        return orderService.getAllOrders();
    }

    @PostMapping("/orders/{id}/accept")
    public ActionResponse acceptOrder(
            @RequestHeader(name = "X-User-Id", required = false) Long adminUserId,
            @PathVariable Long id,
            @RequestBody @Valid AdminAcceptOrderRequest request
    ) {
        requireAdmin(adminUserId);
        orderService.acceptOrder(id, request.getEta());
        return new ActionResponse("Заказ принят, клиент уведомлен.");
    }

    @GetMapping("/products")
    public List<ProductResponse> products(@RequestHeader(name = "X-User-Id", required = false) Long adminUserId) {
        requireAdmin(adminUserId);
        return productService.getAllForAdmin();
    }

    @DeleteMapping("/products/{id}")
    public ActionResponse deleteProduct(
            @RequestHeader(name = "X-User-Id", required = false) Long adminUserId,
            @PathVariable Long id
    ) {
        requireAdmin(adminUserId);
        String message = productService.deleteForAdmin(id);
        return new ActionResponse(message);
    }

    @PostMapping("/products")
    public ProductResponse createProduct(
            @RequestHeader(name = "X-User-Id", required = false) Long adminUserId,
            @RequestBody @Valid AdminCreateProductRequest request
    ) {
        requireAdmin(adminUserId);
        return productService.create(request);
    }

    @PutMapping("/products/{id}")
    public ProductResponse updateProduct(
            @RequestHeader(name = "X-User-Id", required = false) Long adminUserId,
            @PathVariable Long id,
            @RequestBody @Valid AdminCreateProductRequest request
    ) {
        requireAdmin(adminUserId);
        return productService.update(id, request);
    }

    @GetMapping("/admins")
    public List<UserResponse> admins(@RequestHeader(name = "X-User-Id", required = false) Long adminUserId) {
        requireAdmin(adminUserId);
        return userService.findAdmins().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @PostMapping("/admins")
    public UserResponse addAdmin(
            @RequestHeader(name = "X-User-Id", required = false) Long adminUserId,
            @RequestBody @Valid AdminAddAdminRequest request
    ) {
        requireAdmin(adminUserId);
        AppUser user = userService.grantAdmin(request.getMaxUserId());
        return toUserResponse(user);
    }

    @GetMapping("/settings/payment-details")
    public PaymentDetailsResponse paymentDetails(
            @RequestHeader(name = "X-User-Id", required = false) Long adminUserId
    ) {
        requireAdmin(adminUserId);
        return new PaymentDetailsResponse(appSettingsService.getPaymentDetails(), appProperties.getCityDeliveryFee());
    }

    @PutMapping("/settings/payment-details")
    public ActionResponse updatePaymentDetails(
            @RequestHeader(name = "X-User-Id", required = false) Long adminUserId,
            @RequestBody @Valid AdminPaymentDetailsRequest request
    ) {
        requireAdmin(adminUserId);
        appSettingsService.setPaymentDetails(request.getPaymentDetails().trim());
        return new ActionResponse("Данные для оплаты обновлены.");
    }

    @PostMapping("/info-posts")
    public InfoPostResponse createInfoPost(
            @RequestHeader(name = "X-User-Id", required = false) Long adminUserId,
            @RequestBody @Valid AdminCreateInfoPostRequest request
    ) {
        requireAdmin(adminUserId);
        return infoPostService.create(request, adminUserId);
    }

    @GetMapping("/info-posts")
    public List<InfoPostResponse> infoPosts(@RequestHeader(name = "X-User-Id", required = false) Long adminUserId) {
        requireAdmin(adminUserId);
        return infoPostService.getAll();
    }

    @DeleteMapping("/info-posts/{id}")
    public ActionResponse deleteInfoPost(
            @RequestHeader(name = "X-User-Id", required = false) Long adminUserId,
            @PathVariable Long id
    ) {
        requireAdmin(adminUserId);
        infoPostService.deleteById(id);
        return new ActionResponse("Пост удален.");
    }

    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse uploadImage(
            @RequestHeader(name = "X-User-Id", required = false) Long adminUserId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        requireAdmin(adminUserId);

        if (file.isEmpty()) {
            throw new BadRequestException("Файл пустой");
        }

        String originalName = file.getOriginalFilename();
        String ext = StringUtils.getFilenameExtension(originalName);
        if (ext == null || ext.isBlank()) {
            ext = "jpg";
        }

        String filename = UUID.randomUUID() + "." + ext.toLowerCase();
        Path uploadsPath = Path.of(appProperties.getUploadsDir()).toAbsolutePath().normalize();
        Files.createDirectories(uploadsPath);
        Path target = uploadsPath.resolve(filename);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return new UploadResponse(appProperties.getPublicBaseUrl() + "/uploads/" + filename);
    }

    private void requireAdmin(Long adminUserId) {
        userService.requireAdmin(adminUserId);
    }

    private UserResponse toUserResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getMaxUserId(),
                user.isAdmin(),
                user.getFullName(),
                user.getPhone(),
                user.getAddress(),
                user.getCreatedAt()
        );
    }
}
