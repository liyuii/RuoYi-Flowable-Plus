<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="专家姓名" prop="expertName">
        <el-input v-model="queryParams.expertName" placeholder="请输入专家姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="工作单位" prop="companyName">
        <el-input v-model="queryParams.companyName" placeholder="请输入工作单位" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="专业" prop="majorCode">
        <el-select v-model="queryParams.majorCode" placeholder="请选择专业" clearable style="width:180px">
          <el-option v-for="m in majorOptions" :key="m.dictValue" :label="m.dictLabel" :value="m.dictValue" />
        </el-select>
      </el-form-item>
      <el-form-item label="地区" prop="regionCode">
        <el-select v-model="queryParams.regionCode" placeholder="请选择地区" clearable style="width:160px">
          <el-option v-for="r in regionOptions" :key="r.dictValue" :label="r.dictLabel" :value="r.dictValue" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width:120px">
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
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" @click="handleAdd">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" :disabled="single" @click="handleUpdate">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" :disabled="multiple" @click="handleDelete">删除</el-button></el-col>
    </el-row>

    <el-table v-loading="loading" :data="expertList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="专家姓名" prop="expertName" width="110" />
      <el-table-column label="身份证号" width="190">
        <template slot-scope="scope">{{ maskIdCard(scope.row.idCard) }}</template>
      </el-table-column>
      <el-table-column label="联系电话" prop="phone" width="130" />
      <el-table-column label="工作单位" prop="companyName" show-overflow-tooltip />
      <el-table-column label="地区" width="120">
        <template slot-scope="scope">{{ regionLabel(scope.row.regionCode) }}</template>
      </el-table-column>
      <el-table-column label="专业" prop="majorNames" show-overflow-tooltip min-width="160" />
      <el-table-column label="状态" width="90" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="专家姓名" prop="expertName">
          <el-input v-model="form.expertName" placeholder="请输入专家姓名" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号，回避名单按姓名加身份证号匹配" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="工作单位" prop="companyName">
          <el-input v-model="form.companyName" placeholder="请输入工作单位全称，单位回避按这个匹配" />
        </el-form-item>
        <el-form-item label="所属地区" prop="regionCode">
          <el-select v-model="form.regionCode" placeholder="请选择所属地区" style="width:100%">
            <el-option v-for="r in regionOptions" :key="r.dictValue" :label="r.dictLabel" :value="r.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业" prop="majorCodes">
          <el-select v-model="form.majorCodes" multiple placeholder="请选择专业，可以多选" style="width:100%">
            <el-option v-for="m in majorOptions" :key="m.dictValue" :label="m.dictLabel" :value="m.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listExpert, getExpert, addExpert, updateExpert, delExpert } from '@/api/lims/expertInfo'
import { getDicts } from '@/api/system/dict/data'

export default {
  name: 'ExpertInfo',
  data() {
    return {
      loading: false,
      showSearch: true,
      total: 0,
      single: true,
      multiple: true,
      ids: [],
      expertList: [],
      majorOptions: [],
      regionOptions: [],
      dialogTitle: '',
      dialogVisible: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        expertName: null,
        companyName: null,
        majorCode: null,
        regionCode: null,
        status: null
      },
      form: {},
      rules: {
        expertName: [{ required: true, message: '专家姓名不能为空', trigger: 'blur' }],
        idCard: [{ required: true, message: '身份证号不能为空', trigger: 'blur' }],
        regionCode: [{ required: true, message: '请选择所属地区', trigger: 'change' }],
        majorCodes: [{ required: true, message: '请至少选择一个专业', trigger: 'change' }]
      }
    }
  },
  created() {
    this.loadDicts()
    this.getList()
  },
  methods: {
    loadDicts() {
      getDicts('expert_major').then(res => { this.majorOptions = res.data || [] })
      getDicts('expert_region').then(res => { this.regionOptions = res.data || [] })
    },
    majorLabel(code) {
      const item = this.majorOptions.find(m => m.dictValue === code)
      return item ? item.dictLabel : code
    },
    regionLabel(code) {
      const item = this.regionOptions.find(r => r.dictValue === code)
      return item ? item.dictLabel : code
    },
    maskIdCard(value) {
      if (!value || value.length < 10) {
        return value
      }
      return value.substring(0, 6) + '********' + value.substring(value.length - 4)
    },
    getList() {
      this.loading = true
      listExpert(this.queryParams).then(res => {
        this.expertList = res.rows
        this.total = res.total
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.$refs.queryRef.resetFields()
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    resetForm() {
      this.form = {
        id: null,
        expertName: null,
        idCard: null,
        phone: null,
        companyName: null,
        regionCode: null,
        majorCodes: [],
        status: '0',
        remark: null
      }
      this.$nextTick(() => {
        if (this.$refs.formRef) {
          this.$refs.formRef.clearValidate()
        }
      })
    },
    handleAdd() {
      this.resetForm()
      this.dialogTitle = '新增专家'
      this.dialogVisible = true
    },
    handleUpdate(row) {
      this.resetForm()
      const id = row.id
      getExpert(id).then(res => {
        const data = res.data
        this.form = {
          id: data.id,
          expertName: data.expertName,
          idCard: data.idCard,
          phone: data.phone,
          companyName: data.companyName,
          regionCode: data.regionCode,
          majorCodes: data.majorCodes || [],
          status: data.status,
          remark: data.remark
        }
        this.dialogTitle = '修改专家'
        this.dialogVisible = true
      })
    },
    submitForm() {
      this.$refs.formRef.validate(valid => {
        if (!valid) {
          return
        }
        const request = this.form.id ? updateExpert(this.form) : addExpert(this.form)
        request.then(() => {
          this.$modal.msgSuccess('操作成功')
          this.dialogVisible = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const deleteIds = row.id ? [row.id] : this.ids
      this.$modal.confirm('确认删除选中的专家吗？').then(() => delExpert(deleteIds)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      })
    }
  }
}
</script>
