package com.ruoyi.lims.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.LimsTestItem;
import com.ruoyi.flowable.factory.FlowServiceFactory;
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
    private final FlowServiceFactory flowServiceFactory;

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

    /**
     * 获取当前多实例任务的 testItem
     */
    @GetMapping("/getByTask/{taskId}")
    public R<LimsTestItem> getByTask(@PathVariable String taskId) {
        org.flowable.task.api.Task task = flowServiceFactory.getTaskService()
            .createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return R.fail("任务不存在");
        }
        LimsTestItem item = (LimsTestItem) flowServiceFactory.getRuntimeService()
            .getVariable(task.getExecutionId(), "testItem");
        return R.ok(item);
    }
}