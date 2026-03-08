package com.babyshop.controller;

import com.babyshop.common.Result;
import com.babyshop.entity.*;
import com.babyshop.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final AnnouncementService announcementService;
    private final BannerService bannerService;

    // ========== 用户管理 ==========
    @GetMapping("/users")
    public Result<?> listUsers(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer status) {
        return Result.success(userService.adminListUsers(page, size, keyword, status));
    }

    @PutMapping("/users/{id}/status")
    public Result<?> toggleUserStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        userService.toggleUserStatus(id, body.get("status"));
        return Result.success();
    }

    // ========== 商品管理 ==========
    @GetMapping("/products")
    public Result<?> listProducts(@RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size,
                                  @RequestParam(required = false) Long categoryId,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) Integer status) {
        return Result.success(productService.listProducts(page, size, categoryId, keyword, null, status, null, null));
    }

    @PostMapping("/products")
    public Result<?> addProduct(@RequestBody Product product) {
        productService.save(product);
        return Result.success("添加成功", product);
    }

    @PutMapping("/products/{id}")
    public Result<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        productService.updateById(product);
        return Result.success("更新成功");
    }

    @DeleteMapping("/products/{id}")
    public Result<?> deleteProduct(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success("删除成功");
    }

    @PutMapping("/products/{id}/status")
    public Result<?> toggleProductStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        productService.toggleStatus(id, body.get("status"));
        return Result.success();
    }

    // ========== 分类管理 ==========
    @GetMapping("/categories")
    public Result<?> listCategories() {
        return Result.success(categoryService.listAll());
    }

    @PostMapping("/categories")
    public Result<?> addCategory(@RequestBody Category category) {
        categoryService.save(category);
        return Result.success("添加成功", category);
    }

    @PutMapping("/categories/{id}")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        categoryService.updateById(category);
        return Result.success("更新成功");
    }

    @DeleteMapping("/categories/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.success("删除成功");
    }

    // ========== 订单管理 ==========
    @GetMapping("/orders")
    public Result<?> listOrders(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size,
                                @RequestParam(required = false) Integer status,
                                @RequestParam(required = false) String orderNo,
                                @RequestParam(required = false) String keyword) {
        return Result.success(orderService.adminListOrders(page, size, status, orderNo, keyword));
    }

    @GetMapping("/orders/{id}")
    public Result<?> getOrderDetail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id, null));
    }

    @PutMapping("/orders/{id}/ship")
    public Result<?> shipOrder(@PathVariable Long id) {
        orderService.adminShipOrder(id);
        return Result.success("发货成功");
    }

    @PutMapping("/orders/{id}/refund")
    public Result<?> refundOrder(@PathVariable Long id) {
        orderService.adminRefundOrder(id);
        return Result.success("退款成功");
    }

    // ========== 评论管理 ==========
    @GetMapping("/reviews")
    public Result<?> listReviews(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size,
                                 @RequestParam(required = false) Long productId) {
        return Result.success(reviewService.adminListReviews(page, size, productId));
    }

    @DeleteMapping("/reviews/{id}")
    public Result<?> deleteReview(@PathVariable Long id) {
        reviewService.removeById(id);
        return Result.success("删除成功");
    }

    @PutMapping("/reviews/{id}/status")
    public Result<?> toggleReviewStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        Review review = reviewService.getById(id);
        if (review != null) {
            review.setStatus(body.get("status"));
            reviewService.updateById(review);
        }
        return Result.success();
    }

    // ========== 公告管理 ==========
    @GetMapping("/announcements")
    public Result<?> listAnnouncements(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(announcementService.adminList(page, size));
    }

    @PostMapping("/announcements")
    public Result<?> addAnnouncement(@RequestBody Announcement announcement) {
        if (announcement.getStatus() == 1 && announcement.getPublishTime() == null) {
            announcement.setPublishTime(LocalDateTime.now());
        }
        announcementService.save(announcement);
        return Result.success("添加成功", announcement);
    }

    @PutMapping("/announcements/{id}")
    public Result<?> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        announcementService.updateById(announcement);
        return Result.success("更新成功");
    }

    @DeleteMapping("/announcements/{id}")
    public Result<?> deleteAnnouncement(@PathVariable Long id) {
        announcementService.removeById(id);
        return Result.success("删除成功");
    }

    // ========== 轮播图管理 ==========
    @GetMapping("/banners")
    public Result<?> listBanners() {
        return Result.success(bannerService.listAll());
    }

    @PostMapping("/banners")
    public Result<?> addBanner(@RequestBody Banner banner) {
        bannerService.save(banner);
        return Result.success("添加成功", banner);
    }

    @PutMapping("/banners/{id}")
    public Result<?> updateBanner(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id);
        bannerService.updateById(banner);
        return Result.success("更新成功");
    }

    @DeleteMapping("/banners/{id}")
    public Result<?> deleteBanner(@PathVariable Long id) {
        bannerService.removeById(id);
        return Result.success("删除成功");
    }

    // ========== 数据统计 ==========
    @GetMapping("/statistics")
    public Result<?> getStatistics() {
        return Result.success(orderService.getStatistics());
    }

    @GetMapping("/statistics/users")
    public Result<?> getUserStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalUsers", userService.count());
        return Result.success(stats);
    }

    @GetMapping("/statistics/products")
    public Result<?> getProductStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalProducts", productService.count());
        return Result.success(stats);
    }
}
