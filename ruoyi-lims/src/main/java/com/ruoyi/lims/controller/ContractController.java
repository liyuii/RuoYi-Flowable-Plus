package com.ruoyi.lims.controller;

import cn.hutool.core.date.DateUtil;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.flowable.factory.FlowServiceFactory;
import com.ruoyi.lims.domain.ContractApprove;
import com.ruoyi.lims.service.IContractApproveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/lims/contract")
public class ContractController extends BaseController {
    private final IContractApproveService contractService;
    private final FlowServiceFactory flowServiceFactory;

    @GetMapping("/list")
    public TableDataInfo<ContractApprove> list(ContractApprove bo, PageQuery pageQuery) {
        return contractService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/{id}")
    public R<ContractApprove> getInfo(@PathVariable Long id) {
        return R.ok(contractService.queryById(id));
    }

   @PostMapping
   public R<Void> add(@Valid @RequestBody ContractApprove bo) {
       bo.setContractNo("HT" + DateUtil.format(new Date(), "yyyyMMdd") + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        bo.setStatus("0"); bo.setDelFlag("0");
       return toAjax(contractService.insert(bo));
   }

    @PutMapping
    public R<Void> edit(@Valid @RequestBody ContractApprove bo) {
        return toAjax(contractService.update(bo));
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(contractService.deleteWithValidByIds(Arrays.asList(ids)));
    }


    @PostMapping("/submit")
    public R<Void> submit(@RequestBody ContractApprove bo){
        ContractApprove entity = contractService.queryById(bo.getId());
        if (entity == null) {
            return R.fail("合同不存在");
        }

        entity.setStatus("1");
        contractService.update(entity);
        //设置发起人
        flowServiceFactory.getIdentityService().setAuthenticatedUserId(LoginHelper.getUserId().toString());
        //启动流程
        RuntimeService runtimeService = flowServiceFactory.getRuntimeService();
        runtimeService.startProcessInstanceByKey("Process_1781834760920", "contract_" + entity.getId());
        log.info("合同审批流程启动成功，流程实例ID: {}", "contract_" + entity.getId());
        return R.ok("提交成功");
    }




//    @PostMapping("/submit")
//    public R<Void> submit(@RequestBody ContractApprove bo) {
//        ContractApprove entity = contractService.queryById(bo.getId());
//        if (entity == null) return R.fail("合同不存在");
//        entity.setStatus("1");
//        contractService.update(entity);
//        Map<String, Object> vars = new HashMap<>();
//        vars.put("amount", entity.getAmount());
//        vars.put("initiator", LoginHelper.getUserId().toString());
//        flowServiceFactory.getIdentityService().setAuthenticatedUserId(LoginHelper.getUserId().toString());
//        flowServiceFactory.getRuntimeService().startProcessInstanceByKey(
//            "contract_approve_process", "contract_" + entity.getId(), vars);
//        return R.ok("提交成功");
//    }
}
