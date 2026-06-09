<template>
  <div class="app-container">
    <div v-loading="loading">
      <h3>{{ pageTitle }}</h3>
      <el-descriptions :column="2" border size="small" style="margin-bottom:16px;">
        <el-descriptions-item label="报检单">{{ inspection.inspectionNo || '加载中...' }}</el-descriptions-item>
        <el-descriptions-item label="批号">{{ inspection.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="规格书">{{ inspection.specId }}</el-descriptions-item>
        <el-descriptions-item label="取样日期">{{ inspection.sampleDate }}</el-descriptions-item>
      </el-descriptions>

      <!-- 多实例模式：单个检测项目 -->
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
              <el-input v-model="currentItem.resultValue" placeholder="请输入检测结果" style="width:200px;" />
            </el-form-item>
            <el-form-item label="QC判定">
              <el-tag v-if="qcResult === 'P'" type="success">合格</el-tag>
              <el-tag v-else-if="qcResult === 'F'" type="danger">不合格</el-tag>
              <span v-else>输入结果后自动判定</span>
            </el-form-item>
          </el-form>
        </el-card>
        <div style="margin-top:16px;text-align:right;">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
        </div>
      </div>

      <!-- 预览模式（无 taskId）：显示全部项目 -->
      <div v-else>
        <el-table :data="itemList" border>
          <el-table-column label="检测项目" prop="itemName" width="120" />
          <el-table-column label="检测方法" prop="itemMethod" width="120" />
          <el-table-column label="规格下限" prop="specLower" width="80" />
          <el-table-column label="规格上限" prop="specUpper" width="80" />
          <el-table-column label="结果值" width="150">
            <template slot-scope="scope">
              <el-input v-model="scope.row.resultValue" size="small" @input="onResultChange(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="QC判定" prop="qcResult" width="80">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.qcResult === 'P'" size="small" type="success">合格</el-tag>
              <el-tag v-else-if="scope.row.qcResult === 'F'" size="small" type="danger">不合格</el-tag>
              <span v-else>&#8212;</span>
            </template>
          </el-table-column>
          <el-table-column label="检测组" prop="testGroupName" width="100" />
        </el-table>
        <div style="margin-top:16px;text-align:right;">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" @click="handleSubmitPreview">保存</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getInspection, listTestItem, updateTestItem } from '@/api/lims/inspection'
import { getTestItemByTask } from '@/api/lims/inspection'
import { complete } from '@/api/workflow/task'

export default {
  name: 'AssayInput',
  data() {
    return {
      loading: true,
      submitting: false,
      inspection: {},
      itemList: [],
      currentItem: null,
      qcResult: null,
      pageTitle: '检测结果录入',
      taskId: null
    }
  },
  created() {
    this.taskId = this.$route.query.taskId
    const businessKey = this.$route.query.businessKey
    const inspectionId = this.$route.params.inspectionId || this.$route.query.inspectionId

    if (this.taskId) {
      // 多实例模式：加载当前任务的 testItem
      this.pageTitle = '检测结果录入'
      Promise.all([
        this.loadInspectionByTask(this.taskId),
        this.loadCurrentItem(this.taskId)
      ]).finally(() => { this.loading = false })
    } else if (inspectionId) {
      // 预览模式：加载报检单所有项目
      this.pageTitle = '检测结果预览'
      Promise.all([
        getInspection(inspectionId).then(res => { this.inspection = res.data }),
        listTestItem({ inspectionId, pageSize: 999 }).then(res => { this.itemList = res.rows })
      ]).finally(() => { this.loading = false })
    } else {
      this.loading = false
    }
  },
  methods: {
    loadInspectionByTask(taskId) {
      // 通过 taskId 获取 businessKey 再查报检单
      // 简化处理：从 URL 参数读取 businessKey
      const bk = this.$route.query.businessKey
      if (bk && bk.startsWith('inspection_')) {
        const id = bk.replace('inspection_', '')
        return getInspection(id).then(res => { this.inspection = res.data })
      }
    },
    loadCurrentItem(taskId) {
      return getTestItemByTask(taskId).then(res => {
        this.currentItem = res.data
        if (this.currentItem) {
          this.onResultChange(this.currentItem)
        }
      })
    },
    onResultChange(row) {
      if (row.resultValue && row.specLower !== null && row.specUpper !== null) {
        const val = parseFloat(row.resultValue)
        if (!isNaN(val)) {
          row.qcResult = (val >= row.specLower && val <= row.specUpper) ? 'P' : 'F'
          if (row === this.currentItem) {
            this.qcResult = row.qcResult
          }
        } else {
          row.qcResult = null
          if (row === this.currentItem) this.qcResult = null
        }
      }
    },
    handleSubmit() {
      if (!this.taskId || !this.currentItem) return
      this.submitting = true
      updateTestItem(this.currentItem).then(() => {
        return complete({ taskId: this.taskId })
      }).then(() => {
        this.$modal.msgSuccess('提交成功')
        this.submitting = false
        this.goBack()
      }).catch(() => { this.submitting = false })
    },
    handleSubmitPreview() {
      const promises = this.itemList.map(item => updateTestItem(item))
      Promise.all(promises).then(() => {
        this.$modal.msgSuccess('保存成功')
      })
    },
    goBack() {
      this.$router.back()
    }
  }
}
</script>