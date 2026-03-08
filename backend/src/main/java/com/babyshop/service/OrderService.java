package com.babyshop.service;

import com.babyshop.common.BusinessException;
import com.babyshop.common.PageResult;
import com.babyshop.dto.OrderCreateDTO;
import com.babyshop.entity.*;
import com.babyshop.mapper.OrderInfoMapper;
import com.babyshop.mapper.OrderItemMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderService extends ServiceImpl<OrderInfoMapper, OrderInfo> {

    private final OrderItemMapper orderItemMapper;
    private final CartService cartService;
    private final ProductService productService;
    private final AddressService addressService;

    @Transactional
    public OrderInfo createOrder(Long userId, OrderCreateDTO dto) {
        // 1. 获取收货地址
        Address address = addressService.getById(dto.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("收货地址不存在");
        }

        // 2. 获取购物车中已选中的商品
        List<Cart> cartItems = cartService.getCheckedItems(userId, dto.getCartIds());
        if (cartItems.isEmpty()) {
            throw new BusinessException("请选择要购买的商品");
        }

        // 3. 计算总金额 & 扣减库存
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (Cart cart : cartItems) {
            Product product = productService.getById(cart.getProductId());
            if (product == null || product.getStatus() != 1) {
                throw new BusinessException("商品 [" + (product != null ? product.getName() : cart.getProductId()) + "] 已下架");
            }
            if (product.getStock() < cart.getQuantity()) {
                throw new BusinessException("商品 [" + product.getName() + "] 库存不足");
            }

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getMainImage());
            item.setPrice(product.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setSubtotal(subtotal);
            orderItems.add(item);

            // 扣减库存
            productService.decreaseStock(product.getId(), cart.getQuantity());
        }

        // 4. 创建订单
        OrderInfo order = new OrderInfo();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0); // 待付款
        order.setReceiver(address.getReceiver());
        order.setPhone(address.getPhone());
        order.setAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
        order.setRemark(dto.getRemark());
        this.save(order);

        // 5. 保存订单商品明细
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        // 6. 清除购物车中已下单的商品
        for (Cart cart : cartItems) {
            cartService.removeById(cart.getId());
        }

        return order;
    }

    public Map<String, Object> getOrderDetail(Long orderId, Long userId) {
        OrderInfo order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (userId != null && !order.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此订单");
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("items", items);
        return result;
    }

    public PageResult<OrderInfo> listUserOrders(Long userId, Integer page, Integer size, Integer status) {
        Page<OrderInfo> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getUserId, userId);
        if (status != null) {
            wrapper.eq(OrderInfo::getStatus, status);
        }
        wrapper.orderByDesc(OrderInfo::getCreatedAt);
        Page<OrderInfo> result = this.page(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Transactional
    public void payOrder(Long userId, Long orderId) {
        OrderInfo order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不允许支付");
        }
        order.setStatus(1); // 待发货
        order.setPayTime(LocalDateTime.now());
        this.updateById(order);
    }

    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        OrderInfo order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BusinessException("订单状态不允许取消");
        }
        order.setStatus(4); // 已取消
        order.setCancelTime(LocalDateTime.now());
        this.updateById(order);

        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : items) {
            productService.increaseStock(item.getProductId(), item.getQuantity());
        }
    }

    public void confirmReceive(Long userId, Long orderId) {
        OrderInfo order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("订单状态不允许确认收货");
        }
        order.setStatus(3); // 已完成
        order.setFinishTime(LocalDateTime.now());
        this.updateById(order);
    }

    // ===== Admin operations =====

    public PageResult<OrderInfo> adminListOrders(Integer page, Integer size, Integer status,
                                                  String orderNo, String keyword) {
        Page<OrderInfo> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(OrderInfo::getStatus, status);
        }
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like(OrderInfo::getOrderNo, orderNo);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(OrderInfo::getReceiver, keyword)
                    .or().like(OrderInfo::getPhone, keyword));
        }
        wrapper.orderByDesc(OrderInfo::getCreatedAt);
        Page<OrderInfo> result = this.page(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    public void adminShipOrder(Long orderId) {
        OrderInfo order = this.getById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getStatus() != 1) throw new BusinessException("订单状态不允许发货");
        order.setStatus(2); // 已发货
        order.setShipTime(LocalDateTime.now());
        this.updateById(order);
    }

    @Transactional
    public void adminRefundOrder(Long orderId) {
        OrderInfo order = this.getById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        order.setStatus(5); // 已退款
        this.updateById(order);
        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : items) {
            productService.increaseStock(item.getProductId(), item.getQuantity());
        }
    }

    // ===== Statistics =====

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总订单数
        stats.put("totalOrders", this.count());

        // 待发货订单数
        stats.put("pendingShipment", this.count(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getStatus, 1)));

        // 已完成订单数
        long completedOrders = this.count(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getStatus, 3));
        stats.put("completedOrders", completedOrders);

        // 总销售额（已完成订单）
        List<OrderInfo> completedList = this.list(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getStatus, 3));
        BigDecimal totalRevenue = completedList.stream()
                .map(OrderInfo::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalRevenue", totalRevenue);

        return stats;
    }

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
