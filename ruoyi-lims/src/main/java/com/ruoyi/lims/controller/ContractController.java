package com.ruoyi.lims.controller;

import cn.hutool.core.date.DateUtil;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.flowable.factory.FlowServiceFactory;
import com.ruoyi.lims.domain.ContractApprove;
import com.ruoyi.lims.service.IContractApproveService;
import com.ruoyi.lims.vo.ContractNotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.api.Task;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/lims/contract")
public class ContractController extends BaseController {
    private final IContractApproveService contractService;
    private final FlowServiceFactory flowServiceFactory;
    private final RabbitTemplate rabbitTemplate;

    @GetMapping("/list")
   public TableDataInfo<ContractApprove> list(ContractApprove bo, PageQuery pageQuery) {
        TableDataInfo<ContractApprove> page = contractService.queryPageList(bo, pageQuery);
        HistoryService historyService = flowServiceFactory.getHistoryService();
        for (ContractApprove contract : page.getRows()) {
            if (StringUtils.isNotBlank(contract.getStatus()) && !"0".equals(contract.getStatus())) {
                HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceBusinessKey("contract_" + contract.getId())
                    .singleResult();
                if (hpi != null) {
                    contract.setProcInstId(hpi.getId());
                }
            }
        }
        return page;
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

        // 异步部分：发送通知消息到 RabbitMQ
        TaskService taskService = flowServiceFactory.getTaskService();
        Task task = taskService.createTaskQuery()
            .processInstanceBusinessKey("contract_" + entity.getId())
            .singleResult();

//        ContractNotificationMessage msg = new ContractNotificationMessage();
//        msg.setContractId(entity.getId());
//        msg.setContractName(entity.getContractName());
//        msg.setApproverIds(queryApproverIds(task));  // 查询审批人列表
//        msg.setNotificationType("approve_submit");
//        msg.setSubmitterUserId(LoginHelper.getUserId());
//        msg.setSubmitTime(LocalDateTime.now().toString());
//        msg.setProcInsId(task.getProcessInstanceId());
//        msg.setTaskId(task.getId());
//
//        rabbitTemplate.convertAndSend(
//            "contract.notification.exchange",
//            "contract.notification.approve",
//            msg);
        log.info("合同审批流程启动成功，流程实例ID: {}", "contract_" + entity.getId());
        return R.ok("提交成功");
    }

    /**
     * 查询当前审批节点的审批人 ID 列表
     */
    private List<Long> queryApproverIds(Task task) {
        if (task == null) {
            return Collections.emptyList();
        }
        // 优先取 assignee
        if (StringUtils.isNotBlank(task.getAssignee())) {
            try {
                return Collections.singletonList(Long.parseLong(task.getAssignee()));
            } catch (NumberFormatException e) {
                log.warn("审批人 assignee 不是数字: {}", task.getAssignee());
            }
        }
        // 回退：取候选人
        TaskService taskService = flowServiceFactory.getTaskService();
        List<IdentityLink> links = taskService.getIdentityLinksForTask(task.getId());
        List<Long> userIds = new ArrayList<>();
        for (IdentityLink link : links) {
            if (link.getUserId() != null) {
                try { userIds.add(Long.parseLong(link.getUserId())); }
                catch (NumberFormatException ignored) {}
            }
        }
        return userIds;
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
