package com.lizirui.baby.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lizirui.baby.entity.Address;
import com.lizirui.baby.mapper.AddressMapper;
import com.lizirui.baby.service.AddressService;
import org.springframework.stereotype.Service;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {
}
