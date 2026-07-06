package com.ruoyi.lims.listener;

import com.ruoyi.lims.domain.ContractApprove;
import com.ruoyi.lims.service.IContractApproveService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("contractEndListener")
public class ContractEndListener implements ExecutionListener {

    @Autowired
    private IContractApproveService contractService;

    @Override
    public void notify(DelegateExecution execution) {
        String businessKey = execution.getProcessInstanceBusinessKey();
        Long contractId = Long.valueOf(businessKey.replace("contract_", ""));
        ContractApprove contractApprove = contractService.queryById(contractId);
        String status = (String)execution.getVariable("status");

        if ("3".equals(status)) {
            contractApprove.setStatus("3");
        } else {
            contractApprove.setStatus("9");
        }
        contractService.update(contractApprove);
    }
}
