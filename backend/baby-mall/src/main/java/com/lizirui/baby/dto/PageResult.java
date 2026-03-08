package com.lizirui.baby.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private List<T> records;
    private Long total;
    private Long page;
    private Long size;

    public PageResult(IPage<T> pageData) {
        this.records = pageData.getRecords();
        this.total = pageData.getTotal();
        this.page = pageData.getCurrent();
        this.size = pageData.getSize();
    }
}
