package com.maxminiapp.controller;

import com.maxminiapp.config.AppProperties;
import com.maxminiapp.dto.*;
import com.maxminiapp.exception.BadRequestException;
import com.maxminiapp.model.AppUser;
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
    private final AppProperties appProperties;

    public AdminController(
            UserService userService,
            ProductService productService,
            OrderService orderService,
            InfoPostService infoPostService,
            AppProperties appProperties
    ) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
        this.infoPostService = infoPostService;
        this.appProperties = appProperties;
    }

    @GetMapping("/users")
    public List<UserResponse> users(@RequestHeader("X-User-Id") Long adminUserId) {
        requireAdmin(adminUserId);
        return userService.findAll().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @GetMapping("/orders")
    public List<OrderResponse> orders(@RequestHeader("X-User-Id") Long adminUserId) {
        requireAdmin(adminUserId);
        return orderService.getAllOrders();
    }

    @GetMapping("/products")
    public List<ProductResponse> products(@RequestHeader("X-User-Id") Long adminUserId) {
        requireAdmin(adminUserId);
        return productService.getAllForAdmin();
    }

    @PostMapping("/products")
    public ProductResponse createProduct(
            @RequestHeader("X-User-Id") Long adminUserId,
            @RequestBody @Valid AdminCreateProductRequest request
    ) {
        requireAdmin(adminUserId);
        return productService.create(request);
    }

    @PutMapping("/products/{id}")
    public ProductResponse updateProduct(
            @RequestHeader("X-User-Id") Long adminUserId,
            @PathVariable Long id,
            @RequestBody @Valid AdminCreateProductRequest request
    ) {
        requireAdmin(adminUserId);
        return productService.update(id, request);
    }

    @GetMapping("/admins")
    public List<UserResponse> admins(@RequestHeader("X-User-Id") Long adminUserId) {
        requireAdmin(adminUserId);
        return userService.findAdmins().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @PostMapping("/admins")
    public UserResponse addAdmin(
            @RequestHeader("X-User-Id") Long adminUserId,
            @RequestBody @Valid AdminAddAdminRequest request
    ) {
        requireAdmin(adminUserId);
        AppUser user = userService.grantAdmin(request.getMaxUserId());
        return toUserResponse(user);
    }

    @PostMapping("/info-posts")
    public InfoPostResponse createInfoPost(
            @RequestHeader("X-User-Id") Long adminUserId,
            @RequestBody @Valid AdminCreateInfoPostRequest request
    ) {
        requireAdmin(adminUserId);
        return infoPostService.create(request, adminUserId);
    }

    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse uploadImage(
            @RequestHeader("X-User-Id") Long adminUserId,
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
