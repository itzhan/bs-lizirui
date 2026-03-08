package com.babyshop.controller;

import com.babyshop.common.Result;
import com.babyshop.entity.User;
import com.babyshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public Result<?> getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(userService.getCurrentUser(userId));
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(Authentication authentication, @RequestBody User user) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(userService.updateProfile(userId, user));
    }

    @PutMapping("/password")
    public Result<?> changePassword(Authentication authentication, @RequestBody Map<String, String> body) {
        Long userId = (Long) authentication.getPrincipal();
        userService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return Result.success("密码修改成功");
    }
}
