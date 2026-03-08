package com.lizirui.baby.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lizirui.baby.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
