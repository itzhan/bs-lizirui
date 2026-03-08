# 母婴商城系统 API 文档

## 概览
- **Base URL**: `http://localhost:8080`
- **认证方式**: JWT Bearer Token
- **Content-Type**: `application/json`
- **分页参数**: `page`（页码，从1开始）、`size`（每页条数）

## 统一响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1709000000000
}
```

## 错误码
| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 401 | 未登录或Token过期 |
| 403 | 无权访问 |
| 500 | 服务器内部错误 |

## 测试账号
| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | user1 | user123 |

---

## 一、认证模块 `/api/auth`

### 1.1 登录
- **POST** `/api/auth/login`
- **无需认证**
- 请求体：
```json
{ "username": "admin", "password": "admin123" }
```
- 响应：
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGci...",
    "user": { "id": 1, "username": "admin", "nickname": "系统管理员", "role": "ADMIN", ... }
  }
}
```

### 1.2 注册
- **POST** `/api/auth/register`
- **无需认证**
- 请求体：
```json
{ "username": "newuser", "password": "123456", "nickname": "新用户", "phone": "13800000000", "email": "test@qq.com" }
```

---

## 二、用户模块 `/api/user`

### 2.1 获取当前用户信息
- **GET** `/api/user/info`
- **需要认证**

### 2.2 更新个人信息
- **PUT** `/api/user/profile`
- **需要认证**
- 请求体：
```json
{ "nickname": "新昵称", "phone": "13900000000", "email": "new@qq.com", "avatar": "url", "gender": 2 }
```

### 2.3 修改密码
- **PUT** `/api/user/password`
- **需要认证**
- 请求体：
```json
{ "oldPassword": "123456", "newPassword": "654321" }
```

---

## 三、商品模块 `/api/products`

### 3.1 商品列表（公开）
- **GET** `/api/products`
- 查询参数：`page`, `size`, `categoryId`, `keyword`, `brand`, `sortBy`(price/sales), `sortOrder`(asc/desc)
- 响应示例：
```json
{
  "code": 200,
  "data": {
    "records": [{ "id": 1, "name": "飞鹤星飞帆3段", "price": 298.00, "sales": 1230, ... }],
    "total": 32, "page": 1, "size": 12
  }
}
```

### 3.2 商品详情（公开）
- **GET** `/api/products/{id}`

### 3.3 推荐商品（公开）
- **GET** `/api/products/recommend`
- 查询参数：`page`, `size`

### 3.4 热销商品（公开）
- **GET** `/api/products/hot`
- 查询参数：`page`, `size`

---

## 四、分类模块 `/api/categories`

### 4.1 所有分类（公开）
- **GET** `/api/categories`

### 4.2 分类树（公开）
- **GET** `/api/categories/tree`

### 4.3 分类详情（公开）
- **GET** `/api/categories/{id}`

---

## 五、购物车模块 `/api/cart`

### 5.1 购物车列表
- **GET** `/api/cart`
- **需要认证**

### 5.2 添加到购物车
- **POST** `/api/cart`
- 请求体：`{ "productId": 1, "quantity": 2 }`

### 5.3 修改数量
- **PUT** `/api/cart/{id}/quantity`
- 请求体：`{ "quantity": 3 }`

### 5.4 勾选/取消勾选
- **PUT** `/api/cart/{id}/checked`
- 请求体：`{ "checked": 1 }`

### 5.5 全选/全不选
- **PUT** `/api/cart/checkAll`
- 请求体：`{ "checked": 1 }`

### 5.6 删除购物车商品
- **DELETE** `/api/cart/{id}`

### 5.7 清空购物车
- **DELETE** `/api/cart/clear`

---

## 六、订单模块 `/api/orders`

### 6.1 创建订单
- **POST** `/api/orders`
- **需要认证**
- 请求体：
```json
{ "addressId": 1, "remark": "备注", "cartIds": [1, 2] }
```

### 6.2 我的订单列表
- **GET** `/api/orders`
- 查询参数：`page`, `size`, `status`（0待付款/1待发货/2已发货/3已完成/4已取消/5已退款）

### 6.3 订单详情
- **GET** `/api/orders/{id}`
- 响应包含 `order` 和 `items` 两个字段

### 6.4 支付订单
- **PUT** `/api/orders/{id}/pay`

### 6.5 取消订单
- **PUT** `/api/orders/{id}/cancel`

### 6.6 确认收货
- **PUT** `/api/orders/{id}/confirm`

---

## 七、收货地址模块 `/api/addresses`

### 7.1 地址列表
- **GET** `/api/addresses`
- **需要认证**

### 7.2 新增地址
- **POST** `/api/addresses`
- 请求体：
```json
{ "receiver": "张三", "phone": "13800000000", "province": "北京市", "city": "北京市", "district": "朝阳区", "detail": "XX路XX号", "isDefault": 1 }
```

### 7.3 修改地址
- **PUT** `/api/addresses/{id}`

### 7.4 删除地址
- **DELETE** `/api/addresses/{id}`

### 7.5 设为默认
- **PUT** `/api/addresses/{id}/default`

---

## 八、评价模块 `/api/reviews`

### 8.1 商品评价列表（公开）
- **GET** `/api/reviews/product/{productId}`
- 查询参数：`page`, `size`

### 8.2 发表评价
- **POST** `/api/reviews`
- **需要认证**
- 请求体：
```json
{ "productId": 1, "orderId": 1, "rating": 5, "content": "非常好", "images": "[\"url1\"]" }
```

---

## 九、收藏模块 `/api/favorites`

### 9.1 收藏列表
- **GET** `/api/favorites`
- 查询参数：`page`, `size`

### 9.2 添加收藏
- **POST** `/api/favorites/{productId}`

### 9.3 取消收藏
- **DELETE** `/api/favorites/{productId}`

### 9.4 检查是否已收藏
- **GET** `/api/favorites/check/{productId}`

---

## 十、公告模块 `/api/announcements`

### 10.1 公告列表（公开）
- **GET** `/api/announcements`
- 查询参数：`page`, `size`

### 10.2 公告详情（公开）
- **GET** `/api/announcements/{id}`

---

## 十一、轮播图模块 `/api/banners`

### 11.1 轮播图列表（公开）
- **GET** `/api/banners`

---

## 十二、文件上传 `/api/upload`

### 12.1 上传文件
- **POST** `/api/upload`
- **需要认证**
- Content-Type: `multipart/form-data`
- 参数：`file`（文件），`type`（子目录，默认images）

---

## 十三、管理员模块 `/api/admin`（需ADMIN角色）

### 用户管理
- **GET** `/api/admin/users` — 查询参数：`page`, `size`, `keyword`, `status`
- **PUT** `/api/admin/users/{id}/status` — 请求体：`{ "status": 0 }`

### 商品管理
- **GET** `/api/admin/products` — 查询参数：`page`, `size`, `categoryId`, `keyword`, `status`
- **POST** `/api/admin/products` — 新增商品
- **PUT** `/api/admin/products/{id}` — 编辑商品
- **DELETE** `/api/admin/products/{id}` — 删除商品
- **PUT** `/api/admin/products/{id}/status` — 上/下架：`{ "status": 1 }`

### 分类管理
- **GET** `/api/admin/categories` — 所有分类
- **POST** `/api/admin/categories` — 新增分类
- **PUT** `/api/admin/categories/{id}` — 编辑分类
- **DELETE** `/api/admin/categories/{id}` — 删除分类

### 订单管理
- **GET** `/api/admin/orders` — 查询参数：`page`, `size`, `status`, `orderNo`, `keyword`
- **GET** `/api/admin/orders/{id}` — 订单详情（含 items）
- **PUT** `/api/admin/orders/{id}/ship` — 发货
- **PUT** `/api/admin/orders/{id}/refund` — 退款

### 评论管理
- **GET** `/api/admin/reviews` — 查询参数：`page`, `size`, `productId`
- **DELETE** `/api/admin/reviews/{id}` — 删除评论
- **PUT** `/api/admin/reviews/{id}/status` — 隐藏/显示：`{ "status": 0 }`

### 公告管理
- **GET** `/api/admin/announcements` — 查询参数：`page`, `size`
- **POST** `/api/admin/announcements` — 新增公告
- **PUT** `/api/admin/announcements/{id}` — 编辑公告
- **DELETE** `/api/admin/announcements/{id}` — 删除公告

### 轮播图管理
- **GET** `/api/admin/banners` — 所有轮播图
- **POST** `/api/admin/banners` — 新增轮播图
- **PUT** `/api/admin/banners/{id}` — 编辑轮播图
- **DELETE** `/api/admin/banners/{id}` — 删除轮播图

### 数据统计
- **GET** `/api/admin/statistics` — 订单统计（总订单/待发货/已完成/总销售额）
- **GET** `/api/admin/statistics/users` — 用户统计
- **GET** `/api/admin/statistics/products` — 商品统计
