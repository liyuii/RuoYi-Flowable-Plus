package com.ruoyi.lims.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsInspection;
import com.ruoyi.lims.service.ILimsInspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Arrays;

@RequiredArgsConstructor
@RestController
@RequestMapping("/lims/inspection")
public class LimsInspectionController extends BaseController {

    private final ILimsInspectionService inspectionService;

    @GetMapping("/list")
    public TableDataInfo<LimsInspection> list(LimsInspection bo, PageQuery pageQuery) {
        return inspectionService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/{id}")
    public R<LimsInspection> getInfo(@PathVariable Long id) {
        return R.ok(inspectionService.queryById(id));
    }

    @PostMapping
    public R<Void> add(@Valid @RequestBody LimsInspection bo) {
        return toAjax(inspectionService.insert(bo));
    }

    @PutMapping
    public R<Void> edit(@Valid @RequestBody LimsInspection bo) {
        return toAjax(inspectionService.update(bo));
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(inspectionService.deleteWithValidByIds(Arrays.asList(ids)));
    }
}
