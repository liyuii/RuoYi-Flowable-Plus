<template>
  <div class="app-container">
    <div v-loading="loading">
      <h3>检测结果录入 — {{ inspection.inspectionNo || '加载中...' }}</h3>
      <el-descriptions :column="2" border size="small" style="margin-bottom:16px;">
        <el-descriptions-item label="规格书">{{ inspection.specId }}</el-descriptions-item>
        <el-descriptions-item label="批号">{{ inspection.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="取样日期">{{ inspection.sampleDate }}</el-descriptions-item>
        <el-descriptions-item label="取样地点">{{ inspection.samplePlace }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="itemList" border>
        <el-table-column label="检测项目" prop="itemName" width="120" />
        <el-table-column label="检测方法" prop="itemMethod" width="120" />
        <el-table-column label="规格下限" prop="specLower" width="80" />
        <el-table-column label="规格上限" prop="specUpper" width="80" />
        <el-table-column label="单位" prop="unit" width="60" />
        <el-table-column label="结果值" width="150">
          <template slot-scope="scope">
            <el-input v-model="scope.row.resultValue" size="small" @input="onResultChange(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column label="QC判定" prop="qcResult" width="80">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.qcResult === 'P'" size="small" type="success">合格</el-tag>
            <el-tag v-else-if="scope.row.qcResult === 'F'" size="small" type="danger">不合格</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="检测组" prop="testGroupName" width="100" />
      </el-table>
      <div style="margin-top:16px;text-align:right;">
        <el-button @click="goBack">返回</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { getInspection } from '@/api/lims/inspection'
import { listTestItem, updateTestItem } from '@/api/lims/inspection'
import { listSpecItem } from '@/api/lims/specification'

export default {
  name: 'AssayInput',
  data() {
    return {
      loading: true,
      submitting: false,
      inspection: {},
      itemList: []
    }
  },
  created() {
    const inspectionId = this.$route.query.inspectionId || this.$route.params.inspectionId
    if (inspectionId) {
      Promise.all([
        getInspection(inspectionId).then(res => { this.inspection = res.data }),
        listTestItem({ inspectionId, pageSize: 999 }).then(res => { this.itemList = res.rows })
      ]).finally(() => { this.loading = false })
    }
  },
  methods: {
    goBack() { this.$router.back() },
    onResultChange(row) {
      if (row.resultValue && row.specLower !== null && row.specUpper !== null) {
        const val = parseFloat(row.resultValue)
        if (!isNaN(val)) {
          row.qcResult = (val >= row.specLower && val <= row.specUpper) ? 'P' : 'F'
        } else { row.qcResult = null }
      } else { row.qcResult = null }
    },
    handleSubmit() {
      this.submitting = true
      const promises = this.itemList.map(item => updateTestItem(item))
      Promise.all(promises).then(() => {
        this.$modal.msgSuccess('操作成功')
        this.submitting = false
        if (this.$route.query.taskId) {
          import('@/api/workflow/task').then(m => m.complete({ taskId: this.$route.query.taskId }))
            .then(() => { this.$modal.msgSuccess('流程已提交'); this.goBack() })
        }
      }).catch(() => { this.submitting = false })
    }
  }
}
</script>