package com.ruoyi.demo.controller;

import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.demo.domain.DemoUploadFile;
import com.ruoyi.demo.domain.bo.DemoUploadCompleteBo;
import com.ruoyi.demo.domain.bo.DemoUploadInitBo;
import com.ruoyi.demo.domain.vo.DemoUploadSessionVo;
import com.ruoyi.demo.service.IDemoUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 文件管理上传Controller
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/demo/upload")
public class DemoUploadController {

    private final IDemoUploadService demoUploadService;

    @PostMapping("/init")
    public R<DemoUploadSessionVo> init(@Valid @RequestBody DemoUploadInitBo bo) {
        return R.ok(demoUploadService.initUpload(bo));
    }

    @GetMapping("/progress")
    public R<DemoUploadSessionVo> progress(@RequestParam("fileHash") String fileHash) {
        return R.ok(demoUploadService.getProgress(fileHash));
    }

    @PostMapping(value = "/chunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Void> chunk(@RequestParam("uploadId") String uploadId,
                         @RequestParam("chunkIndex") Integer chunkIndex,
                         @RequestPart("file") MultipartFile file) {
        demoUploadService.uploadChunk(uploadId, chunkIndex, file);
        return R.ok();
    }

    @PostMapping("/complete")
    public R<Long> complete(@Valid @RequestBody DemoUploadCompleteBo bo) {
        return R.ok(demoUploadService.completeUpload(bo));
    }

    @PostMapping("/cancel")
    public R<Void> cancel(@RequestParam("uploadId") String uploadId) {
        demoUploadService.cancelUpload(uploadId);
        return R.ok();
    }

    @GetMapping("/list")
    public TableDataInfo<DemoUploadFile> list(PageQuery pageQuery) {
        return demoUploadService.pageUploadFiles(pageQuery);
    }

    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        demoUploadService.downloadFile(id, response);
    }

    @DeleteMapping("/file/{id}")
    public R<Void> delete(@PathVariable Long id) {
        demoUploadService.deleteFile(id);
        return R.ok();
    }

}
