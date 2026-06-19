<template>
  <div class="app-container">
    <div v-loading="loading">
      <h3>检测报告 — {{ inspection.inspectionNo }}</h3>
      <el-descriptions :column="2" border size="small" style="margin-bottom:16px;">
        <el-descriptions-item label="报检单号">{{ inspection.inspectionNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag type="success">已完成</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="规格书">{{ inspection.specId }}</el-descriptions-item>
        <el-descriptions-item label="生产批号">{{ inspection.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="取样日期">{{ inspection.sampleDate }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ inspection.applicant }}</el-descriptions-item>
      </el-descriptions>

      <h4 style="margin:16px 0 8px;">检测结果汇总</h4>
      <el-table :data="itemList" border>
        <el-table-column label="检测项目" prop="itemName" />
        <el-table-column label="检测方法" prop="itemMethod" />
        <el-table-column label="规格下限" prop="specLower" width="80" />
        <el-table-column label="规格上限" prop="specUpper" width="80" />
        <el-table-column label="结果值" prop="resultValue" width="100" />
        <el-table-column label="单位" prop="unit" width="60" />
        <el-table-column label="QC判定" prop="qcResult" width="80">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.qcResult === 'P'" size="small" type="success">合格</el-tag>
            <el-tag v-else-if="scope.row.qcResult === 'F'" size="small" type="danger">不合格</el-tag>
            <span v-else>&#8212;</span>
          </template>
        </el-table-column>
        <el-table-column label="检测组" prop="testGroupName" width="100" />
        <el-table-column label="检测人" prop="assignee" width="70" />
      </el-table>

      <div style="margin-top:16px;text-align:right;">
        <el-button @click="goBack">返回</el-button>
        <el-button type="primary" @click="handleComplete" :loading="submitting">生成报告</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { listTestItem } from '@/api/lims/inspection'
import { complete } from '@/api/workflow/task'

export default {
  name: 'AssayReport',
  data() {
    return { loading: true, submitting: false, inspection: {}, itemList: [], taskId: null }
  },
  created() {
    this.taskId = this.$route.query.taskId
    const bk = this.$route.query.businessKey
    if (bk && bk.startsWith('inspection_')) {
      const id = bk.replace('inspection_', '')
      Promise.all([
        import('@/api/lims/inspection').then(m => m.getInspection(id)).then(res => { this.inspection = res.data }),
        listTestItem({ inspectionId: id, pageSize: 999 }).then(res => { this.itemList = res.rows })
      ]).finally(() => { this.loading = false })
    } else {
      this.loading = false
    }
  },
  methods: {
    handleComplete() {
      if (!this.taskId) return
      this.submitting = true
      complete({ taskId: this.taskId }).then(() => {
        this.$modal.msgSuccess('报告已生成，流程结束')
        this.submitting = false
        this.$router.back()
      }).catch(() => { this.submitting = false })
    },
    goBack() { this.$router.back() }
  }
}
</script>