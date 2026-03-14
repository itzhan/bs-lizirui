package com.babyshop.service;

import com.babyshop.common.BusinessException;
import com.babyshop.common.PageResult;
import com.babyshop.dto.ReviewDTO;
import com.babyshop.entity.Review;
import com.babyshop.entity.User;
import com.babyshop.mapper.ReviewMapper;
import com.babyshop.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ReviewService extends ServiceImpl<ReviewMapper, Review> {

    private final UserMapper userMapper;
    private final UserBehaviorService userBehaviorService;

    public PageResult<Map<String, Object>> listByProduct(Long productId, Integer page, Integer size) {
        Page<Review> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId)
               .eq(Review::getStatus, 1)
               .orderByDesc(Review::getCreatedAt);
        Page<Review> result = this.page(pageParam, wrapper);

        List<Map<String, Object>> records = new ArrayList<>();
        for (Review review : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", review.getId());
            map.put("rating", review.getRating());
            map.put("content", review.getContent());
            map.put("images", review.getImages());
            map.put("createdAt", review.getCreatedAt());
            // 用户昵称
            User user = userMapper.selectById(review.getUserId());
            map.put("nickname", user != null ? user.getNickname() : "匿名用户");
            map.put("avatar", user != null ? user.getAvatar() : null);
            records.add(map);
        }

        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    public Review addReview(Long userId, ReviewDTO dto) {
        // 检查是否已评价
        long count = this.count(new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, userId)
                .eq(Review::getProductId, dto.getProductId())
                .eq(Review::getOrderId, dto.getOrderId()));
        if (count > 0) {
            throw new BusinessException("您已评价过此商品");
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(dto.getProductId());
        review.setOrderId(dto.getOrderId());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setImages(dto.getImages());
        review.setStatus(1);
        this.save(review);
        // 记录评价行为（协同过滤数据采集）
        userBehaviorService.recordRate(userId, dto.getProductId(), dto.getRating());
        return review;
    }

    public PageResult<Review> adminListReviews(Integer page, Integer size, Long productId) {
        Page<Review> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        if (productId != null) {
            wrapper.eq(Review::getProductId, productId);
        }
        wrapper.orderByDesc(Review::getCreatedAt);
        Page<Review> result = this.page(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }
}
