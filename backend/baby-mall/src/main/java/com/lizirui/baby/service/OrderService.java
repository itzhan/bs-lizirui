package com.lizirui.baby.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lizirui.baby.dto.OrderCreateDTO;
import com.lizirui.baby.entity.Order;

public interface OrderService extends IService<Order> {

    Order createOrder(Long userId, OrderCreateDTO dto);

    void cancelOrder(Long userId, Long orderId);

    void payOrder(Long userId, Long orderId);

    void deliverOrder(Long orderId);

    void receiveOrder(Long userId, Long orderId);
}
