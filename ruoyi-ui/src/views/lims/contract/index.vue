<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="合同名称" prop="contractName"><el-input v-model="queryParams.contractName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width:120px">
          <el-option label="草稿" value="0" /><el-option label="审批中" value="1" /><el-option label="已通过" value="2" /><el-option label="已驳回" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button><el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" @click="handleAdd">新增合同</el-button></el-col><el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" :disabled="multiple" @click="handleDelete">删除</el-button></el-col></el-row>
    <el-table v-loading="loading" :data="contractList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="合同编号" prop="contractNo" width="180" />
      <el-table-column label="合同名称" prop="contractName" min-width="200" />
      <el-table-column label="合同类型" prop="contractType" width="100" />
      <el-table-column label="合同金额" width="130"><template slot-scope="s"><span>{{ s.row.currency || "CNY" }} {{ s.row.amount }}</span></template></el-table-column>
      <el-table-column label="申请人" prop="createBy" width="100" />
      <el-table-column label="状态" width="90"><template slot-scope="s"><el-tag :type="statusTag(s.row.status)">{{ statusLabel(s.row.status) }}</el-tag></template></el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template slot-scope="s">
          <el-button type="text" icon="el-icon-view" @click="handleDetail(s.row)">详情</el-button>
          <el-button type="text" icon="el-icon-edit" v-if="s.row.status === '0'" @click="handleUpdate(s.row)">编辑</el-button>
          <el-button type="text" icon="el-icon-upload" v-if="s.row.status === '0'" @click="handleSubmit(s.row)">提交</el-button>
          <el-button type="text" icon="el-icon-delete" v-if="s.row.status === '0'" @click="handleDelete(s.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="700px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="合同名称" prop="contractName"><el-input v-model="form.contractName" placeholder="请输入合同名称" /></el-form-item>
        <el-row><el-col :span="12"><el-form-item label="合同类型" prop="contractType"><el-select v-model="form.contractType" placeholder="请选择" style="width:100%"><el-option label="采购合同" value="采购" /><el-option label="销售合同" value="销售" /><el-option label="服务合同" value="服务" /><el-option label="其他" value="其他" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="合同金额" prop="amount"><el-input v-model="form.amount" placeholder="请输入金额" /></el-form-item></el-col></el-row>
        <el-row><el-col :span="12"><el-form-item label="甲方"><el-input v-model="form.partyA" placeholder="请输入甲方" /></el-form-item></el-col><el-col :span="12"><el-form-item label="乙方"><el-input v-model="form.partyB" placeholder="请输入乙方" /></el-form-item></el-col></el-row>
        <el-row><el-col :span="12"><el-form-item label="生效日期"><el-date-picker v-model="form.startDate" type="date" value-format="yyyy-MM-dd" style="width:100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="到期日期"><el-date-picker v-model="form.endDate" type="date" value-format="yyyy-MM-dd" style="width:100%" /></el-form-item></el-col></el-row>
        <el-form-item label="合同内容"><el-input v-model="form.content" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submitForm">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script>
import { listContract, getContract, addContract, updateContract, delContract, submitContract } from "@/api/lims/contract"
export default {
  name: "Contract", data() {
    return { contractList: [], loading: false, total: 0, showSearch: true, multiple: true, dialogVisible: false, dialogTitle: "",
      queryParams: { pageNum: 1, pageSize: 10, contractName: null, status: null }, form: {},
      rules: { contractName: [{ required: true, message: "合同名称不能为空", trigger: "blur" }] } }
  }, created() { this.getList() },
  methods: {
    getList() { this.loading = true; listContract(this.queryParams).then(r => { this.contractList = r.rows; this.total = r.total; this.loading = false }) },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() }, resetQuery() { this.$refs.queryRef.resetFields(); this.handleQuery() },
    handleSelectionChange(s) { this.multiple = !s.length },
    statusTag(s) { return { "0":"info","1":"primary","2":"success","3":"danger" }[s] || "" },
    statusLabel(s) { return { "0":"草稿","1":"审批中","2":"已通过","3":"已驳回" }[s] || "" },
    handleAdd() { this.dialogTitle = "新增合同"; this.form = {}; this.dialogVisible = true },
    handleUpdate(r) { getContract(r.id).then(res => { this.form = res.data; this.dialogTitle = "编辑合同"; this.dialogVisible = true }) },
    submitForm() { this.$refs.formRef.validate(v => { if (v) { (this.form.id ? updateContract(this.form) : addContract(this.form)).then(() => { this.$modal.msgSuccess("保存成功"); this.dialogVisible = false; this.getList() }) } }) },
    handleDetail(r) { this.$router.push("/lims/contract/detail/" + r.id) },
    handleSubmit(r) { this.$modal.confirm("确定提交审批？").then(() => { submitContract({ id: r.id }).then(() => { this.$modal.msgSuccess("提交成功"); this.getList() }) }) },
    handleDelete(r) { const ids = r.id || this.selectedIds; this.$modal.confirm("确认删除？").then(() => delContract(ids).then(() => { this.getList(); this.$modal.msgSuccess("删除成功") })) }
  }
}
</script>