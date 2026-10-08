package com.ruoyi.lims.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.lims.domain.ExpertExtract;
import com.ruoyi.lims.domain.bo.ExpertExtractAuditBo;
import com.ruoyi.lims.domain.bo.ExpertExtractBo;
import com.ruoyi.lims.domain.bo.ExpertExtractResultBo;
import com.ruoyi.lims.domain.bo.ExpertMajorCountBo;
import com.ruoyi.lims.domain.vo.ExpertDrawCheckVo;
import com.ruoyi.lims.domain.vo.ExpertDrawResultVo;
import com.ruoyi.lims.domain.vo.ExpertExtractDetailVo;
import com.ruoyi.lims.domain.vo.ExpertMajorCountVo;
import com.ruoyi.lims.service.IExpertExtractService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 专家抽取申请 Controller
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/lims/expertExtract")
public class ExpertExtractController extends BaseController {

    private final IExpertExtractService expertExtractService;

    @GetMapping("/list")
    public TableDataInfo<ExpertExtract> list(ExpertExtract bo, PageQuery pageQuery) {
        return expertExtractService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/{id}")
    public R<ExpertExtractDetailVo> getInfo(@PathVariable Long id) {
        return R.ok(expertExtractService.queryDetail(id));
    }

    @PostMapping
    public R<Void> add(@RequestBody ExpertExtractBo bo) {
        return toAjax(expertExtractService.insert(bo));
    }

    @PutMapping
    public R<Void> edit(@RequestBody ExpertExtractBo bo) {
        return toAjax(expertExtractService.update(bo));
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(expertExtractService.deleteWithValidByIds(Arrays.asList(ids)));
    }

    /** 提交审核 */
    @PostMapping("/submit/{id}")
    public R<Void> submit(@PathVariable Long id) {
        return toAjax(expertExtractService.submit(id));
    }

    /** 审核 */
    @PostMapping("/audit")
    public R<Void> audit(@RequestBody ExpertExtractAuditBo bo) {
        return toAjax(expertExtractService.audit(bo));
    }

    /** 预检：只看每条范围够不够，不落库 */
    @GetMapping("/check/{id}")
    public R<List<ExpertDrawCheckVo>> check(@PathVariable Long id) {
        return R.ok(expertExtractService.check(id));
    }

    /** 统计各专业当前可抽取的人数，给选择专业的下拉用 */
    @PostMapping("/majorCount")
    public R<List<ExpertMajorCountVo>> majorCount(@RequestBody ExpertMajorCountBo bo) {
        return R.ok(expertExtractService.queryMajorCount(bo));
    }

    /** 首次抽取 */
    @PostMapping("/draw/{id}")
    public R<ExpertDrawResultVo> draw(@PathVariable Long id) {
        return R.ok(expertExtractService.draw(id));
    }

    /** 补充抽取 */
    @PostMapping("/supplement/{id}")
    public R<ExpertDrawResultVo> supplement(@PathVariable Long id) {
        return R.ok(expertExtractService.supplement(id));
    }

    /** 变更某位专家的参加状态 */
    @PostMapping("/result/status")
    public R<Void> updateResultStatus(@RequestBody ExpertExtractResultBo bo) {
        return toAjax(expertExtractService.updateResultStatus(bo));
    }
}
