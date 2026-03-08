package com.babyshop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.babyshop.mapper")
public class BabyShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(BabyShopApplication.class, args);
    }
}
