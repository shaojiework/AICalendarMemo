package com.aicalendar.controller;

import com.aicalendar.dto.response.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.access-path:/upload/}")
    private String accessPath;

    /**
     * 允许的业务类型
     */
    private static final List<String> ALLOWED_TYPES = Arrays.asList("avatar", "memorial", "schedule", "profile");

    /**
     * 文件上传接口
     * @param file 上传的文件
     * @param type 业务类型：avatar(头像)/memorial(纪念日)/schedule(日程)/profile(用户)
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "avatar") String type) {
        
        if (file.isEmpty()) {
            return Result.error(400, "请选择要上传的文件");
        }

        // 验证业务类型
        if (!ALLOWED_TYPES.contains(type)) {
            return Result.error(400, "不支持的业务类型：" + type);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isValidImage(originalFilename)) {
            return Result.error(400, "只支持图片格式（jpg, jpeg, png, gif）");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + extension;

        try {
            // 获取项目运行时的绝对路径
            String basePath = System.getProperty("user.dir");
            // 创建业务类型子目录
            Path uploadPath = Paths.get(basePath, uploadDir, type);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(newFilename);
            file.transferTo(filePath.toFile());

            // 访问路径包含业务类型目录
            String accessUrl = accessPath + type + "/" + newFilename;
            return Result.success("上传成功", accessUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(500, "文件上传失败");
        }
    }

    private boolean isValidImage(String filename) {
        String lowerFilename = filename.toLowerCase();
        return lowerFilename.endsWith(".jpg") || 
               lowerFilename.endsWith(".jpeg") || 
               lowerFilename.endsWith(".png") || 
               lowerFilename.endsWith(".gif");
    }
}