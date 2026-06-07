package com.ruoyi.lims.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsSpecItem;
import com.ruoyi.lims.service.ILimsSpecItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Arrays;

@RequiredArgsConstructor
@RestController
@RequestMapping("/lims/specItem")
public class LimsSpecItemController extends BaseController {

    private final ILimsSpecItemService specItemService;

    @GetMapping("/list")
    public TableDataInfo<LimsSpecItem> list(LimsSpecItem bo, PageQuery pageQuery) {
        return specItemService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/{id}")
    public R<LimsSpecItem> getInfo(@PathVariable Long id) {
        return R.ok(specItemService.queryById(id));
    }

    @PostMapping
    public R<Void> add(@Valid @RequestBody LimsSpecItem bo) {
        return toAjax(specItemService.insert(bo));
    }

    @PutMapping
    public R<Void> edit(@Valid @RequestBody LimsSpecItem bo) {
        return toAjax(specItemService.update(bo));
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(specItemService.deleteWithValidByIds(Arrays.asList(ids)));
    }
}
