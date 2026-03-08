package com.lizirui.baby.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {

    private String nickname;

    private String phone;

    private String email;

    private String avatar;

    private Integer gender;
}
