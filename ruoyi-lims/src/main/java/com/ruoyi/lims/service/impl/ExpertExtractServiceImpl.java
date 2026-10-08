package com.ruoyi.lims.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.lims.constant.ExpertConstants;
import com.ruoyi.lims.domain.ExpertExtract;
import com.ruoyi.lims.domain.ExpertExtractAvoid;
import com.ruoyi.lims.domain.ExpertExtractResult;
import com.ruoyi.lims.domain.ExpertExtractScope;
import com.ruoyi.lims.domain.bo.ExpertExtractAuditBo;
import com.ruoyi.lims.domain.bo.ExpertExtractBo;
import com.ruoyi.lims.domain.bo.ExpertExtractResultBo;
import com.ruoyi.lims.domain.bo.ExpertMajorCountBo;
import com.ruoyi.lims.domain.vo.ExpertDrawCheckVo;
import com.ruoyi.lims.domain.vo.ExpertDrawResultVo;
import com.ruoyi.lims.domain.vo.ExpertExtractDetailVo;
import com.ruoyi.lims.domain.vo.ExpertExtractResultVo;
import com.ruoyi.lims.domain.vo.ExpertMajorCountVo;
import com.ruoyi.lims.mapper.ExpertExtractAvoidMapper;
import com.ruoyi.lims.mapper.ExpertExtractMapper;
import com.ruoyi.lims.mapper.ExpertExtractResultMapper;
import com.ruoyi.lims.mapper.ExpertExtractScopeMapper;
import com.ruoyi.lims.mapper.ExpertInfoMapper;
import com.ruoyi.lims.service.ExpertDrawService;
import com.ruoyi.lims.service.IExpertExtractService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 专家抽取申请 Service
 */
@RequiredArgsConstructor
@Service
public class ExpertExtractServiceImpl implements IExpertExtractService {

    private final ExpertExtractMapper baseMapper;

    private final ExpertExtractScopeMapper scopeMapper;

    private final ExpertExtractAvoidMapper avoidMapper;

    private final ExpertExtractResultMapper resultMapper;

    private final ExpertInfoMapper expertInfoMapper;

    private final ExpertDrawService drawService;

    @Override
    public TableDataInfo<ExpertExtract> queryPageList(ExpertExtract bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ExpertExtract> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotBlank(bo.getProjectName()), ExpertExtract::getProjectName, bo.getProjectName());
        lqw.eq(StringUtils.isNotBlank(bo.getRegionCode()), ExpertExtract::getRegionCode, bo.getRegionCode());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), ExpertExtract::getStatus, bo.getStatus());
        lqw.orderByDesc(ExpertExtract::getId);
        return TableDataInfo.build(baseMapper.selectPage(pageQuery.build(), lqw));
    }

    @Override
    public ExpertExtractDetailVo queryDetail(Long id) {
        ExpertExtract extract = baseMapper.selectById(id);
        if (extract == null) {
            throw new ServiceException("抽取申请不存在");
        }
        ExpertExtractDetailVo vo = new ExpertExtractDetailVo();
        BeanUtils.copyProperties(extract, vo);

        List<ExpertExtractScope> scopes = scopeMapper.selectScopeListByExtractId(id);
        List<ExpertExtractResultVo> results = resultMapper.selectResultListByExtractId(id);

        Map<Long, Long> validCount = results.stream()
            .filter(r -> isOccupy(r.getStatus()))
            .collect(Collectors.groupingBy(ExpertExtractResultVo::getScopeId, Collectors.counting()));
        for (ExpertExtractScope scope : scopes) {
            int valid = validCount.getOrDefault(scope.getId(), 0L).intValue();
            int need = scope.getNeedNum() == null ? 0 : scope.getNeedNum();
            scope.setValidNum(valid);
            scope.setShortNum(Math.max(0, need - valid));
        }

        vo.setScopes(scopes);
        vo.setAvoids(avoidMapper.selectList(new LambdaQueryWrapper<ExpertExtractAvoid>()
            .eq(ExpertExtractAvoid::getExtractId, id)
            .orderByAsc(ExpertExtractAvoid::getId)));
        vo.setResults(results);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insert(ExpertExtractBo bo) {
        validate(bo);
        ExpertExtract entity = new ExpertExtract();
        BeanUtils.copyProperties(bo, entity);
        entity.setId(null);
        // 保存即进入待审核，不需要用户再点一次提交
        entity.setStatus(ExpertConstants.EXTRACT_WAIT_AUDIT);
        entity.setApplyBy(LoginHelper.getUsername());
        entity.setApplyTime(new Date());
        entity.setAuditBy(null);
        entity.setAuditTime(null);
        entity.setAuditOpinion(null);
        int rows = baseMapper.insert(entity);
        saveChildren(entity.getId(), bo);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(ExpertExtractBo bo) {
        if (bo.getId() == null) {
            throw new ServiceException("申请ID不能为空");
        }
        ExpertExtract exist = baseMapper.selectById(bo.getId());
        if (exist == null) {
            throw new ServiceException("抽取申请不存在");
        }
        String status = exist.getStatus();
        // 待审核的申请不能改，避免审核的还是旧内容
        if (ExpertConstants.EXTRACT_WAIT_AUDIT.equals(status)) {
            throw new ServiceException("待审核的申请不能修改，请先审核");
        }

        // 已经抽过人的申请只允许调抽取范围
        if (ExpertConstants.EXTRACT_EXTRACTED.equals(status)
            || ExpertConstants.EXTRACT_WAIT_SUPPLEMENT.equals(status)) {
            updateScopesOnly(bo);
            refreshExtractStatus(bo.getId());
            return true;
        }

        validate(bo);
        ExpertExtract entity = new ExpertExtract();
        BeanUtils.copyProperties(bo, entity);
        // 状态和审核信息不能通过编辑接口改掉，置空让 MyBatis-Plus 跳过这几列
        entity.setStatus(null);
        entity.setApplyBy(null);
        entity.setApplyTime(null);
        entity.setAuditBy(null);
        entity.setAuditTime(null);
        entity.setAuditOpinion(null);
        int rows = baseMapper.updateById(entity);

        // 被驳回的申请改完自动重新进入待审核，并把上一轮的审核信息清掉
        if (ExpertConstants.EXTRACT_REJECTED.equals(exist.getStatus())) {
            baseMapper.update(null, new LambdaUpdateWrapper<ExpertExtract>()
                .eq(ExpertExtract::getId, bo.getId())
                .set(ExpertExtract::getStatus, ExpertConstants.EXTRACT_WAIT_AUDIT)
                .set(ExpertExtract::getApplyBy, LoginHelper.getUsername())
                .set(ExpertExtract::getApplyTime, new Date())
                .set(ExpertExtract::getAuditBy, null)
                .set(ExpertExtract::getAuditTime, null)
                .set(ExpertExtract::getAuditOpinion, null));
        }

        // 范围和回避名单整体重建，避免残留旧数据
        scopeMapper.delete(new LambdaQueryWrapper<ExpertExtractScope>()
            .eq(ExpertExtractScope::getExtractId, bo.getId()));
        avoidMapper.delete(new LambdaQueryWrapper<ExpertExtractAvoid>()
            .eq(ExpertExtractAvoid::getExtractId, bo.getId()));
        saveChildren(bo.getId(), bo);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        // 审核通过之后的申请不允许删除
        List<ExpertExtract> list = baseMapper.selectBatchIds(ids);
        for (ExpertExtract item : list) {
            if (!ExpertConstants.EXTRACT_DRAFT.equals(item.getStatus())
                && !ExpertConstants.EXTRACT_WAIT_AUDIT.equals(item.getStatus())
                && !ExpertConstants.EXTRACT_REJECTED.equals(item.getStatus())) {
                throw new ServiceException("审核通过的申请不能删除");
            }
        }
        scopeMapper.delete(new LambdaQueryWrapper<ExpertExtractScope>()
            .in(ExpertExtractScope::getExtractId, ids));
        avoidMapper.delete(new LambdaQueryWrapper<ExpertExtractAvoid>()
            .in(ExpertExtractAvoid::getExtractId, ids));
        resultMapper.delete(new LambdaQueryWrapper<ExpertExtractResult>()
            .in(ExpertExtractResult::getExtractId, ids));
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public Boolean submit(Long id) {
        ExpertExtract extract = baseMapper.selectById(id);
        if (extract == null) {
            throw new ServiceException("抽取申请不存在");
        }
        if (!ExpertConstants.EXTRACT_DRAFT.equals(extract.getStatus())
            && !ExpertConstants.EXTRACT_REJECTED.equals(extract.getStatus())) {
            throw new ServiceException("当前状态不能提交审核");
        }
        Long scopeCount = scopeMapper.selectCount(new LambdaQueryWrapper<ExpertExtractScope>()
            .eq(ExpertExtractScope::getExtractId, id));
        if (scopeCount == null || scopeCount == 0) {
            throw new ServiceException("请先填写抽取范围");
        }
        extract.setStatus(ExpertConstants.EXTRACT_WAIT_AUDIT);
        extract.setApplyBy(LoginHelper.getUsername());
        extract.setApplyTime(new Date());
        return baseMapper.updateById(extract) > 0;
    }

    @Override
    public Boolean audit(ExpertExtractAuditBo bo) {
        if (bo.getId() == null) {
            throw new ServiceException("申请ID不能为空");
        }
        ExpertExtract extract = baseMapper.selectById(bo.getId());
        if (extract == null) {
            throw new ServiceException("抽取申请不存在");
        }
        if (!ExpertConstants.EXTRACT_WAIT_AUDIT.equals(extract.getStatus())) {
            throw new ServiceException("当前状态不能审核");
        }
        extract.setAuditBy(LoginHelper.getUsername());
        extract.setAuditTime(new Date());
        extract.setAuditOpinion(bo.getOpinion());
        extract.setStatus(Boolean.TRUE.equals(bo.getPass())
            ? ExpertConstants.EXTRACT_WAIT_EXTRACT : ExpertConstants.EXTRACT_REJECTED);
        return baseMapper.updateById(extract) > 0;
    }

    @Override
    public List<ExpertDrawCheckVo> check(Long id) {
        ExpertExtract extract = baseMapper.selectById(id);
        if (extract == null) {
            throw new ServiceException("抽取申请不存在");
        }
        List<ExpertExtractScope> scopes = scopeMapper.selectScopeListByExtractId(id);
        if (CollUtil.isEmpty(scopes)) {
            throw new ServiceException("请先填写抽取范围");
        }
        // 预检的口径和真正抽取保持一致：
        //   1) 已经抽到人的申请只检查还缺几个人，抽满的范围直接跳过；
        //   2) 本次申请出现过的人（含不参加和请假的）要排掉，否则算出来的可用人数会比实际能补的人多。
        List<ExpertExtractResult> allResults = listResults(id);
        Map<Long, Long> validCount = countValid(allResults);
        List<ScopeRequire> requires = new ArrayList<>();
        for (ExpertExtractScope scope : scopes) {
            long valid = validCount.getOrDefault(scope.getId(), 0L);
            int need = scope.getNeedNum() == null ? 0 : scope.getNeedNum();
            int shortNum = (int) Math.max(0, need - valid);
            if (shortNum > 0) {
                requires.add(new ScopeRequire(scope.getId(), scope.getMajorCode(), scope.getMajorName(), shortNum));
            }
        }
        if (CollUtil.isEmpty(requires)) {
            return new ArrayList<>();
        }
        Set<Long> excludeIds = allResults.stream()
            .map(ExpertExtractResult::getExpertId)
            .collect(Collectors.toSet());
        return buildPlan(extract, requires, excludeIds).getChecks();
    }

    @Override
    public List<ExpertMajorCountVo> queryMajorCount(ExpertMajorCountBo bo) {
        if (StringUtils.isBlank(bo.getRegionCode()) || bo.getEvaluateTime() == null) {
            return new ArrayList<>();
        }
        return expertInfoMapper.selectMajorCountList(
            bo.getRegionCode(),
            DateUtil.beginOfDay(bo.getEvaluateTime()),
            bo.getExpertKeys(),
            bo.getCompanies());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExpertDrawResultVo draw(Long id) {
        ExpertExtract extract = baseMapper.selectById(id);
        if (extract == null) {
            throw new ServiceException("抽取申请不存在");
        }
        if (!ExpertConstants.EXTRACT_WAIT_EXTRACT.equals(extract.getStatus())) {
            throw new ServiceException("当前状态不能执行抽取");
        }
        List<ExpertExtractScope> scopes = scopeMapper.selectScopeListByExtractId(id);
        if (CollUtil.isEmpty(scopes)) {
            throw new ServiceException("请先填写抽取范围");
        }

        DrawPlan plan = buildPlan(extract, toRequires(scopes), new HashSet<>());
        if (!plan.isPass()) {
            // 没抽成，不写任何数据，把每条范围差几个人返回给前端
            return failResult(plan);
        }

        saveResults(extract, plan,
            DateUtil.beginOfDay(extract.getEvaluateTime()),
            1,
            ExpertConstants.DRAW_TYPE_FIRST);

        extract.setStatus(ExpertConstants.EXTRACT_EXTRACTED);
        baseMapper.updateById(extract);
        return successResult(id, plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExpertDrawResultVo supplement(Long id) {
        ExpertExtract extract = baseMapper.selectById(id);
        if (extract == null) {
            throw new ServiceException("抽取申请不存在");
        }
        if (!ExpertConstants.EXTRACT_WAIT_SUPPLEMENT.equals(extract.getStatus())) {
            throw new ServiceException("当前状态不能补充抽取");
        }
        List<ExpertExtractScope> scopes = scopeMapper.selectScopeListByExtractId(id);
        List<ExpertExtractResult> allResults = listResults(id);
        Map<Long, Long> validCount = countValid(allResults);

        // 只补缺人的范围，缺几个补几个
        List<ScopeRequire> requires = new ArrayList<>();
        for (ExpertExtractScope scope : scopes) {
            long valid = validCount.getOrDefault(scope.getId(), 0L);
            int need = scope.getNeedNum() == null ? 0 : scope.getNeedNum();
            int shortNum = (int) Math.max(0, need - valid);
            if (shortNum > 0) {
                requires.add(new ScopeRequire(scope.getId(), scope.getMajorCode(), scope.getMajorName(), shortNum));
            }
        }
        if (CollUtil.isEmpty(requires)) {
            throw new ServiceException("当前没有缺额，不需要补充抽取");
        }

        // 补抽要排除本次申请里出现过的所有人，包括已经确认不参加和请假的，
        // 否则占用已经释放了，会把刚拒绝的人再抽回来
        Set<Long> excludeIds = allResults.stream()
            .map(ExpertExtractResult::getExpertId)
            .collect(Collectors.toSet());

        DrawPlan plan = buildPlan(extract, requires, excludeIds);
        if (!plan.isPass()) {
            return failResult(plan);
        }

        int maxRound = allResults.stream()
            .mapToInt(r -> r.getRoundNo() == null ? 0 : r.getRoundNo())
            .max().orElse(0);
        saveResults(extract, plan,
            DateUtil.beginOfDay(extract.getEvaluateTime()),
            maxRound + 1,
            ExpertConstants.DRAW_TYPE_SUPPLEMENT);

        // 补完之后重新算一次状态：补满回到已抽取，还有缺额就保持补充抽取
        refreshExtractStatus(id);
        return successResult(id, plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateResultStatus(ExpertExtractResultBo bo) {
        if (bo.getId() == null) {
            throw new ServiceException("结果ID不能为空");
        }
        String target = bo.getStatus();
        if (!ExpertConstants.RESULT_JOIN.equals(target)
            && !ExpertConstants.RESULT_QUIT.equals(target)
            && !ExpertConstants.RESULT_LEAVE.equals(target)) {
            throw new ServiceException("要改成的状态不合法");
        }
        if (ExpertConstants.RESULT_LEAVE.equals(target) && StringUtils.isBlank(bo.getReason())) {
            throw new ServiceException("请假必须填写理由");
        }
        ExpertExtractResult result = resultMapper.selectById(bo.getId());
        if (result == null) {
            throw new ServiceException("抽取结果不存在");
        }
        result.setStatus(target);
        result.setReason(bo.getReason());
        result.setStatusBy(LoginHelper.getUsername());
        result.setStatusTime(new Date());
        int rows = resultMapper.updateById(result);

        // 有人不参加或者请假就会产生缺额，这里把申请状态同步过去
        refreshExtractStatus(result.getExtractId());
        return rows > 0;
    }

    // ------------------------------------------------------------------
    // 以下是内部方法
    // ------------------------------------------------------------------

    /**
     * 按范围顺序构建抽取计划。
     * 每条范围先查候选，再按需要的数量随机取人，取到的人会加进排除集合，
     * 后面范围查候选时会把他们排掉，这样保证一个人只属于一条范围。
     * 只要有一条范围不够，pass 就是 false，调用方不落库。
     */
    private DrawPlan buildPlan(ExpertExtract extract, List<ScopeRequire> requires, Set<Long> baseExclude) {
        Date evaluateDate = DateUtil.beginOfDay(extract.getEvaluateTime());
        Set<Long> selected = new HashSet<>(baseExclude);
        DrawPlan plan = new DrawPlan();

        for (ScopeRequire require : requires) {
            List<Long> candidates = expertInfoMapper.selectCandidateIds(
                require.getMajorCode(), extract.getRegionCode(), evaluateDate,
                extract.getId(), selected);

            int available = candidates.size();
            int need = require.getNeedNum() == null ? 0 : require.getNeedNum();

            ExpertDrawCheckVo check = new ExpertDrawCheckVo();
            check.setScopeId(require.getScopeId());
            check.setMajorCode(require.getMajorCode());
            check.setMajorName(require.getMajorName());
            check.setNeedNum(need);
            check.setAvailableNum(available);
            check.setShortNum(Math.max(0, need - available));
            plan.getChecks().add(check);

            List<Long> picked = drawService.randomPick(candidates, Math.min(need, available));
            selected.addAll(picked);
            plan.getPicks().add(new ScopePick(require.getScopeId(), picked));
        }
        plan.setPass(plan.getChecks().stream().allMatch(c -> c.getShortNum() == 0));
        return plan;
    }

    private void saveResults(ExpertExtract extract, DrawPlan plan, Date evaluateDate,
                             int roundNo, String drawType) {
        String operator = LoginHelper.getUsername();
        Date now = new Date();
        for (ScopePick pick : plan.getPicks()) {
            for (Long expertId : pick.getExpertIds()) {
                ExpertExtractResult row = new ExpertExtractResult();
                row.setExtractId(extract.getId());
                row.setScopeId(pick.getScopeId());
                row.setExpertId(expertId);
                row.setEvaluateDate(evaluateDate);
                row.setStatus(ExpertConstants.RESULT_WAIT_CONFIRM);
                row.setRoundNo(roundNo);
                row.setExtractType(drawType);
                row.setExtractBy(operator);
                row.setExtractTime(now);
                row.setCreateTime(now);
                row.setUpdateTime(now);
                resultMapper.insert(row);
            }
        }
    }

    private ExpertDrawResultVo successResult(Long extractId, DrawPlan plan) {
        ExpertDrawResultVo vo = new ExpertDrawResultVo();
        vo.setSuccess(Boolean.TRUE);
        vo.setChecks(plan.getChecks());
        vo.setResults(resultMapper.selectResultListByExtractId(extractId));
        return vo;
    }

    private ExpertDrawResultVo failResult(DrawPlan plan) {
        ExpertDrawResultVo vo = new ExpertDrawResultVo();
        vo.setSuccess(Boolean.FALSE);
        vo.setChecks(plan.getChecks());
        vo.setMessage("候选专家人数不足，请查看每条范围的可用人数");
        return vo;
    }

    /**
     * 重新统计申请状态。
     * 有缺额就是补充抽取，没缺额就是已抽取。
     */
    private void refreshExtractStatus(Long extractId) {
        ExpertExtract extract = baseMapper.selectById(extractId);
        if (extract == null) {
            return;
        }
        if (!ExpertConstants.EXTRACT_EXTRACTED.equals(extract.getStatus())
            && !ExpertConstants.EXTRACT_WAIT_SUPPLEMENT.equals(extract.getStatus())) {
            return;
        }
        List<ExpertExtractScope> scopes = scopeMapper.selectList(
            new LambdaQueryWrapper<ExpertExtractScope>().eq(ExpertExtractScope::getExtractId, extractId));
        Map<Long, Long> validCount = countValid(listResults(extractId));
        boolean shortage = scopes.stream().anyMatch(scope -> {
            long valid = validCount.getOrDefault(scope.getId(), 0L);
            int need = scope.getNeedNum() == null ? 0 : scope.getNeedNum();
            return valid < need;
        });
        String target = shortage
            ? ExpertConstants.EXTRACT_WAIT_SUPPLEMENT : ExpertConstants.EXTRACT_EXTRACTED;
        if (!target.equals(extract.getStatus())) {
            extract.setStatus(target);
            baseMapper.updateById(extract);
        }
    }

    private List<ExpertExtractResult> listResults(Long extractId) {
        return resultMapper.selectList(new LambdaQueryWrapper<ExpertExtractResult>()
            .eq(ExpertExtractResult::getExtractId, extractId));
    }

    private Map<Long, Long> countValid(List<ExpertExtractResult> results) {
        return results.stream()
            .filter(r -> isOccupy(r.getStatus()))
            .collect(Collectors.groupingBy(ExpertExtractResult::getScopeId, Collectors.counting()));
    }

    private boolean isOccupy(String status) {
        return ExpertConstants.RESULT_WAIT_CONFIRM.equals(status)
            || ExpertConstants.RESULT_JOIN.equals(status);
    }

    private List<ScopeRequire> toRequires(List<ExpertExtractScope> scopes) {
        return scopes.stream()
            .map(s -> new ScopeRequire(s.getId(), s.getMajorCode(), s.getMajorName(), s.getNeedNum()))
            .collect(Collectors.toList());
    }

    private void saveChildren(Long extractId, ExpertExtractBo bo) {
        if (CollUtil.isNotEmpty(bo.getScopes())) {
            int order = 1;
            for (ExpertExtractScope scope : bo.getScopes()) {
                scope.setId(null);
                scope.setExtractId(extractId);
                scope.setOrderNum(order++);
                scopeMapper.insert(scope);
            }
        }
        if (CollUtil.isNotEmpty(bo.getAvoids())) {
            for (ExpertExtractAvoid avoid : bo.getAvoids()) {
                if (ExpertConstants.AVOID_TYPE_EXPERT.equals(avoid.getAvoidType())) {
                    if (StringUtils.isBlank(avoid.getExpertName()) || StringUtils.isBlank(avoid.getIdCard())) {
                        continue;
                    }
                    avoid.setCompanyName(null);
                } else if (ExpertConstants.AVOID_TYPE_COMPANY.equals(avoid.getAvoidType())) {
                    if (StringUtils.isBlank(avoid.getCompanyName())) {
                        continue;
                    }
                    avoid.setExpertName(null);
                    avoid.setIdCard(null);
                } else {
                    continue;
                }
                avoid.setId(null);
                avoid.setExtractId(extractId);
                avoidMapper.insert(avoid);
            }
        }
    }

    /**
     * 已经抽过人的申请只允许调整抽取范围。
     * 允许：改数量、新增范围。
     * 不允许：删除范围、改已有范围的专业、改其他条件。
     * 数量不能小于该范围当前的有效人数，要减人请先在结果里把专家改成不参加。
     */
    private void updateScopesOnly(ExpertExtractBo bo) {
        List<ExpertExtractScope> existing = scopeMapper.selectScopeListByExtractId(bo.getId());
        Map<Long, ExpertExtractScope> existingMap = existing.stream()
            .collect(Collectors.toMap(ExpertExtractScope::getId, s -> s));
        Map<Long, Long> validCount = countValid(listResults(bo.getId()));

        Set<Long> keptIds = new HashSet<>();
        int order = 0;
        for (ExpertExtractScope scope : bo.getScopes()) {
            order++;
            if (scope.getNeedNum() == null || scope.getNeedNum() < 0) {
                throw new ServiceException("抽取范围的人数不能小于 0");
            }
            if (scope.getId() == null) {
                if (StringUtils.isBlank(scope.getMajorCode())) {
                    throw new ServiceException("新增范围的专业不能为空");
                }
                if (scope.getNeedNum() <= 0) {
                    throw new ServiceException("新增范围的人数必须大于 0");
                }
                ExpertExtractScope add = new ExpertExtractScope();
                add.setExtractId(bo.getId());
                add.setMajorCode(scope.getMajorCode());
                add.setNeedNum(scope.getNeedNum());
                add.setOrderNum(order);
                scopeMapper.insert(add);
                continue;
            }

            ExpertExtractScope old = existingMap.get(scope.getId());
            if (old == null) {
                throw new ServiceException("要修改的抽取范围不存在");
            }
            if (!old.getMajorCode().equals(scope.getMajorCode())) {
                throw new ServiceException("已经抽取过的申请不能修改范围的专业，"
                    + "想换专业请新增一条范围，并把原范围的人数改成 0");
            }
            long valid = validCount.getOrDefault(scope.getId(), 0L);
            if (scope.getNeedNum() < valid) {
                throw new ServiceException("范围 [" + old.getMajorName() + "] 已经抽到 " + valid
                    + " 人，人数不能小于这个数，请先在结果里把多余的专家改成不参加");
            }
            ExpertExtractScope update = new ExpertExtractScope();
            update.setId(scope.getId());
            update.setNeedNum(scope.getNeedNum());
            update.setOrderNum(order);
            scopeMapper.updateById(update);
            keptIds.add(scope.getId());
        }

        for (ExpertExtractScope old : existing) {
            if (!keptIds.contains(old.getId())) {
                throw new ServiceException("已经抽取过的申请不能删除抽取范围，可以把人数改成 0");
            }
        }

        // 主表只留一条修改痕迹
        baseMapper.update(null, new LambdaUpdateWrapper<ExpertExtract>()
            .eq(ExpertExtract::getId, bo.getId())
            .set(ExpertExtract::getUpdateBy, LoginHelper.getUsername())
            .set(ExpertExtract::getUpdateTime, new Date()));
    }

    private void validate(ExpertExtractBo bo) {
        if (StringUtils.isBlank(bo.getProjectName())) {
            throw new ServiceException("项目名称不能为空");
        }
        if (StringUtils.isBlank(bo.getRegionCode())) {
            throw new ServiceException("抽取地区不能为空");
        }
        if (bo.getEvaluateTime() == null) {
            throw new ServiceException("评标时间不能为空");
        }
        if (CollUtil.isEmpty(bo.getScopes())) {
            throw new ServiceException("请至少填写一条抽取范围");
        }
        for (ExpertExtractScope scope : bo.getScopes()) {
            if (StringUtils.isBlank(scope.getMajorCode())) {
                throw new ServiceException("抽取范围的专业不能为空");
            }
            if (scope.getNeedNum() == null || scope.getNeedNum() <= 0) {
                throw new ServiceException("抽取范围的人数必须大于 0");
            }
        }
    }

    /** 一条范围需要抽几个人 */
    @Data
    @AllArgsConstructor
    private static class ScopeRequire {
        private Long scopeId;
        private String majorCode;
        private String majorName;
        private Integer needNum;
    }

    /** 一条范围实际抽到的人 */
    @Data
    @AllArgsConstructor
    private static class ScopePick {
        private Long scopeId;
        private List<Long> expertIds;
    }

    /** 一次抽取的完整计划 */
    @Data
    private static class DrawPlan {
        private boolean pass;
        private List<ExpertDrawCheckVo> checks = new ArrayList<>();
        private List<ScopePick> picks = new ArrayList<>();
    }
}
