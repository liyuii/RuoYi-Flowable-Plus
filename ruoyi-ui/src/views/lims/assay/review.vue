<template>
  <div class="app-container">
    <div v-loading="loading">
      <h3>结果审核 — {{ inspection.inspectionNo }}</h3>
      <el-descriptions :column="2" border size="small" style="margin-bottom:16px;">
        <el-descriptions-item label="报检单">{{ inspection.inspectionNo }}</el-descriptions-item>
        <el-descriptions-item label="批号">{{ inspection.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="规格书">{{ inspection.specId }}</el-descriptions-item>
        <el-descriptions-item label="取样日期">{{ inspection.sampleDate }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="currentItem">
        <el-card shadow="hover">
          <div slot="header">
            <span>检测项目：{{ currentItem.itemName }}</span>
          </div>
          <el-form label-width="120px" size="small">
            <el-form-item label="检测方法">{{ currentItem.itemMethod }}</el-form-item>
            <el-form-item label="规格范围">{{ currentItem.specLower }} ~ {{ currentItem.specUpper }} {{ currentItem.unit }}</el-form-item>
            <el-form-item label="检测组">{{ currentItem.testGroupName }}</el-form-item>
            <el-form-item label="检测结果">
              <el-input v-model="currentItem.resultValue" readonly style="width:200px;" />
            </el-form-item>
            <el-form-item label="QC判定">
              <el-tag v-if="currentItem.qcResult === 'P'" type="success">合格</el-tag>
              <el-tag v-else-if="currentItem.qcResult === 'F'" type="danger">不合格</el-tag>
              <span v-else>未检测</span>
            </el-form-item>
          </el-form>
        </el-card>
        <div style="margin-top:16px;text-align:right;">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" @click="handleApprove" :loading="submitting">审核通过</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getTestItemByTask } from '@/api/lims/inspection'
import { complete } from '@/api/workflow/task'

export default {
  name: 'AssayReview',
  data() {
    return { loading: true, submitting: false, inspection: {}, currentItem: null, taskId: null }
  },
  created() {
    this.taskId = this.$route.query.taskId
    const bk = this.$route.query.businessKey
    if (this.taskId) {
      Promise.all([
        this.loadInspection(bk),
        getTestItemByTask(this.taskId).then(res => { this.currentItem = res.data })
      ]).finally(() => { this.loading = false })
    } else {
      this.loading = false
    }
  },
  methods: {
    loadInspection(bk) {
      if (bk && bk.startsWith('inspection_')) {
        const id = bk.replace('inspection_', '')
        return import('@/api/lims/inspection').then(m => m.getInspection(id)).then(res => { this.inspection = res.data })
      }
    },
    handleApprove() {
      this.submitting = true
      complete({ taskId: this.taskId }).then(() => {
        this.$modal.msgSuccess('审核通过')
        this.submitting = false
        this.$router.back()
      }).catch(() => { this.submitting = false })
    },
    goBack() { this.$router.back() }
  }
}
</script>