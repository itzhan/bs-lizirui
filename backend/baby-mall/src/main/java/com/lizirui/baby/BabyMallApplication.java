package com.lizirui.baby;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lizirui.baby.mapper")
public class BabyMallApplication {
    public static void main(String[] args) {
        SpringApplication.run(BabyMallApplication.class, args);
    }
}
