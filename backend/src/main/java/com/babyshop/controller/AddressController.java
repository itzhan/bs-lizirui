package com.babyshop.controller;

import com.babyshop.common.Result;
import com.babyshop.entity.Address;
import com.babyshop.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public Result<?> listAddresses(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(addressService.listByUser(userId));
    }

    @PostMapping
    public Result<?> addAddress(Authentication authentication, @RequestBody Address address) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(addressService.addAddress(userId, address));
    }

    @PutMapping("/{id}")
    public Result<?> updateAddress(Authentication authentication, @PathVariable Long id, @RequestBody Address address) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(addressService.updateAddress(userId, id, address));
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteAddress(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        addressService.deleteAddress(userId, id);
        return Result.success();
    }

    @PutMapping("/{id}/default")
    public Result<?> setDefault(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        addressService.setDefault(userId, id);
        return Result.success();
    }
}
