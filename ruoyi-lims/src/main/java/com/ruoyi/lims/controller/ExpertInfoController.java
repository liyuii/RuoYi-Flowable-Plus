package com.ruoyi.lims.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.ExpertInfo;
import com.ruoyi.lims.service.IExpertInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * 评标专家库 Controller
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/lims/expertInfo")
public class ExpertInfoController extends BaseController {

    private final IExpertInfoService expertInfoService;

    @GetMapping("/list")
    public TableDataInfo<ExpertInfo> list(ExpertInfo bo, PageQuery pageQuery) {
        return expertInfoService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/{id}")
    public R<ExpertInfo> getInfo(@PathVariable Long id) {
        return R.ok(expertInfoService.queryById(id));
    }

    @PostMapping
    public R<Void> add(@RequestBody ExpertInfo bo) {
        return toAjax(expertInfoService.insert(bo));
    }

    @PutMapping
    public R<Void> edit(@RequestBody ExpertInfo bo) {
        return toAjax(expertInfoService.update(bo));
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(expertInfoService.deleteWithValidByIds(Arrays.asList(ids)));
    }
}
