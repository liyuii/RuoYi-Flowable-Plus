<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="检测组名称" prop="groupName">
        <el-input v-model="queryParams.groupName" placeholder="请输入检测组名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="检测组编码" prop="groupCode">
        <el-input v-model="queryParams.groupCode" placeholder="请输入检测组编码" clearable @keyup.enter="handleQuery" />
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
    <el-table v-loading="loading" :data="groupList" @selection-change="handleSelectionChange" :row-key="row => row.id" ref="groupTableRef" @expand-change="handleExpandChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column type="expand" width="50">
        <template slot-scope="scope">
          <div style="padding:12px 20px;">
            <div style="margin-bottom:8px;"><el-button type="primary" size="small" icon="el-icon-plus" @click="handleMemberAdd(scope.row)">新增成员</el-button></div>
            <el-table :data="scope.row.memberList" size="small">
              <el-table-column label="成员姓名" prop="userName" />
              <el-table-column label="排序号" prop="sortOrder" width="80" />
              <el-table-column label="操作" width="100">
                <template slot-scope="subScope"><el-button type="danger" size="small" icon="el-icon-delete" @click="handleMemberDelete(subScope.row, scope.row)" /></template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="检测组编码" prop="groupCode" />
      <el-table-column label="检测组名称" prop="groupName" />
      <el-table-column label="备注" prop="remark" />
      <el-table-column label="创建时间" prop="createTime" width="160" />
      <el-table-column label="操作" width="150">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="检测组编码" prop="groupCode"><el-input v-model="form.groupCode" placeholder="请输入检测组编码" /></el-form-item>
        <el-form-item label="检测组名称" prop="groupName"><el-input v-model="form.groupName" placeholder="请输入检测组名称" /></el-form-item>
        <el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio label="0">正常</el-radio><el-radio label="1">停用</el-radio></el-radio-group></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></span>
    </el-dialog>
    <el-dialog title="新增成员" :visible.sync="memberDialogVisible" width="400px">
      <el-form ref="memberFormRef" :model="memberForm" :rules="memberRules" label-width="80px">
        <el-form-item label="用户" prop="userId"><el-select v-model="memberForm.userId" placeholder="请选择用户" filterable style="width:100%"><el-option v-for="u in userList" :key="u.userId" :label="u.userName" :value="u.userId" /></el-select></el-form-item>
        <el-form-item label="排序号" prop="sortOrder"><el-input-number v-model="memberForm.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="memberDialogVisible = false">取消</el-button><el-button type="primary" @click="submitMemberForm">确定</el-button></span>
    </el-dialog>
  </div>
</template>

<script>
import { listTestGroup, getTestGroup, addTestGroup, updateTestGroup, delTestGroup, listGroupMember, addGroupMember, delGroupMember } from '@/api/lims/testGroup'
import { listUser } from '@/api/system/user'

export default {
  name: 'TestGroup',
  data() {
    return {
      groupList: [], loading: false, total: 0, showSearch: true, single: true, multiple: true,
      dialogTitle: '', dialogVisible: false, memberDialogVisible: false, currentGroup: null, userList: [],
      queryParams: { pageNum: 1, pageSize: 10, groupName: null, groupCode: null },
      form: { id: null, groupCode: null, groupName: null, status: '0', remark: null },
      memberForm: { groupId: null, userId: null, userName: null, sortOrder: 0 },
      rules: { groupCode: [{ required: true, message: '编码不能为空', trigger: 'blur' }], groupName: [{ required: true, message: '名称不能为空', trigger: 'blur' }] },
      memberRules: { userId: [{ required: true, message: '请选择用户', trigger: 'change' }] }
    }
  },
  created() { this.getList() },
  methods: {
    getList() { this.loading = true; listTestGroup(this.queryParams).then(res => { this.groupList = res.rows; this.total = res.total; this.loading = false }) },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.$refs.queryRef.resetFields(); this.handleQuery() },
    handleSelectionChange(selection) { this.single = selection.length !== 1; this.multiple = !selection.length },
    handleExpandChange(row, expanded) {
      if (expanded && expanded.length > 0) {
        listGroupMember({ groupId: row.id, pageSize: 999 }).then(res => { this.$set(row, "memberList", res.rows) })
      }
    },
    refreshMemberList(groupId) {
      const group = this.groupList.find(g => g.id === groupId)
      if (group) {
        listGroupMember({ groupId, pageSize: 999 }).then(res => { this.$set(group, "memberList", res.rows) })
      }
    },
    handleAdd() { this.dialogTitle = '新增检测组'; this.form.id = null; this.form.groupCode = null; this.form.groupName = null; this.form.status = '0'; this.form.remark = null; this.dialogVisible = true },
    handleUpdate(row) { this.dialogTitle = '修改检测组'; getTestGroup(row.id || row.id).then(res => { this.$set(this, 'form', res.data); this.dialogVisible = true }) },
    submitForm() { this.$refs.formRef.validate(valid => { if (valid) (this.form.id ? updateTestGroup(this.form) : addTestGroup(this.form)).then(() => { this.$modal.msgSuccess('操作成功'); this.dialogVisible = false; this.getList() }) }) },
    handleDelete(row) { const ids = row.id || this.getIds(this.groupList); this.$modal.confirm('确认删除？').then(() => delTestGroup(ids)).then(() => { this.getList(); this.$modal.msgSuccess('删除成功') }) },
    handleMemberAdd(row) { this.currentGroup = row; this.memberForm.groupId = row.id; this.memberForm.userId = null; this.memberForm.sortOrder = 0; listUser({ pageNum: 1, pageSize: 999 }).then(res => { this.userList = res.rows }); this.memberDialogVisible = true },
    submitMemberForm() { this.$refs.memberFormRef.validate(valid => { if (valid) { const u = this.userList.find(x => x.userId === this.memberForm.userId); this.memberForm.userName = u ? u.userName : ''; addGroupMember(this.memberForm).then(() => { this.$modal.msgSuccess('新增成功'); this.memberDialogVisible = false; this.refreshMemberList(this.memberForm.groupId) }) } }) },
    handleMemberDelete(member, group) { this.$modal.confirm('确认移除该成员？').then(() => delGroupMember(member.id)).then(() => { this.$modal.msgSuccess('移除成功'); this.refreshMemberList(group.id || member.groupId) }) }
  }
}
</script>