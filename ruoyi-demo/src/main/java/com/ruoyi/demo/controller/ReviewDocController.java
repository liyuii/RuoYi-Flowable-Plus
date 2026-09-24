package com.ruoyi.demo.controller;

import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.demo.domain.ReviewDoc;
import com.ruoyi.demo.domain.ReviewSpan;
import com.ruoyi.demo.domain.vo.ReviewContentVO;
import com.ruoyi.demo.service.IReviewDocService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * 人工审核Controller
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/demo/review")
public class ReviewDocController {

    private final IReviewDocService reviewDocService;

    @GetMapping("/doc/list")
    public TableDataInfo<ReviewDoc> list(ReviewDoc query, PageQuery pageQuery) {
        return reviewDocService.pageDocs(query, pageQuery);
    }

    @GetMapping("/doc/{docId}")
    public R<ReviewDoc> getInfo(@PathVariable Long docId) {
        return R.ok(reviewDocService.getDoc(docId));
    }

    @PostMapping("/doc")
    public R<Void> add(@RequestBody ReviewDoc doc) {
        reviewDocService.createDoc(doc);
        return R.ok();
    }

    @PutMapping("/doc")
    public R<Void> edit(@RequestBody ReviewDoc doc) {
        reviewDocService.updateDoc(doc);
        return R.ok();
    }

    @DeleteMapping("/doc/{docId}")
    public R<Void> remove(@PathVariable Long docId) {
        reviewDocService.deleteDoc(docId);
        return R.ok();
    }

    /**
     * 拆分章节：提取标题命中关键字的章节，生成本地截取文件
     */
    @PostMapping("/doc/{docId}/split")
    public R<ReviewDoc> split(@PathVariable Long docId) {
        return R.ok(reviewDocService.split(docId));
    }

    /**
     * 下载截取文件
     */
    @GetMapping("/doc/{docId}/extract/download")
    public void downloadExtract(@PathVariable Long docId, HttpServletResponse response) {
        reviewDocService.downloadExtract(docId, response);
    }

    @GetMapping("/doc/{docId}/content")
    public R<ReviewContentVO> content(@PathVariable Long docId) {
        return R.ok(reviewDocService.content(docId));
    }

    @PutMapping("/span/{id}/confirm")
    public R<Void> confirm(@PathVariable Long id) {
        return toR(reviewDocService.confirmSpan(id));
    }

    @PutMapping("/span/{id}/ignore")
    public R<Void> ignore(@PathVariable Long id) {
        return toR(reviewDocService.ignoreSpan(id));
    }

    @PostMapping("/span")
    public R<ReviewSpan> addSpan(@RequestBody ReviewSpan span) {
        return R.ok(reviewDocService.addSpan(span));
    }

    @PutMapping("/span")
    public R<ReviewSpan> updateSpan(@RequestBody ReviewSpan span) {
        return R.ok(reviewDocService.updateSpan(span));
    }

    @PostMapping("/doc/{docId}/apply")
    public R<String> apply(@PathVariable Long docId) {
        return R.ok("已生成脱敏文件: " + reviewDocService.applyMask(docId));
    }

    @PostMapping("/doc/{docId}/complete")
    public R<Void> complete(@PathVariable Long docId) {
        return toR(reviewDocService.completeReview(docId));
    }

    private R<Void> toR(Boolean flag) {
        return flag ? R.ok() : R.fail("操作失败");
    }

}
