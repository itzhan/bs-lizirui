package com.lizirui.baby.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lizirui.baby.entity.UserBehavior;
import com.lizirui.baby.mapper.UserBehaviorMapper;
import com.lizirui.baby.service.UserBehaviorService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserBehaviorServiceImpl extends ServiceImpl<UserBehaviorMapper, UserBehavior> implements UserBehaviorService {

    @Override
    public void recordBehavior(Long userId, Long productId, String behaviorType) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setProductId(productId);
        behavior.setBehaviorType(behaviorType);
        behavior.setCreatedAt(LocalDateTime.now());
        save(behavior);
    }
}
