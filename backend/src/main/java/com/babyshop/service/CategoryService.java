package com.babyshop.service;

import com.babyshop.entity.Category;
import com.babyshop.mapper.CategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {

    public List<Category> listTree() {
        List<Category> all = this.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort));
        return buildTree(all, 0L);
    }

    public List<Category> listAll() {
        return this.list(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort));
    }

    private List<Category> buildTree(List<Category> all, Long parentId) {
        // 简化的树形构建：不在 Category 实体中加 children 字段
        // 前端根据 parentId 自行构建
        return all.stream()
                .filter(c -> parentId.equals(c.getParentId()))
                .collect(Collectors.toList());
    }

    /** 获取某分类的所有子分类ID（含自身） */
    public List<Long> getChildIds(Long categoryId) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);
        List<Category> children = this.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, categoryId));
        for (Category child : children) {
            ids.addAll(getChildIds(child.getId()));
        }
        return ids;
    }
}
