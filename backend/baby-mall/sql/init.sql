CREATE DATABASE IF NOT EXISTS baby_mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE baby_mall;
SET NAMES utf8mb4;
SET CHARACTER_SET_CLIENT = utf8mb4;
SET CHARACTER_SET_RESULTS = utf8mb4;
SET CHARACTER_SET_CONNECTION = utf8mb4;

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `username`   VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`   VARCHAR(255) NOT NULL COMMENT '密码',
    `nickname`   VARCHAR(50)           DEFAULT NULL COMMENT '昵称',
    `phone`      VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
    `email`      VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    `avatar`     VARCHAR(255)          DEFAULT NULL COMMENT '头像',
    `gender`     TINYINT               DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
    `role`       VARCHAR(20)           DEFAULT 'user' COMMENT '角色 user/admin',
    `status`     TINYINT               DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`    TINYINT               DEFAULT 0 COMMENT '逻辑删除 0-未删 1-已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表';

-- ----------------------------
-- 商品分类表
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `name`       VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id`  BIGINT               DEFAULT 0 COMMENT '父分类ID，0为顶级',
    `icon`       VARCHAR(255)         DEFAULT NULL COMMENT '图标',
    `sort`       INT                  DEFAULT 0 COMMENT '排序值',
    `status`     TINYINT              DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`    TINYINT              DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品分类表';

-- ----------------------------
-- 商品表
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT,
    `category_id`    BIGINT        NOT NULL COMMENT '分类ID',
    `name`           VARCHAR(200)  NOT NULL COMMENT '商品名称',
    `description`    TEXT                   DEFAULT NULL COMMENT '商品描述',
    `price`          DECIMAL(10,2) NOT NULL COMMENT '售价',
    `original_price` DECIMAL(10,2)          DEFAULT NULL COMMENT '原价',
    `cover_image`    VARCHAR(500)           DEFAULT NULL COMMENT '封面图',
    `images`         TEXT                   DEFAULT NULL COMMENT '商品图片，逗号分隔',
    `stock`          INT                    DEFAULT 0 COMMENT '库存',
    `sales`          INT                    DEFAULT 0 COMMENT '销量',
    `status`         TINYINT                DEFAULT 1 COMMENT '状态 0-下架 1-上架',
    `is_recommend`   TINYINT                DEFAULT 0 COMMENT '是否推荐 0-否 1-是',
    `sort`           INT                    DEFAULT 0 COMMENT '排序值',
    `created_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        TINYINT                DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品表';

-- ----------------------------
-- 购物车表
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `product_id` BIGINT   NOT NULL COMMENT '商品ID',
    `quantity`   INT               DEFAULT 1 COMMENT '数量',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '购物车表';

-- ----------------------------
-- 收货地址表
-- ----------------------------
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT       NOT NULL COMMENT '用户ID',
    `receiver_name` VARCHAR(50)  NOT NULL COMMENT '收货人',
    `phone`         VARCHAR(20)  NOT NULL COMMENT '联系电话',
    `province`      VARCHAR(50)           DEFAULT NULL COMMENT '省',
    `city`          VARCHAR(50)           DEFAULT NULL COMMENT '市',
    `district`      VARCHAR(50)           DEFAULT NULL COMMENT '区',
    `detail`        VARCHAR(255) NOT NULL COMMENT '详细地址',
    `is_default`    TINYINT               DEFAULT 0 COMMENT '是否默认 0-否 1-是',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`       TINYINT               DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '收货地址表';

-- ----------------------------
-- 订单表
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT,
    `user_id`          BIGINT        NOT NULL COMMENT '用户ID',
    `order_no`         VARCHAR(32)   NOT NULL COMMENT '订单编号',
    `total_amount`     DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `pay_amount`       DECIMAL(10,2)          DEFAULT NULL COMMENT '实付金额',
    `status`           TINYINT                DEFAULT 0 COMMENT '状态 0-待付款 1-待发货 2-已发货 3-已完成 4-已取消',
    `receiver_name`    VARCHAR(50)            DEFAULT NULL COMMENT '收货人',
    `receiver_phone`   VARCHAR(20)            DEFAULT NULL COMMENT '收货电话',
    `receiver_address` VARCHAR(500)           DEFAULT NULL COMMENT '收货地址',
    `pay_time`         DATETIME               DEFAULT NULL COMMENT '支付时间',
    `deliver_time`     DATETIME               DEFAULT NULL COMMENT '发货时间',
    `receive_time`     DATETIME               DEFAULT NULL COMMENT '收货时间',
    `remark`           VARCHAR(500)           DEFAULT NULL COMMENT '备注',
    `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`          TINYINT                DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单表';

-- ----------------------------
-- 订单明细表
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT,
    `order_id`      BIGINT        NOT NULL COMMENT '订单ID',
    `product_id`    BIGINT        NOT NULL COMMENT '商品ID',
    `product_name`  VARCHAR(200)           DEFAULT NULL COMMENT '商品名称快照',
    `product_image` VARCHAR(500)           DEFAULT NULL COMMENT '商品图片快照',
    `price`         DECIMAL(10,2)          DEFAULT NULL COMMENT '单价',
    `quantity`      INT                    DEFAULT NULL COMMENT '数量',
    `total_price`   DECIMAL(10,2)          DEFAULT NULL COMMENT '小计',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单明细表';

-- ----------------------------
-- 评价表
-- ----------------------------
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `product_id` BIGINT   NOT NULL COMMENT '商品ID',
    `order_id`   BIGINT            DEFAULT NULL COMMENT '订单ID',
    `rating`     TINYINT  NOT NULL COMMENT '评分 1-5',
    `content`    TEXT              DEFAULT NULL COMMENT '评价内容',
    `images`     TEXT              DEFAULT NULL COMMENT '评价图片',
    `status`     TINYINT           DEFAULT 1 COMMENT '状态 0-隐藏 1-显示',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`    TINYINT           DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品评价表';

-- ----------------------------
-- 用户行为表（协同过滤推荐）
-- ----------------------------
DROP TABLE IF EXISTS `user_behavior`;
CREATE TABLE `user_behavior` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT      NOT NULL COMMENT '用户ID',
    `product_id`    BIGINT      NOT NULL COMMENT '商品ID',
    `behavior_type` VARCHAR(20) NOT NULL COMMENT '行为类型 view/cart/purchase/rate/favorite',
    `created_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_behavior_type` (`behavior_type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户行为记录表';

-- ----------------------------
-- 收藏表
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `product_id` BIGINT   NOT NULL COMMENT '商品ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '收藏表';

-- ----------------------------
-- 轮播图表
-- ----------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `title`      VARCHAR(100)          DEFAULT NULL COMMENT '标题',
    `image`      VARCHAR(500) NOT NULL COMMENT '图片地址',
    `url`        VARCHAR(500)          DEFAULT NULL COMMENT '跳转链接',
    `sort`       INT                   DEFAULT 0 COMMENT '排序值',
    `status`     TINYINT               DEFAULT 1 COMMENT '状态 0-隐藏 1-显示',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`    TINYINT               DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '轮播图表';

-- ----------------------------
-- 公告表
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `title`      VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content`    TEXT                  DEFAULT NULL COMMENT '公告内容',
    `status`     TINYINT               DEFAULT 1 COMMENT '状态 0-草稿 1-发布',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`    TINYINT               DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '公告表';

-- ----------------------------
-- 初始管理员账号 (密码: admin123 BCrypt加密)
-- ----------------------------
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin', 1);
