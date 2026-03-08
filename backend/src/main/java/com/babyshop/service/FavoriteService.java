package com.babyshop.service;

import com.babyshop.common.BusinessException;
import com.babyshop.common.PageResult;
import com.babyshop.entity.Favorite;
import com.babyshop.entity.Product;
import com.babyshop.mapper.FavoriteMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FavoriteService extends ServiceImpl<FavoriteMapper, Favorite> {

    private final ProductService productService;

    public PageResult<Map<String, Object>> listByUser(Long userId, Integer page, Integer size) {
        Page<Favorite> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .orderByDesc(Favorite::getCreatedAt);
        Page<Favorite> result = this.page(pageParam, wrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        for (Favorite fav : result.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", fav.getId());
            item.put("productId", fav.getProductId());
            item.put("createdAt", fav.getCreatedAt());
            Product product = productService.getById(fav.getProductId());
            if (product != null) {
                item.put("productName", product.getName());
                item.put("mainImage", product.getMainImage());
                item.put("price", product.getPrice());
                item.put("status", product.getStatus());
            }
            records.add(item);
        }

        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    public void addFavorite(Long userId, Long productId) {
        long count = this.count(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
        if (count > 0) {
            throw new BusinessException("已收藏过此商品");
        }
        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setProductId(productId);
        this.save(fav);
    }

    public void removeFavorite(Long userId, Long productId) {
        this.remove(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
    }

    public boolean isFavorited(Long userId, Long productId) {
        return this.count(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId)) > 0;
    }
}
