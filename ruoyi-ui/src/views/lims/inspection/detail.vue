<template>
  <div class="app-container">
    <el-page-header @back="goBack" content="报检单详情" />
    <div v-loading="loading" style="margin-top:16px;">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="报检单号" :span="1">{{ detail.inspectionNo }}</el-descriptions-item>
        <el-descriptions-item label="状态" :span="1">
          <el-tag :type="statusTag(detail.status)">{{ statusLabel(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="规格书">{{ detail.specId }}</el-descriptions-item>
        <el-descriptions-item label="结论">
          <el-tag v-if="detail.resultVerdict === 'P'" type="success">合格</el-tag>
          <el-tag v-else-if="detail.resultVerdict === 'F'" type="danger">不合格</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="生产批号">{{ detail.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="取样日期">{{ detail.sampleDate }}</el-descriptions-item>
        <el-descriptions-item label="取样地点">{{ detail.samplePlace }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detail.applicant }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark }}</el-descriptions-item>
      </el-descriptions>
      <h3 style="margin:20px 0 10px;">检测结果</h3>
      <el-table :data="itemList" border>
        <el-table-column label="检测项目" prop="itemName" />
        <el-table-column label="检测方法" prop="itemMethod" />
        <el-table-column label="规格下限" prop="specLower" width="90" />
        <el-table-column label="规格上限" prop="specUpper" width="90" />
        <el-table-column label="结果值" prop="resultValue" width="100" />
        <el-table-column label="单位" prop="unit" width="60" />
        <el-table-column label="QC判定" prop="qcResult" width="80">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.qcResult === 'P'" size="small" type="success">合格</el-tag>
            <el-tag v-else-if="scope.row.qcResult === 'F'" size="small" type="danger">不合格</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="检测组" prop="testGroupName" width="120" />
        <el-table-column label="检测人" prop="assignee" width="80" />
        <el-table-column label="检测时间" prop="detectTime" width="140" />
      </el-table>
    </div>
  </div>
</template>

<script>
import { getInspection } from '@/api/lims/inspection'
import { listTestItem } from '@/api/lims/inspection'

export default {
  name: 'InspectionDetail',
  data() {
    return { loading: false, detail: {}, itemList: [] }
  },
  created() {
    const id = this.$route.params.id
    if (id) {
      this.loading = true
      getInspection(id).then(res => { this.detail = res.data })
      listTestItem({ inspectionId: id, pageSize: 999 }).then(res => { this.itemList = res.rows; this.loading = false })
    }
  },
  methods: {
    goBack() { this.$router.push('/lims/inspection') },
    statusTag(s) { return { '0':'info','1':'primary','2':'warning','3':'success','4':'danger' }[s] || '' },
    statusLabel(s) { return { '0':'待检','1':'检测中','2':'审核中','3':'已完成','4':'已驳回' }[s] || '' }
  }
}
</script>