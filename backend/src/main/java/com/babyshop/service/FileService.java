package com.babyshop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.babyshop.common.BusinessException;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileService {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.base-url}")
    private String baseUrl;

    public String uploadFile(MultipartFile file, String subDir) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";

        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        String dir = uploadPath + (subDir != null ? subDir + "/" : "");
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        try {
            file.transferTo(new File(dir + filename));
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }

        return baseUrl + (subDir != null ? subDir + "/" : "") + filename;
    }
}
