<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="名称" prop="demoName">
        <el-input v-model="queryParams.demoName" placeholder="请输入名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="demoType">
        <el-input v-model="queryParams.demoType" placeholder="请输入类型" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width:120px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" :disabled="single" @click="handleUpdate">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="编号" prop="demoNo" width="160" />
      <el-table-column label="名称" prop="demoName" />
      <el-table-column label="类型" prop="demoType" width="100" />
      <el-table-column label="内容" prop="content" show-overflow-tooltip />
      <el-table-column label="状态" prop="status" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
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
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="编号" prop="demoNo"><el-input v-model="form.demoNo" placeholder="请输入编号" /></el-form-item>
        <el-form-item label="名称" prop="demoName"><el-input v-model="form.demoName" placeholder="请输入名称" /></el-form-item>
        <el-form-item label="类型" prop="demoType"><el-input v-model="form.demoType" placeholder="请输入类型" /></el-form-item>
        <el-form-item label="内容" prop="content"><el-input v-model="form.content" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listTestDemo, getTestDemo, addTestDemo, updateTestDemo, delTestDemo } from '@/api/lims/testDemo'

export default {
  name: 'TestDemo',
  data() {
    return {
      dataList: [], loading: false, total: 0, showSearch: true, single: true, multiple: true,
      dialogTitle: '', dialogVisible: false,
      queryParams: { pageNum: 1, pageSize: 10, demoName: null, demoType: null, status: null },
      form: { id: null, demoNo: null, demoName: null, demoType: null, content: null, status: '0', remark: null },
      rules: { demoNo: [{ required: true, message: '编号不能为空', trigger: 'blur' }], demoName: [{ required: true, message: '名称不能为空', trigger: 'blur' }] }
    }
  },
  created() { this.getList() },
  methods: {
    getList() { this.loading = true; listTestDemo(this.queryParams).then(res => { this.dataList = res.rows; this.total = res.total; this.loading = false }) },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.$refs.queryRef.resetFields(); this.handleQuery() },
    handleSelectionChange(s) { this.single = s.length !== 1; this.multiple = !s.length },
    handleAdd() { this.dialogTitle = '新增记录'; this.form = { id: null, demoNo: null, demoName: null, demoType: null, content: null, status: '0', remark: null }; this.dialogVisible = true },
    handleUpdate(row) { this.dialogTitle = '修改记录'; getTestDemo(row.id || row.id).then(res => { this.$set(this, 'form', res.data); this.dialogVisible = true }) },
    submitForm() { this.$refs.formRef.validate(v => { if (v) (this.form.id ? updateTestDemo(this.form) : addTestDemo(this.form)).then(() => { this.$modal.msgSuccess('操作成功'); this.dialogVisible = false; this.getList() }) }) },
    handleDelete(row) { const ids = row.id || this.getIds(this.dataList); this.$modal.confirm('确认删除？').then(() => delTestDemo(ids)).then(() => { this.getList(); this.$modal.msgSuccess('删除成功') }) }
  }
}
</script>