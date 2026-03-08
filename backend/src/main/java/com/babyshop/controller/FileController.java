package com.babyshop.controller;

import com.babyshop.common.Result;
import com.babyshop.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping
    public Result<?> upload(@RequestParam("file") MultipartFile file,
                            @RequestParam(value = "type", defaultValue = "images") String type) {
        String url = fileService.uploadFile(file, type);
        return Result.success("上传成功", url);
    }
}
