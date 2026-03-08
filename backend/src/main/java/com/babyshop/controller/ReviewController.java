package com.babyshop.controller;

import com.babyshop.common.Result;
import com.babyshop.dto.ReviewDTO;
import com.babyshop.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/product/{productId}")
    public Result<?> listByProduct(@PathVariable Long productId,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(reviewService.listByProduct(productId, page, size));
    }

    @PostMapping
    public Result<?> addReview(Authentication authentication, @Valid @RequestBody ReviewDTO dto) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success("评价成功", reviewService.addReview(userId, dto));
    }
}
