package com.babyshop.service;

import com.babyshop.entity.Banner;
import com.babyshop.mapper.BannerMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerService extends ServiceImpl<BannerMapper, Banner> {

    public List<Banner> listActive() {
        return this.list(new LambdaQueryWrapper<Banner>()
                .eq(Banner::getStatus, 1)
                .orderByAsc(Banner::getSort));
    }

    public List<Banner> listAll() {
        return this.list(new LambdaQueryWrapper<Banner>()
                .orderByAsc(Banner::getSort));
    }
}
