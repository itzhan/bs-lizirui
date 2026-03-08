package com.lizirui.baby.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BannerDTO {

    private String title;

    @NotBlank(message = "图片不能为空")
    private String image;

    private String url;

    private Integer sort;

    private Integer status;
}
