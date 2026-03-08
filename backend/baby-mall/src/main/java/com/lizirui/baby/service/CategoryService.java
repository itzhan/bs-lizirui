package com.lizirui.baby.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lizirui.baby.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {

    List<Category> listTree();
}
