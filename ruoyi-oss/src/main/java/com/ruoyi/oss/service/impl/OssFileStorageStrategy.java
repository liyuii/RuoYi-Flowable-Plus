package com.ruoyi.oss.service;

import com.ruoyi.oss.core.OssClient;
import com.ruoyi.oss.entity.UploadResult;
import com.ruoyi.oss.enumd.AccessPolicyType;
import com.ruoyi.oss.factory.OssFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * OSS 文件存储策略
 * 文件存到 OSS 服务器（MinIO / 阿里云 OSS 等），通过 S3 协议操作
 * URL 格式由 OssClient 决定，通常为 http(s)://bucket.endpoint/prefix/日期/uuid.suffix
 */
@Service
@ConditionalOnProperty(prefix = "ruoyi", name = "file.storage", havingValue = "oss")
public class OssFileStorageStrategy implements FileStorageStrategy {

    @Override
    public String upload(MultipartFile file, String bizType) {
        String originalName = file.getOriginalFilename();
        String suffix = originalName.substring(originalName.lastIndexOf("."));
        OssClient client = OssFactory.instance();
        try {
            UploadResult result = client.uploadSuffix(
                file.getBytes(), suffix, file.getContentType());
            return result.getUrl();
        } catch (IOException e) {
            throw new RuntimeException("OSS 上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream getContent(String fileUrl) {
        OssClient client = OssFactory.instance();
        return client.getObjectContent(fileUrl);
    }

    @Override
    public void delete(String fileUrl) {
        OssClient client = OssFactory.instance();
        client.delete(fileUrl);
    }

    @Override
    public void overwrite(String fileUrl, byte[] content) {
        OssClient client = OssFactory.instance();
        String baseUrl = client.getUrl();
        String objectKey = fileUrl.substring(baseUrl.length() + 1);
        client.upload(content, objectKey, "application/octet-stream");
    }

    @Override
    public String getAccessUrl(String fileUrl) {
        OssClient client = OssFactory.instance();
        if (client.getAccessPolicy() == AccessPolicyType.PRIVATE) {
            String baseUrl = client.getUrl();
            String objectKey = fileUrl.substring(baseUrl.length() + 1);
            return client.getPrivateUrl(objectKey, 3600);
        }
        return fileUrl;
    }
}

