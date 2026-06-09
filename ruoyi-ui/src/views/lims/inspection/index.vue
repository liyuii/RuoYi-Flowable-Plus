<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="报检单号" prop="inspectionNo">
        <el-input v-model="queryParams.inspectionNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="批号" prop="batchNo">
        <el-input v-model="queryParams.batchNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width:120px">
          <el-option label="待检" value="0" />
          <el-option label="检测中" value="1" />
          <el-option label="审核中" value="2" />
          <el-option label="已完成" value="3" />
          <el-option label="已驳回" value="4" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" @click="handleAdd">新增报检</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" :disabled="multiple" @click="handleDelete">删除</el-button></el-col>
    </el-row>
    <el-table v-loading="loading" :data="inspectionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="报检单号" prop="inspectionNo" width="180" />
      <el-table-column label="规格书" prop="specId" width="100" />
      <el-table-column label="批号" prop="batchNo" width="120" />
      <el-table-column label="取样日期" prop="sampleDate" width="100" />
      <el-table-column label="取样地点" prop="samplePlace" />
      <el-table-column label="申请人" prop="applicant" width="80" />
      <el-table-column label="状态" prop="status" width="90">
        <template slot-scope="scope">
          <el-tag :type="statusTag(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="结论" prop="resultVerdict" width="70">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.resultVerdict === 'P'" type="success">合格</el-tag>
          <el-tag v-else-if="scope.row.resultVerdict === 'F'" type="danger">不合格</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-view" @click="handleDetail(scope.row)">详情</el-button>
          <el-button type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
    <el-dialog title="新增报检" :visible.sync="dialogVisible" width="650px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="规格书" prop="specId">
          <el-select v-model="form.specId" placeholder="请选择规格书" filterable @change="onSpecChange" style="width:100%">
            <el-option v-for="s in specOptions" :key="s.id" :label="s.specName + ' (' + s.materialName + ')'" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="生产批号" prop="batchNo"><el-input v-model="form.batchNo" placeholder="请输入生产批号" /></el-form-item>
        <el-form-item label="取样日期" prop="sampleDate"><el-date-picker v-model="form.sampleDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd" /></el-form-item>
        <el-form-item label="取样地点" prop="samplePlace"><el-input v-model="form.samplePlace" placeholder="如：生产线A" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <div v-if="previewItems.length > 0" style="margin:12px 0 0 20px;">
        <span style="font-weight:bold;font-size:13px;">检测项目预览（来自规格书）：</span>
        <el-table :data="previewItems" size="small" style="margin-top:8px;" border>
          <el-table-column label="项目名称" prop="itemName" />
          <el-table-column label="检测方法" prop="itemMethod" />
          <el-table-column label="规格下限" prop="specLower" />
          <el-table-column label="规格上限" prop="specUpper" />
          <el-table-column label="检测组" prop="testGroupName" />
        </el-table>
      </div>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script>
import { listInspection, getInspection, addInspection, delInspection, submitInspection } from '@/api/lims/inspection'
import { listSpecification } from '@/api/lims/specification'
import { listSpecItem } from '@/api/lims/specification'

export default {
  name: 'Inspection',
  data() {
    return {
      inspectionList: [], loading: false, total: 0, showSearch: true, multiple: true,
      dialogVisible: false, specOptions: [], previewItems: [],
      queryParams: { pageNum: 1, pageSize: 10, inspectionNo: null, batchNo: null, status: null },
      form: { specId: null, batchNo: null, sampleDate: null, samplePlace: null, remark: null },
      rules: { specId: [{ required: true, message: '请选择规格书', trigger: 'change' }], batchNo: [{ required: true, trigger: 'blur' }] }
    }
  },
  created() { this.getList() },
  methods: {
    getList() { this.loading = true; listInspection(this.queryParams).then(res => { this.inspectionList = res.rows; this.total = res.total; this.loading = false }) },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.$refs.queryRef.resetFields(); this.handleQuery() },
    handleSelectionChange(selection) { this.multiple = !selection.length },
    statusTag(s) { return { '0':'info','1':'primary','2':'warning','3':'success','4':'danger' }[s] || '' },
    statusLabel(s) { return { '0':'待检','1':'检测中','2':'审核中','3':'已完成','4':'已驳回' }[s] || '' },
    handleDetail(row) { this.$router.push('/lims/inspection/detail/' + (row.id || row.id)) },
    handleAdd() { this.dialogVisible = true; this.form.specId = null; this.form.batchNo = null; this.form.sampleDate = null; this.form.samplePlace = null; this.form.remark = null; this.previewItems = []; listSpecification({ pageSize: 999 }).then(res => { this.specOptions = res.rows }) },
    onSpecChange(specId) { this.previewItems = []; if (specId) listSpecItem({ specId, pageSize: 999 }).then(res => { this.previewItems = res.rows }) },
    handleDelete(row) { const ids = row.id || this.getIds(this.inspectionList); this.$modal.confirm('确认删除？').then(() => delInspection(ids)).then(() => { this.getList(); this.$modal.msgSuccess('删除成功') }) },
    /* 提交报检单 */
    submitForm() {
      this.$refs.formRef.validate(valid => {
        if (valid) {
          submitInspection(this.form).then(() => {
            this.$modal.msgSuccess("提交成功")
            this.dialogVisible = false
            this.getList()
          })
        }
      })
    }
}
}
</script>