package com.ruoyi.doc.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.doc.domain.bo.DocInfoBo;
import com.ruoyi.doc.domain.vo.DocDetailVo;
import com.ruoyi.doc.domain.vo.DocInfoVo;
import com.ruoyi.doc.service.IDocInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * 文档管理Controller（文档库侧，模拟独立项目）
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/doc/info")
public class DocInfoController extends BaseController {

    private final IDocInfoService docInfoService;

    /**
     * 文档分页列表
     */
    @SaCheckPermission("doc:info:list")
    @GetMapping("/list")
    public TableDataInfo<DocInfoVo> list(DocInfoBo bo, PageQuery pageQuery) {
        return docInfoService.pageList(bo, pageQuery);
    }

    /**
     * 文档详情（文档信息 + 页面图片列表）
     */
    @SaCheckPermission("doc:info:query")
    @GetMapping("/{id}")
    public R<DocDetailVo> getInfo(@NotNull(message = "文档ID不能为空") @PathVariable Long id) {
        return R.ok(docInfoService.detail(id));
    }

    /**
     * 下载文档（整个 docx）
     */
    @SaCheckPermission("doc:info:download")
    @Log(title = "文档管理", businessType = BusinessType.OTHER)
    @GetMapping("/{id}/download")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        docInfoService.download(id, response);
    }

    /**
     * 删除文档（逻辑删除主表 + 删除页图片明细）
     */
    @SaCheckPermission("doc:info:remove")
    @Log(title = "文档管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        docInfoService.remove(id);
        return R.ok();
    }

}
