package com.lizirui.baby.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lizirui.baby.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
