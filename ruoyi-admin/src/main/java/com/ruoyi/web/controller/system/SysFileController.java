package com.ruoyi.web.controller.system;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.jwt.JWTUtil;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.system.domain.SysFile;
import com.ruoyi.system.service.ISysFileService;
import com.ruoyi.web.config.OnlyOfficeConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/common/file")
public class SysFileController extends BaseController {

    @Value("${ruoyi.profile}")
    private String profile;

    private final ISysFileService fileService;

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
        String relativePath = sysFile.getOssUrl().replace("/profile/", "");
        String fullPath = profile + File.separator + relativePath;
        File file = new File(fullPath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        byte[] fileBytes = FileUtil.readBytes(file);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(fileBytes));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
                .body(resource);
    }

}
