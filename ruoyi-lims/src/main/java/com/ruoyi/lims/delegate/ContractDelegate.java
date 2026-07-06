package com.ruoyi.lims.delegate;

import cn.hutool.core.util.StrUtil;
import com.ruoyi.flowable.factory.FlowServiceFactory;
import com.ruoyi.lims.domain.ContractApprove;
import com.ruoyi.lims.service.IContractApproveService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component("contractDelegate")
public class ContractDelegate extends FlowServiceFactory implements JavaDelegate {

    @Autowired
    private IContractApproveService contractService;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        String procId = delegateExecution.getProcessInstanceId();
        Map<String, Object> variables = new HashMap<>();
        //设置状态：审批中
        variables.put("status", "1");
        //设置合同类型
        String businessKey = delegateExecution.getProcessInstanceBusinessKey();
        if(StrUtil.isNotEmpty(businessKey)){
            String[] arrs = businessKey.split("_");
            String type = arrs[0];
            String bizId = arrs[1];
            if("contract".equals(type)){
                ContractApprove contractApprove = contractService.queryById(Long.valueOf(bizId));
                //根据合同类型设置审核人
                String contractType = contractApprove.getContractType();
                variables.put("contractType", contractType);
                //市场部合同
                if("1".equals(contractType)){
                    //获取组长
                    //获取区域BD
                    String teamLeader = getTeamLeader(contractType);
                    String areaLeader = getAreaLeader(contractType);
                    String topAuditUser = getTopAuditUser(contractType);
                    variables.put("teamLeader", teamLeader);
                    variables.put("areaLeader", areaLeader);
                    variables.put("topAuditUser", topAuditUser);
                }else if("2".equals(contractType)){
                    //研发部合同
                    //获取部门经理
                    String deptLeader = getDeptLeader(contractType);
                    String topAuditUser = getTopAuditUser(contractType);
                    variables.put("deptLeader", deptLeader);
                    variables.put("topAuditUser", topAuditUser);
                }else if ("3".equals(contractType)){
                    //财务部合同
                    //获取部门经理
                    String deptLeader = getDeptLeader(contractType);
                    variables.put("deptLeader", deptLeader);
                    String topAuditUser = getTopAuditUser(contractType);
                    variables.put("topAuditUser", topAuditUser);
                } else {
                    throw new RuntimeException("合同类型错误");
                }
            }
        }
        runtimeService.setVariables(procId, variables);
    }

    //获取组长
    private String getTeamLeader(String contractType){
        return "2073335016098197505";
    }

    //获取区域BD
    private String getAreaLeader(String contractType){
        return "2073334853640220674";
    }


    private String getDeptLeader(String contractType){
        return "111";
    }

    private String getTopAuditUser(String contractType){
        return "2073336866855809026";
    }
}
