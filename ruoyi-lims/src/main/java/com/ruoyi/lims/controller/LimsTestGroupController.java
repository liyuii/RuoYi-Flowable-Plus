package com.ruoyi.lims.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsTestGroup;
import com.ruoyi.lims.service.ILimsTestGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Arrays;

@RequiredArgsConstructor
@RestController
@RequestMapping("/lims/testGroup")
public class LimsTestGroupController extends BaseController {

    private final ILimsTestGroupService testGroupService;

    @GetMapping("/list")
    public TableDataInfo<LimsTestGroup> list(LimsTestGroup bo, PageQuery pageQuery) {
        return testGroupService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/{id}")
    public R<LimsTestGroup> getInfo(@PathVariable Long id) {
        return R.ok(testGroupService.queryById(id));
    }

    @PostMapping
    public R<Void> add(@Valid @RequestBody LimsTestGroup bo) {
        return toAjax(testGroupService.insert(bo));
    }

    @PutMapping
    public R<Void> edit(@Valid @RequestBody LimsTestGroup bo) {
        return toAjax(testGroupService.update(bo));
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(testGroupService.deleteWithValidByIds(Arrays.asList(ids)));
    }
}
