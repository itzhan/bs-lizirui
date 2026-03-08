package com.lizirui.baby.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long categoryId;

    private String name;

    private String description;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private String coverImage;

    private String images;

    private Integer stock;

    private Integer sales;

    private Integer status;

    private Integer isRecommend;

    private Integer sort;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
