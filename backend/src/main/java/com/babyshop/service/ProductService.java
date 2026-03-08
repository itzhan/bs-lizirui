package com.babyshop.service;

import com.babyshop.common.BusinessException;
import com.babyshop.common.PageResult;
import com.babyshop.entity.Product;
import com.babyshop.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    public PageResult<Product> listProducts(Integer page, Integer size, Long categoryId,
                                            String keyword, String brand, Integer status,
                                            String sortBy, String sortOrder) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Product::getName, keyword)
                    .or().like(Product::getBrand, keyword)
                    .or().like(Product::getDescription, keyword));
        }
        if (brand != null && !brand.isEmpty()) {
            wrapper.eq(Product::getBrand, brand);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        } else {
            wrapper.eq(Product::getStatus, 1); // 默认只显示上架商品
        }

        // 排序
        if ("price".equals(sortBy)) {
            if ("desc".equals(sortOrder)) wrapper.orderByDesc(Product::getPrice);
            else wrapper.orderByAsc(Product::getPrice);
        } else if ("sales".equals(sortBy)) {
            wrapper.orderByDesc(Product::getSales);
        } else {
            wrapper.orderByDesc(Product::getCreatedAt);
        }

        Page<Product> result = this.page(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    public PageResult<Product> listRecommendProducts(Integer page, Integer size) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
               .eq(Product::getRecommend, 1)
               .orderByDesc(Product::getSales);
        Page<Product> result = this.page(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    public PageResult<Product> listHotProducts(Integer page, Integer size) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
               .orderByDesc(Product::getSales);
        Page<Product> result = this.page(pageParam, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    public void toggleStatus(Long productId, Integer status) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        product.setStatus(status);
        this.updateById(product);
    }

    public void decreaseStock(Long productId, Integer quantity) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStock() < quantity) {
            throw new BusinessException("商品 [" + product.getName() + "] 库存不足");
        }
        product.setStock(product.getStock() - quantity);
        product.setSales(product.getSales() + quantity);
        this.updateById(product);
    }

    public void increaseStock(Long productId, Integer quantity) {
        Product product = this.getById(productId);
        if (product != null) {
            product.setStock(product.getStock() + quantity);
            product.setSales(Math.max(0, product.getSales() - quantity));
            this.updateById(product);
        }
    }
}
