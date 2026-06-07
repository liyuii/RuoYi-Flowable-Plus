package com.ruoyi.lims.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsTestGroupMember;
import com.ruoyi.lims.service.ILimsTestGroupMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Arrays;

@RequiredArgsConstructor
@RestController
@RequestMapping("/lims/testGroupMember")
public class LimsTestGroupMemberController extends BaseController {

    private final ILimsTestGroupMemberService testGroupMemberService;

    @GetMapping("/list")
    public TableDataInfo<LimsTestGroupMember> list(LimsTestGroupMember bo, PageQuery pageQuery) {
        return testGroupMemberService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/{id}")
    public R<LimsTestGroupMember> getInfo(@PathVariable Long id) {
        return R.ok(testGroupMemberService.queryById(id));
    }

    @PostMapping
    public R<Void> add(@Valid @RequestBody LimsTestGroupMember bo) {
        return toAjax(testGroupMemberService.insert(bo));
    }

    @PutMapping
    public R<Void> edit(@Valid @RequestBody LimsTestGroupMember bo) {
        return toAjax(testGroupMemberService.update(bo));
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(testGroupMemberService.deleteWithValidByIds(Arrays.asList(ids)));
    }
}
