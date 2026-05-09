package com.maxminiapp.controller;

import com.maxminiapp.dto.InfoPostResponse;
import com.maxminiapp.dto.ProductResponse;
import com.maxminiapp.service.InfoPostService;
import com.maxminiapp.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PublicController {

    private final ProductService productService;
    private final InfoPostService infoPostService;

    public PublicController(ProductService productService, InfoPostService infoPostService) {
        this.productService = productService;
        this.infoPostService = infoPostService;
    }

    @GetMapping("/catalog")
    public List<ProductResponse> getCatalog() {
        return productService.getCatalog();
    }

    @GetMapping("/catalog/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        return productService.getOne(id);
    }

    @GetMapping("/fix-price")
    public List<ProductResponse> getFixPrice() {
        return productService.getFixPriceCatalog();
    }

    @GetMapping("/info")
    public List<InfoPostResponse> getInfoPosts() {
        return infoPostService.getAll();
    }
}
