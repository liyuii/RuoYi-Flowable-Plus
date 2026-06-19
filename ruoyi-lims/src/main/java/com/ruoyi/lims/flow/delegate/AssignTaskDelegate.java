package com.ruoyi.lims.flow.delegate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.lims.domain.LimsTestGroupMember;
import com.ruoyi.lims.domain.LimsTestItem;
import com.ruoyi.lims.mapper.LimsTestGroupMemberMapper;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务分配 ServiceTask
 * 作用：将检测项目按检测组分发，设置 candidateUsers
 */
@RequiredArgsConstructor
@Component("assignTaskDelegate")
public class AssignTaskDelegate implements JavaDelegate {

    private final LimsTestGroupMemberMapper groupMemberMapper;

    @Override
    public void execute(DelegateExecution execution) {
        // 1. 获取流程变量
        List<LimsTestItem> testItems = (List<LimsTestItem>)
            execution.getVariable("testItems");

        if (testItems == null || testItems.isEmpty()) {
            return;
        }

        // 2. 遍历每个检测项目，按检测组分配候选人
        for (LimsTestItem item : testItems) {
            if (item.getTestGroupId() == null) {
                continue;
            }
            LambdaQueryWrapper<LimsTestGroupMember> qw = new LambdaQueryWrapper<>();
            qw.eq(LimsTestGroupMember::getGroupId, item.getTestGroupId());
            List<LimsTestGroupMember> members = groupMemberMapper.selectList(qw);

            // 3. 组内所有成员 userId 拼成 "3,5,7"
            String ids = members.stream()
                .map(m -> String.valueOf(m.getUserId()))
                .collect(Collectors.joining(","));
            item.setGroupMemberIds(ids);

            // 4. 可选：设置首位成员为 assignee
//            if (!members.isEmpty()) {
//                item.setAssignee(String.valueOf(members.get(0).getUserId()));
//            }
        }

        // 5. 更新流程变量
        execution.setVariable("testItems", testItems);
    }
}
