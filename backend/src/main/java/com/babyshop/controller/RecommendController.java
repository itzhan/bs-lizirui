package com.babyshop.controller;

import com.babyshop.common.Result;
import com.babyshop.entity.Product;
import com.babyshop.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 推荐接口控制器
 * 提供基于协同过滤算法的个性化商品推荐
 */
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    /**
     * 协同过滤个性化推荐
     * 登录用户返回个性化推荐，未登录用户返回热销商品
     *
     * @param size 推荐数量，默认8
     */
    @GetMapping("/cf")
    public Result<?> cfRecommend(Authentication authentication,
                                  @RequestParam(defaultValue = "8") Integer size) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            userId = (Long) authentication.getPrincipal();
        }
        List<Product> recommendations = recommendService.getRecommendations(userId, size);
        return Result.success(recommendations);
    }

    /**
     * 获取某商品的相似商品推荐
     * 基于协同过滤的物品相似度计算
     *
     * @param productId 商品ID
     * @param size      推荐数量，默认5
     */
    @GetMapping("/similar/{productId}")
    public Result<?> similarProducts(@PathVariable Long productId,
                                      @RequestParam(defaultValue = "5") Integer size) {
        List<Product> similar = recommendService.getSimilarProducts(productId, size);
        return Result.success(similar);
    }
}
