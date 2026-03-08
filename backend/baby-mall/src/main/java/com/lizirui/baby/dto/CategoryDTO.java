package com.lizirui.baby.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryDTO {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private Long parentId = 0L;

    private String icon;

    private Integer sort;

    private Integer status;
}
