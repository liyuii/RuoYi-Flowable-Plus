package com.ruoyi.lims.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsSpecification;
import com.ruoyi.lims.service.ILimsSpecificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Arrays;

@RequiredArgsConstructor
@RestController
@RequestMapping("/lims/specification")
public class LimsSpecificationController extends BaseController {

    private final ILimsSpecificationService specificationService;

    @GetMapping("/list")
    public TableDataInfo<LimsSpecification> list(LimsSpecification bo, PageQuery pageQuery) {
        return specificationService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/{id}")
    public R<LimsSpecification> getInfo(@PathVariable Long id) {
        return R.ok(specificationService.queryById(id));
    }

    @PostMapping
    public R<Void> add(@Valid @RequestBody LimsSpecification bo) {
        return toAjax(specificationService.insert(bo));
    }

    @PutMapping
    public R<Void> edit(@Valid @RequestBody LimsSpecification bo) {
        return toAjax(specificationService.update(bo));
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(specificationService.deleteWithValidByIds(Arrays.asList(ids)));
    }
}
