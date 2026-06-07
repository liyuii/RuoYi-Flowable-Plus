package com.ruoyi.lims.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsTestItem;
import com.ruoyi.lims.service.ILimsTestItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Arrays;

@RequiredArgsConstructor
@RestController
@RequestMapping("/lims/testItem")
public class LimsTestItemController extends BaseController {

    private final ILimsTestItemService testItemService;

    @GetMapping("/list")
    public TableDataInfo<LimsTestItem> list(LimsTestItem bo, PageQuery pageQuery) {
        return testItemService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/{id}")
    public R<LimsTestItem> getInfo(@PathVariable Long id) {
        return R.ok(testItemService.queryById(id));
    }

    @PostMapping
    public R<Void> add(@Valid @RequestBody LimsTestItem bo) {
        return toAjax(testItemService.insert(bo));
    }

    @PutMapping
    public R<Void> edit(@Valid @RequestBody LimsTestItem bo) {
        return toAjax(testItemService.update(bo));
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(testItemService.deleteWithValidByIds(Arrays.asList(ids)));
    }
}
