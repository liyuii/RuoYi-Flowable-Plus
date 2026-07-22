package com.ruoyi.oss.service;

import cn.hutool.core.io.FileUtil;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地文件存储策略
 * 文件存到 ${ruoyi.profile}/bizType/年-月/日/uuid.suffix
 * URL 格式为 /profile/bizType/年-月/日/uuid.suffix
 */
@Service
@ConditionalOnProperty(prefix = "ruoyi", name = "file.storage", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageStrategy implements FileStorageStrategy {

    @Value("${ruoyi.profile}")
    private String profile;

    @Override
    public String upload(MultipartFile file, String bizType) {
        String originalName = file.getOriginalFilename();
        String suffix = StringUtils.substring(originalName,
            originalName.lastIndexOf("."), originalName.length());
        try {
            LocalDate today = LocalDate.now();
            String yearMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String day = String.format("%02d", today.getDayOfMonth());
            String uploadDir = profile + File.separator + bizType
                + File.separator + yearMonth + File.separator + day;
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String newFileName = UUID.randomUUID().toString() + suffix;
            File dest = new File(dir, newFileName);
            file.transferTo(dest);
            return "/profile/" + bizType + "/" + yearMonth + "/" + day + "/" + newFileName;
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream getContent(String fileUrl) {
        String relativePath = fileUrl.replace("/profile/", "");
        String fullPath = profile + File.separator + relativePath;
        try {
            return new FileInputStream(fullPath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("文件不存在: " + fileUrl, e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        String relativePath = fileUrl.replace("/profile/", "");
        String fullPath = profile + File.separator + relativePath;
        File file = new File(fullPath);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public void overwrite(String fileUrl, byte[] content) {
        String relativePath = fileUrl.replace("/profile/", "");
        String fullPath = profile + File.separator + relativePath;
        FileUtil.writeBytes(content, new File(fullPath));
    }

    @Override
    public String getAccessUrl(String fileUrl) {
        return fileUrl;
    }
}

