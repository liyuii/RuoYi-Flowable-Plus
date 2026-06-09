package com.ruoyi.lims.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsInspection;
import com.ruoyi.flowable.factory.FlowServiceFactory;
import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.lims.domain.LimsSpecItem;
import com.ruoyi.lims.domain.LimsTestItem;
import com.ruoyi.lims.service.ILimsInspectionService;
import com.ruoyi.lims.service.ILimsSpecItemService;
import com.ruoyi.lims.service.ILimsTestItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/lims/inspection")
public class LimsInspectionController extends BaseController {

    private final ILimsInspectionService inspectionService;
    private final ILimsSpecItemService specItemService;
    private final ILimsTestItemService testItemService;
    private final FlowServiceFactory flowServiceFactory;

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

    /**
     * 报检单提交
     */
    @PostMapping("/submit")
    public R<Void> submit(@RequestBody LimsInspection bo) {

        bo.setApplicant(LoginHelper.getUsername());
        bo.setStatus("1");
        inspectionService.insert(bo);

        LimsSpecItem specQuery = new LimsSpecItem();
        specQuery.setSpecId(bo.getSpecId());
        List<LimsSpecItem> specItems = specItemService.queryList(specQuery);
        for (LimsSpecItem si : specItems) {
            LimsTestItem ti = new LimsTestItem();
            ti.setInspectionId(bo.getId());
            ti.setItemName(si.getItemName());
            ti.setItemMethod(si.getItemMethod());
            ti.setSpecLower(si.getSpecLower());
            ti.setSpecUpper(si.getSpecUpper());
            ti.setUnit(si.getUnit());
            ti.setTestGroupId(si.getTestGroupId());
            ti.setTestGroupName(si.getTestGroupName());
            ti.setStatus("0");
            testItemService.insert(ti);
        }

        LimsTestItem itemQuery = new LimsTestItem();
        itemQuery.setInspectionId(bo.getId());
        List<LimsTestItem> testItems = testItemService.queryList(itemQuery);

        flowServiceFactory.getIdentityService().setAuthenticatedUserId(LoginHelper.getUserId().toString());

        Map<String, Object> vars = new HashMap<>();
        vars.put("initiator", LoginHelper.getUserId().toString());
        vars.put("testItems", testItems);
        flowServiceFactory.getRuntimeService().startProcessInstanceByKey(
            "lims_detection_process",
            "inspection_" + bo.getId(),
            vars
        );
        return R.ok("报检单提交成功");
    }
}
