package com.babyshop.controller;

import com.babyshop.common.Result;
import com.babyshop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<?> listCategories() {
        return Result.success(categoryService.listAll());
    }

    @GetMapping("/tree")
    public Result<?> listTree() {
        return Result.success(categoryService.listTree());
    }

    @GetMapping("/{id}")
    public Result<?> getCategory(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }
}
