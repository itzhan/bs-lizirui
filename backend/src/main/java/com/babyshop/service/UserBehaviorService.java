package com.babyshop.service;

import com.babyshop.entity.UserBehavior;
import com.babyshop.mapper.UserBehaviorMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 用户行为记录服务
 * 异步记录用户行为，不影响主业务流程性能
 */
@Service
public class UserBehaviorService extends ServiceImpl<UserBehaviorMapper, UserBehavior> {

    /**
     * 行为权重常量
     */
    public static final double SCORE_VIEW = 1.0;
    public static final double SCORE_CART = 3.0;
    public static final double SCORE_FAVORITE = 4.0;
    public static final double SCORE_PURCHASE = 5.0;

    /**
     * 记录用户行为
     * @param userId 用户ID
     * @param productId 商品ID
     * @param behaviorType 行为类型：view/cart/purchase/favorite/rate
     * @param score 行为评分权重
     */
    public void recordBehavior(Long userId, Long productId, String behaviorType, double score) {
        if (userId == null || productId == null) return;
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setProductId(productId);
        behavior.setBehaviorType(behaviorType);
        behavior.setScore(score);
        this.save(behavior);
    }

    /**
     * 记录浏览行为
     */
    public void recordView(Long userId, Long productId) {
        recordBehavior(userId, productId, "view", SCORE_VIEW);
    }

    /**
     * 记录加入购物车行为
     */
    public void recordCart(Long userId, Long productId) {
        recordBehavior(userId, productId, "cart", SCORE_CART);
    }

    /**
     * 记录收藏行为
     */
    public void recordFavorite(Long userId, Long productId) {
        recordBehavior(userId, productId, "favorite", SCORE_FAVORITE);
    }

    /**
     * 记录购买行为
     */
    public void recordPurchase(Long userId, Long productId) {
        recordBehavior(userId, productId, "purchase", SCORE_PURCHASE);
    }

    /**
     * 记录评价行为（使用实际评分作为分数）
     */
    public void recordRate(Long userId, Long productId, int rating) {
        recordBehavior(userId, productId, "rate", rating);
    }
}
