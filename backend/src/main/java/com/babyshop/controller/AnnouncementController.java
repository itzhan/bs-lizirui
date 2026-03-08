package com.babyshop.controller;

import com.babyshop.common.Result;
import com.babyshop.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public Result<?> listPublished(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(announcementService.listPublished(page, size));
    }

    @GetMapping("/{id}")
    public Result<?> getAnnouncement(@PathVariable Long id) {
        return Result.success(announcementService.getById(id));
    }
}
