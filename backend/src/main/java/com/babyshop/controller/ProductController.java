package com.babyshop.controller;

import com.babyshop.common.Result;
import com.babyshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Result<?> listProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        return Result.success(productService.listProducts(page, size, categoryId, keyword, brand, null, sortBy, sortOrder));
    }

    @GetMapping("/{id}")
    public Result<?> getProduct(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @GetMapping("/recommend")
    public Result<?> recommendProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer size) {
        return Result.success(productService.listRecommendProducts(page, size));
    }

    @GetMapping("/hot")
    public Result<?> hotProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer size) {
        return Result.success(productService.listHotProducts(page, size));
    }
}
