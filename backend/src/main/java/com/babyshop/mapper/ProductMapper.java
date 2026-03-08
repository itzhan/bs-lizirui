package com.babyshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.babyshop.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
