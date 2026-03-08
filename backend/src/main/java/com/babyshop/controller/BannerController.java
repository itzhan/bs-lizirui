package com.babyshop.controller;

import com.babyshop.common.Result;
import com.babyshop.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping
    public Result<?> listBanners() {
        return Result.success(bannerService.listActive());
    }
}
