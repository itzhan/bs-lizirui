package com.babyshop.controller;

import com.babyshop.common.Result;
import com.babyshop.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public Result<?> listFavorites(Authentication authentication,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(favoriteService.listByUser(userId, page, size));
    }

    @PostMapping("/{productId}")
    public Result<?> addFavorite(Authentication authentication, @PathVariable Long productId) {
        Long userId = (Long) authentication.getPrincipal();
        favoriteService.addFavorite(userId, productId);
        return Result.success("收藏成功");
    }

    @DeleteMapping("/{productId}")
    public Result<?> removeFavorite(Authentication authentication, @PathVariable Long productId) {
        Long userId = (Long) authentication.getPrincipal();
        favoriteService.removeFavorite(userId, productId);
        return Result.success("取消收藏");
    }

    @GetMapping("/check/{productId}")
    public Result<?> isFavorited(Authentication authentication, @PathVariable Long productId) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(favoriteService.isFavorited(userId, productId));
    }
}
