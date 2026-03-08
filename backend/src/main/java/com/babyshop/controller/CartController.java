package com.babyshop.controller;

import com.babyshop.common.Result;
import com.babyshop.dto.CartDTO;
import com.babyshop.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Result<?> listCart(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(cartService.listCartItems(userId));
    }

    @PostMapping
    public Result<?> addToCart(Authentication authentication, @Valid @RequestBody CartDTO dto) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(cartService.addToCart(userId, dto));
    }

    @PutMapping("/{id}/quantity")
    public Result<?> updateQuantity(Authentication authentication,
                                     @PathVariable Long id,
                                     @RequestBody Map<String, Integer> body) {
        Long userId = (Long) authentication.getPrincipal();
        cartService.updateQuantity(userId, id, body.get("quantity"));
        return Result.success();
    }

    @PutMapping("/{id}/checked")
    public Result<?> toggleChecked(Authentication authentication,
                                    @PathVariable Long id,
                                    @RequestBody Map<String, Integer> body) {
        Long userId = (Long) authentication.getPrincipal();
        cartService.toggleChecked(userId, id, body.get("checked"));
        return Result.success();
    }

    @PutMapping("/checkAll")
    public Result<?> checkAll(Authentication authentication, @RequestBody Map<String, Integer> body) {
        Long userId = (Long) authentication.getPrincipal();
        cartService.checkAll(userId, body.get("checked"));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> removeFromCart(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        cartService.removeFromCart(userId, id);
        return Result.success();
    }

    @DeleteMapping("/clear")
    public Result<?> clearCart(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        cartService.clearCart(userId);
        return Result.success();
    }
}
