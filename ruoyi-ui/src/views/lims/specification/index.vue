<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="规格书名称" prop="specName">
        <el-input v-model="queryParams.specName" placeholder="请输入规格书名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="物料名称" prop="materialName">
        <el-input v-model="queryParams.materialName" placeholder="请输入物料名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" @click="handleAdd">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" :disabled="single" @click="handleUpdate">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" :disabled="multiple" @click="handleDelete">删除</el-button></el-col>
    </el-row>
    <el-table v-loading="loading" :data="specList" @selection-change="handleSelectionChange" :row-key="row => row.id" @expand-change="handleExpandChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column type="expand" width="50">
        <template slot-scope="scope">
          <div style="padding:12px 20px;">
            <div style="margin-bottom:8px;"><el-button type="primary" size="small" icon="el-icon-plus" @click="handleItemAdd(scope.row)">新增检测项目</el-button></div>
            <el-table :data="scope.row.itemList" size="small">
              <el-table-column label="项目名称" prop="itemName" />
              <el-table-column label="检测方法" prop="itemMethod" />
              <el-table-column label="规格下限" prop="specLower" width="100" />
              <el-table-column label="规格上限" prop="specUpper" width="100" />
              <el-table-column label="单位" prop="unit" width="60" />
              <el-table-column label="检测组" prop="testGroupName" />
              <el-table-column label="排序" prop="sortOrder" width="60" />
              <el-table-column label="操作" width="120">
                <template slot-scope="subScope">
                  <el-button type="text" icon="el-icon-edit" @click="handleItemEdit(subScope.row)">编辑</el-button>
                  <el-button type="text" icon="el-icon-delete" @click="handleItemDelete(subScope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="规格书名称" prop="specName" />
      <el-table-column label="物料名称" prop="materialName" />
      <el-table-column label="版本" prop="version" width="80" />
      <el-table-column label="状态" prop="status" width="80">
        <template slot-scope="scope"><el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="160" />
      <el-table-column label="操作" width="150">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="规格书名称" prop="specName"><el-input v-model="form.specName" placeholder="请输入规格书名称" /></el-form-item>
        <el-form-item label="物料名称" prop="materialName"><el-input v-model="form.materialName" placeholder="请输入物料名称" /></el-form-item>
        <el-form-item label="版本号" prop="version"><el-input v-model="form.version" placeholder="如 V1.0" /></el-form-item>
        <el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio label="0">启用</el-radio><el-radio label="1">停用</el-radio></el-radio-group></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></span>
    </el-dialog>
    <el-dialog :title="itemDialogTitle" :visible.sync="itemDialogVisible" width="600px">
      <el-form ref="itemFormRef" :model="itemForm" :rules="itemRules" label-width="100px">
        <el-form-item label="项目名称" prop="itemName"><el-input v-model="itemForm.itemName" placeholder="如：纯度、铁含量" /></el-form-item>
        <el-form-item label="检测方法" prop="itemMethod"><el-input v-model="itemForm.itemMethod" placeholder="如：滴定法" /></el-form-item>
        <el-form-item label="规格下限" prop="specLower"><el-input-number v-model="itemForm.specLower" :precision="4" :min="0" style="width:200px" /></el-form-item>
        <el-form-item label="规格上限" prop="specUpper"><el-input-number v-model="itemForm.specUpper" :precision="4" :min="0" style="width:200px" /></el-form-item>
        <el-form-item label="单位" prop="unit"><el-input v-model="itemForm.unit" placeholder="如：%、mg/L" style="width:200px" /></el-form-item>
        <el-form-item label="检测组" prop="testGroupId"><el-select v-model="itemForm.testGroupId" placeholder="请选择" filterable style="width:250px"><el-option v-for="g in groupOptions" :key="g.id" :label="g.groupName" :value="g.id" /></el-select></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="itemDialogVisible = false">取消</el-button><el-button type="primary" @click="submitItemForm">确定</el-button></span>
    </el-dialog>
  </div>
</template>

<script>
import { listSpecification, getSpecification, addSpecification, updateSpecification, delSpecification, listSpecItem, addSpecItem, updateSpecItem, delSpecItem } from '@/api/lims/specification'
import { listTestGroup } from '@/api/lims/testGroup'

export default {
  name: 'Specification',
  data() {
    return {
      specList: [], loading: false, total: 0, showSearch: true, single: true, multiple: true,
      dialogTitle: '', dialogVisible: false, itemDialogTitle: '', itemDialogVisible: false, groupOptions: [],
      queryParams: { pageNum: 1, pageSize: 10, specName: null, materialName: null },
      form: { id: null, specName: null, materialName: null, version: null, status: '0', remark: null },
      itemForm: { id: null, specId: null, itemName: null, itemMethod: null, specLower: null, specUpper: null, unit: null, testGroupId: null },
      rules: { specName: [{ required: true, trigger: 'blur' }], materialName: [{ required: true, trigger: 'blur' }] },
      itemRules: { itemName: [{ required: true, trigger: 'blur' }] }
    }
  },
  created() { this.getList() },
  methods: {
    getList() { this.loading = true; listSpecification(this.queryParams).then(res => { this.specList = res.rows; this.total = res.total; this.loading = false }) },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.$refs.queryRef.resetFields(); this.handleQuery() },
    handleSelectionChange(s) { this.single = s.length !== 1; this.multiple = !s.length },
    handleExpandChange(row, expanded) {
      if (expanded && expanded.length > 0) {
        listSpecItem({ specId: row.id, pageSize: 999 }).then(res => { this.$set(row, "itemList", res.rows) })
      }
    },
    handleAdd() { this.dialogTitle = '新增规格书'; this.form = { id: null, specName: null, materialName: null, version: null, status: '0', remark: null }; this.dialogVisible = true },
    handleUpdate(row) { this.dialogTitle = '修改规格书'; getSpecification(row.id).then(res => { this.$set(this, 'form', res.data); this.dialogVisible = true }) },
    submitForm() { this.$refs.formRef.validate(valid => { if (valid) (this.form.id ? updateSpecification(this.form) : addSpecification(this.form)).then(() => { this.$modal.msgSuccess('操作成功'); this.dialogVisible = false; this.getList() }) }) },
    handleDelete(row) { const ids = row.id || this.getIds(this.specList); this.$modal.confirm('确认删除？').then(() => delSpecification(ids)).then(() => { this.getList(); this.$modal.msgSuccess('删除成功') }) },
    loadGroupOptions() { listTestGroup({ pageSize: 999 }).then(res => { this.groupOptions = res.rows }) },
    handleItemAdd(row) { this.loadGroupOptions(); this.itemDialogTitle = '新增检测项目'; this.itemForm = { id: null, specId: row.id, itemName: null, itemMethod: null, specLower: null, specUpper: null, unit: null, testGroupId: null }; this.itemDialogVisible = true },
    handleItemEdit(row) { this.loadGroupOptions(); this.itemDialogTitle = '编辑检测项目'; this.itemForm = { ...row }; this.itemDialogVisible = true },
    submitItemForm() { this.$refs.itemFormRef.validate(valid => { if (valid) { const g = this.groupOptions.find(x => x.id === this.itemForm.testGroupId); const p = { ...this.itemForm }; if (g) p.testGroupName = g.groupName; (p.id ? updateSpecItem(p) : addSpecItem(p)).then(() => { this.$modal.msgSuccess('操作成功'); this.itemDialogVisible = false; this.getList() }) } }) },
    handleItemDelete(id) { this.$modal.confirm('确认删除？').then(() => delSpecItem(id)).then(() => { this.$modal.msgSuccess('删除成功') }) }
  }
}
</script>