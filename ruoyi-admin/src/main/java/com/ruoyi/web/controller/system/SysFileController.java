package com.ruoyi.web.controller.system;

import cn.hutool.core.io.FileUtil;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.system.domain.SysFile;
import com.ruoyi.system.service.ISysFileService;
import com.ruoyi.oss.service.FileStorageStrategy;
import com.ruoyi.web.config.OnlyOfficeConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/common/file")
public class SysFileController extends BaseController {

    private final ISysFileService fileService;
    private final FileStorageStrategy storageStrategy;

    private final OnlyOfficeConfig onlyOfficeConfig;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Map<String, Object>> upload(@RequestPart("file") MultipartFile file, @RequestParam("biz_type") String bizType, @RequestParam("batch_id") String batchId) {
        if (ObjectUtil.isNull(file)) {
            return R.fail("上传文件不能为空");
        }
        SysFile sysFile = fileService.upload(file, bizType, batchId);
        Map<String, Object> map = new HashMap<>(4);
        map.put("id", sysFile.getId());
        map.put("fileName", sysFile.getFileName());
        map.put("fileSuffix", sysFile.getFileSuffix());
        map.put("fileSize", sysFile.getFileSize());
        map.put("ossUrl", sysFile.getOssUrl());
        map.put("createBy", sysFile.getCreateBy());
        map.put("createTime", sysFile.getCreateTime());
        return R.ok(map);
    }

    @DeleteMapping("/{fileId}")
    public R<Void> remove(@PathVariable Long fileId) {
        return toAjax(fileService.deleteById(fileId));
    }

    @GetMapping("/listByBatch/{batchId}")
    public R<List<SysFile>> listByBatch(@PathVariable String batchId) {
        return R.ok(fileService.listByBatch(batchId));
    }

    /**
     * 获取 OnlyOffice 编辑器配置
     */
    @GetMapping("/editorConfig/{fileId}")
    public R<Map<String, Object>> editorConfig(@PathVariable Long fileId) {
        SysFile sysFile = fileService.getById(fileId);
        if (sysFile == null) {
            return R.fail("文件不存在");
        }
        String suffix = sysFile.getFileSuffix().toLowerCase();
        if (!suffix.equals(".docx") && !suffix.equals(".doc")) {
            return R.fail("该文件类型不支持在线编辑");
        }

        String key = fileId + "_" + System.currentTimeMillis();
        String fileUrl = onlyOfficeConfig.getAppUrl() + "/common/file/onlyoffice/file/" + fileId;
        String callbackUrl = onlyOfficeConfig.getAppUrl() + "/common/file/onlyoffice/callback";

        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> doc = new HashMap<>();
        doc.put("url", fileUrl);
        doc.put("fileType", suffix.replace(".", ""));
        doc.put("key", key);
        doc.put("title", sysFile.getFileName());
        payload.put("document", doc);

        Map<String, Object> editorConfig = new HashMap<>();
        editorConfig.put("callbackUrl", callbackUrl);
        editorConfig.put("lang", "zh-CN");
        Map<String, Object> user = new HashMap<>();
        user.put("id", String.valueOf(LoginHelper.getUserId()));
        user.put("name", LoginHelper.getUsername());
        editorConfig.put("user", user);
        payload.put("editorConfig", editorConfig);

        String token = JWTUtil.createToken(payload, onlyOfficeConfig.getJwtSecret().getBytes());

        Map<String, Object> result = new HashMap<>();
        result.put("documentUrl", fileUrl);
        result.put("callbackUrl", callbackUrl);
        result.put("key", key);
        result.put("title", sysFile.getFileName());
        result.put("fileType", suffix.replace(".", ""));
        result.put("token", token);
        result.put("editorUrl", onlyOfficeConfig.getDocServerUrl() + "/web-apps/apps/api/documents/api.js");
        result.put("userId", String.valueOf(LoginHelper.getUserId()));
        result.put("userName", LoginHelper.getUsername());

        return R.ok(result);
    }

    /**
     * OnlyOffice 读取文件内容
     */
    @GetMapping("/onlyoffice/file/{fileId}")
    public ResponseEntity<Resource> fileDownload(@PathVariable Long fileId) {
        SysFile sysFile = fileService.getById(fileId);
        if (sysFile == null) {
            return ResponseEntity.notFound().build();
        }
        InputStreamResource resource = new InputStreamResource(storageStrategy.getContent(sysFile.getOssUrl()));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * 下载文件代理
     * 统一处理后端文件下载，适配本地和 OSS 两种存储模式
     */
    @GetMapping("/download/{fileId}")
    public void download(@PathVariable Long fileId, HttpServletResponse response) throws IOException {
        SysFile sysFile = fileService.getById(fileId);
        if (sysFile == null) {
            response.setStatus(404);
            return;
        }
        // 设置下载响应头：指定文件名
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        String encodedFileName = URLEncoder.encode(sysFile.getFileName(), StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName);
        // 通过策略读取文件内容并输出
        try (InputStream inputStream = storageStrategy.getContent(sysFile.getOssUrl())) {
            cn.hutool.core.io.IoUtil.copy(inputStream, response.getOutputStream());
        }
    }

    /**
     * 预览文件代理（kkfileview 使用）
     * 避免 kkfileview 直接拉取 OSS URL 导致的跨域/信任问题
     */
    @GetMapping("/preview/{fileId}")
    public void preview(@PathVariable Long fileId, HttpServletResponse response) throws IOException {
        SysFile sysFile = fileService.getById(fileId);
        if (sysFile == null) {
            response.setStatus(404);
            return;
        }
        // 根据文件后缀设置正确的 Content-Type
        response.setContentType(getContentType(sysFile.getFileSuffix()));
        // 通过策略读取文件内容并输出
        try (InputStream inputStream = storageStrategy.getContent(sysFile.getOssUrl())) {
            cn.hutool.core.io.IoUtil.copy(inputStream, response.getOutputStream());
        }
    }

    /**
     * 预览文件代理（kkfileview 使用）
     * fileName 参数只是为了给 kkfileview 提供文件后缀信息，不被实际使用
     */
    @GetMapping("/preview/{fileId}/{fileName}")
    public void preview(@PathVariable Long fileId, @PathVariable String fileName, HttpServletResponse response) throws IOException {
        SysFile sysFile = fileService.getById(fileId);
        if (sysFile == null) {
            response.setStatus(404);
            return;
        }
        response.setContentType(getContentType(sysFile.getFileSuffix()));
        try (InputStream inputStream = storageStrategy.getContent(sysFile.getOssUrl())) {
            cn.hutool.core.io.IoUtil.copy(inputStream, response.getOutputStream());
        }
    }

    /**
     * 根据文件后缀获取 Content-Type
     */
    private String getContentType(String suffix) {
        if (suffix == null) return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        switch (suffix.toLowerCase()) {
            case ".jpg": case ".jpeg": return "image/jpeg";
            case ".png": return "image/png";
            case ".gif": return "image/gif";
            case ".bmp": return "image/bmp";
            case ".pdf": return "application/pdf";
            case ".doc": return "application/msword";
            case ".docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case ".xls": return "application/vnd.ms-excel";
            case ".xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case ".ppt": return "application/vnd.ms-powerpoint";
            case ".pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case ".txt": return "text/plain;charset=UTF-8";
            case ".mp4": return "video/mp4";
            case ".avi": return "video/x-msvideo";
            case ".zip": return "application/zip";
            case ".rar": return "application/x-rar-compressed";
            default: return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

}

