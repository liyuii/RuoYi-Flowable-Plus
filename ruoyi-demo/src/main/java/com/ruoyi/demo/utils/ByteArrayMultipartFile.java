package com.ruoyi.demo.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * 把内存里的字节数组包装成 MultipartFile，用于复用 sys_file 的上传逻辑
 * （页面图片是 Aspose 逐页渲染出来的，不需要先落本地文件）。
 */
public class ByteArrayMultipartFile implements MultipartFile {

    private final byte[] content;

    private final String fileName;

    private final String contentType;

    public ByteArrayMultipartFile(byte[] content, String fileName, String contentType) {
        this.content = content;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return "file";
    }

    @Override
    public String getOriginalFilename() {
        return fileName;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content == null || content.length == 0;
    }

    @Override
    public long getSize() {
        return isEmpty() ? 0 : content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        Files.write(dest.toPath(), content);
    }

}
