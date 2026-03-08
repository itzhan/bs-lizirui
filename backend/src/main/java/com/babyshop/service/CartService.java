package com.babyshop.service;

import com.babyshop.common.BusinessException;
import com.babyshop.dto.CartDTO;
import com.babyshop.entity.Cart;
import com.babyshop.entity.Product;
import com.babyshop.mapper.CartMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService extends ServiceImpl<CartMapper, Cart> {

    private final ProductService productService;

    public List<Map<String, Object>> listCartItems(Long userId) {
        List<Cart> carts = this.list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreatedAt));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Cart cart : carts) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", cart.getId());
            item.put("productId", cart.getProductId());
            item.put("quantity", cart.getQuantity());
            item.put("checked", cart.getChecked());

            Product product = productService.getById(cart.getProductId());
            if (product != null) {
                item.put("productName", product.getName());
                item.put("mainImage", product.getMainImage());
                item.put("price", product.getPrice());
                item.put("stock", product.getStock());
                item.put("productStatus", product.getStatus());
            }
            result.add(item);
        }
        return result;
    }

    public Cart addToCart(Long userId, CartDTO dto) {
        Product product = productService.getById(dto.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }
        if (product.getStock() < dto.getQuantity()) {
            throw new BusinessException("库存不足");
        }

        // 检查是否已存在
        Cart existing = this.getOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, dto.getProductId()));

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + dto.getQuantity());
            this.updateById(existing);
            return existing;
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(dto.getProductId());
            cart.setQuantity(dto.getQuantity());
            cart.setChecked(1);
            this.save(cart);
            return cart;
        }
    }

    public void updateQuantity(Long userId, Long cartId, Integer quantity) {
        Cart cart = this.getById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车商品不存在");
        }
        cart.setQuantity(quantity);
        this.updateById(cart);
    }

    public void toggleChecked(Long userId, Long cartId, Integer checked) {
        Cart cart = this.getById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车商品不存在");
        }
        cart.setChecked(checked);
        this.updateById(cart);
    }

    public void checkAll(Long userId, Integer checked) {
        List<Cart> carts = this.list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId));
        for (Cart cart : carts) {
            cart.setChecked(checked);
            this.updateById(cart);
        }
    }

    public void removeFromCart(Long userId, Long cartId) {
        Cart cart = this.getById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车商品不存在");
        }
        this.removeById(cartId);
    }

    public void clearCart(Long userId) {
        this.remove(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId));
    }

    public List<Cart> getCheckedItems(Long userId, List<Long> cartIds) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getChecked, 1);
        if (cartIds != null && !cartIds.isEmpty()) {
            wrapper.in(Cart::getId, cartIds);
        }
        return this.list(wrapper);
    }
}
