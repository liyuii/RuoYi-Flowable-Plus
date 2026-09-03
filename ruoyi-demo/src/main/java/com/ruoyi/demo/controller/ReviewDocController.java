package com.ruoyi.demo.controller;

import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.demo.domain.ReviewDoc;
import com.ruoyi.demo.domain.ReviewSpan;
import com.ruoyi.demo.domain.vo.ReviewContentVO;
import com.ruoyi.demo.service.IReviewDocService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 人工审核Controller
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/demo/review")
public class ReviewDocController {

    private final IReviewDocService reviewDocService;

    @GetMapping("/doc/list")
    public TableDataInfo<ReviewDoc> list(PageQuery pageQuery) {
        return reviewDocService.pageDocs(pageQuery);
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

    private R<Void> toR(Boolean flag) {
        return flag ? R.ok() : R.fail("操作失败");
    }

}
