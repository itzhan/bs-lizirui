package com.lizirui.baby.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lizirui.baby.entity.UserBehavior;

public interface UserBehaviorService extends IService<UserBehavior> {

    void recordBehavior(Long userId, Long productId, String behaviorType);
}
