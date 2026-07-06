package com.ruoyi.web.controller.system;

import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.system.domain.SysFile;
import com.ruoyi.system.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/common/file")
public class SysFileController extends BaseController {

    private final ISysFileService fileService;

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

}
