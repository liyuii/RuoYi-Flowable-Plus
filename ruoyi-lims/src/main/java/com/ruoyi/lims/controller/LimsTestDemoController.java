package com.ruoyi.lims.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsTestDemo;
import com.ruoyi.lims.service.ILimsTestDemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Arrays;

@RequiredArgsConstructor
@RestController
@RequestMapping("/lims/testDemo")
public class LimsTestDemoController extends BaseController {

    private final ILimsTestDemoService testDemoService;

    @GetMapping("/list")
    public TableDataInfo<LimsTestDemo> list(LimsTestDemo bo, PageQuery pageQuery) {
        return testDemoService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/{id}")
    public R<LimsTestDemo> getInfo(@PathVariable Long id) {
        return R.ok(testDemoService.queryById(id));
    }

    @PostMapping
    public R<Void> add(@Valid @RequestBody LimsTestDemo bo) {
        return toAjax(testDemoService.insert(bo));
    }

    @PutMapping
    public R<Void> edit(@Valid @RequestBody LimsTestDemo bo) {
        return toAjax(testDemoService.update(bo));
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(testDemoService.deleteWithValidByIds(Arrays.asList(ids)));
    }
}