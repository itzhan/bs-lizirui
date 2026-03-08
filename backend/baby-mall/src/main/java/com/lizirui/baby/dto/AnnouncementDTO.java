package com.lizirui.baby.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnnouncementDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;

    private Integer status;
}
