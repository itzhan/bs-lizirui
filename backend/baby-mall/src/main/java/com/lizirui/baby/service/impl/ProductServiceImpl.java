package com.lizirui.baby.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lizirui.baby.entity.Product;
import com.lizirui.baby.mapper.ProductMapper;
import com.lizirui.baby.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
}
