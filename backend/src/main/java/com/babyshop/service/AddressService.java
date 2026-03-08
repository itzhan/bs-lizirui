package com.babyshop.service;

import com.babyshop.common.BusinessException;
import com.babyshop.entity.Address;
import com.babyshop.mapper.AddressMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService extends ServiceImpl<AddressMapper, Address> {

    public List<Address> listByUser(Long userId) {
        return this.list(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getCreatedAt));
    }

    public Address addAddress(Long userId, Address address) {
        address.setUserId(userId);
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefault(userId);
        }
        this.save(address);
        return address;
    }

    public Address updateAddress(Long userId, Long addressId, Address update) {
        Address address = this.getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        if (update.getReceiver() != null) address.setReceiver(update.getReceiver());
        if (update.getPhone() != null) address.setPhone(update.getPhone());
        if (update.getProvince() != null) address.setProvince(update.getProvince());
        if (update.getCity() != null) address.setCity(update.getCity());
        if (update.getDistrict() != null) address.setDistrict(update.getDistrict());
        if (update.getDetail() != null) address.setDetail(update.getDetail());
        if (update.getIsDefault() != null) {
            if (update.getIsDefault() == 1) clearDefault(userId);
            address.setIsDefault(update.getIsDefault());
        }
        this.updateById(address);
        return address;
    }

    public void deleteAddress(Long userId, Long addressId) {
        Address address = this.getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        this.removeById(addressId);
    }

    public void setDefault(Long userId, Long addressId) {
        Address address = this.getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        clearDefault(userId);
        address.setIsDefault(1);
        this.updateById(address);
    }

    private void clearDefault(Long userId) {
        List<Address> addresses = this.list(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1));
        for (Address a : addresses) {
            a.setIsDefault(0);
            this.updateById(a);
        }
    }
}
