package com.lizirui.baby.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lizirui.baby.entity.Banner;
import com.lizirui.baby.mapper.BannerMapper;
import com.lizirui.baby.service.BannerService;
import org.springframework.stereotype.Service;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {
}
