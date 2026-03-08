package com.lizirui.baby.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lizirui.baby.dto.OrderCreateDTO;
import com.lizirui.baby.dto.OrderItemDTO;
import com.lizirui.baby.entity.*;
import com.lizirui.baby.mapper.*;
import com.lizirui.baby.service.OrderService;
import com.lizirui.baby.service.UserBehaviorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private UserBehaviorService userBehaviorService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId, OrderCreateDTO dto) {
        Address address = addressMapper.selectById(dto.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("收货地址不存在");
        }

        List<OrderItemDTO> itemDTOs = dto.getItems();
        boolean fromCart = (itemDTOs == null || itemDTOs.isEmpty());

        if (fromCart) {
            List<Cart> cartItems = cartMapper.selectList(
                    new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
            if (cartItems.isEmpty()) {
                throw new RuntimeException("购物车为空");
            }
            itemDTOs = new ArrayList<>();
            for (Cart cart : cartItems) {
                OrderItemDTO oi = new OrderItemDTO();
                oi.setProductId(cart.getProductId());
                oi.setQuantity(cart.getQuantity());
                itemDTOs.add(oi);
            }
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDTO itemDTO : itemDTOs) {
            Product product = productMapper.selectById(itemDTO.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在: " + itemDTO.getProductId());
            }
            if (product.getStock() < itemDTO.getQuantity()) {
                throw new RuntimeException("商品库存不足: " + product.getName());
            }

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getCoverImage());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setTotalPrice(itemTotal);
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItems.add(orderItem);
        }

        String orderNo = "ORD" + System.currentTimeMillis()
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNo(orderNo);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setStatus(0);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverAddress(address.getProvince() + address.getCity()
                + address.getDistrict() + address.getDetail());
        order.setRemark(dto.getRemark());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        save(order);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            orderItemMapper.insert(orderItem);

            Product product = productMapper.selectById(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            product.setSales(product.getSales() + orderItem.getQuantity());
            productMapper.updateById(product);

            userBehaviorService.recordBehavior(userId, orderItem.getProductId(), "purchase");
        }

        if (fromCart) {
            cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        }

        return order;
    }

    @Override
    public void cancelOrder(Long userId, Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("当前订单状态不可取消");
        }
        order.setStatus(4);
        order.setUpdatedAt(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public void payOrder(Long userId, Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("当前订单状态不可支付");
        }
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public void deliverOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new RuntimeException("当前订单状态不可发货");
        }
        order.setStatus(2);
        order.setDeliverTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public void receiveOrder(Long userId, Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (order.getStatus() != 2) {
            throw new RuntimeException("当前订单状态不可确认收货");
        }
        order.setStatus(3);
        order.setReceiveTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        updateById(order);
    }
}
