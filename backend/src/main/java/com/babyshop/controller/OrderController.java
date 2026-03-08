package com.babyshop.controller;

import com.babyshop.common.Result;
import com.babyshop.dto.OrderCreateDTO;
import com.babyshop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<?> createOrder(Authentication authentication, @Valid @RequestBody OrderCreateDTO dto) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success("下单成功", orderService.createOrder(userId, dto));
    }

    @GetMapping
    public Result<?> listOrders(Authentication authentication,
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size,
                                @RequestParam(required = false) Integer status) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(orderService.listUserOrders(userId, page, size, status));
    }

    @GetMapping("/{id}")
    public Result<?> getOrderDetail(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(orderService.getOrderDetail(id, userId));
    }

    @PutMapping("/{id}/pay")
    public Result<?> payOrder(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        orderService.payOrder(userId, id);
        return Result.success("支付成功");
    }

    @PutMapping("/{id}/cancel")
    public Result<?> cancelOrder(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        orderService.cancelOrder(userId, id);
        return Result.success("取消成功");
    }

    @PutMapping("/{id}/confirm")
    public Result<?> confirmReceive(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        orderService.confirmReceive(userId, id);
        return Result.success("确认收货成功");
    }
}
