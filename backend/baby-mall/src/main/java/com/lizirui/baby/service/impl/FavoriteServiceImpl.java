package com.lizirui.baby.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lizirui.baby.entity.Favorite;
import com.lizirui.baby.mapper.FavoriteMapper;
import com.lizirui.baby.service.FavoriteService;
import org.springframework.stereotype.Service;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {
}
