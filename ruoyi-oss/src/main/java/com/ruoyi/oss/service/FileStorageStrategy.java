package com.ruoyi.oss.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface FileStorageStrategy {
    String upload(MultipartFile file, String bizType);
    InputStream getContent(String fileUrl);
    void delete(String fileUrl);
    void overwrite(String fileUrl, byte[] content);
    String getAccessUrl(String fileUrl);
}
