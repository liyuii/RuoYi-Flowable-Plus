<template>
  <el-dialog
    :title="'角色功能数据范围：' + form.roleName"
    :visible.sync="visible"
    width="920px"
    append-to-body
  >
    <div class="scope-toolbar">
      <el-select
        v-model="selectedScopeKey"
        size="small"
        filterable
        placeholder="选择要配置的功能"
        style="width: 260px"
        @change="addRule"
      >
        <el-option
          v-for="item in candidateScopes"
          :key="item.scopeKey"
          :label="item.scopeName"
          :value="item.scopeKey"
        />
      </el-select>
    </div>

    <el-table v-if="rules.length > 0" :data="rules" border>
      <el-table-column label="功能名称" min-width="160">
        <template slot-scope="scope">
          <span>{{ scope.row.scopeName }}</span>
          <div class="scope-key">{{ scope.row.scopeKey }}</div>
        </template>
      </el-table-column>
      <el-table-column label="数据范围" width="190">
        <template slot-scope="scope">
          <el-select
            v-model="scope.row.dataScope"
            size="small"
            placeholder="请选择"
            style="width: 100%"
            @change="handleScopeChange(scope.row)"
          >
            <el-option
              v-for="item in dataScopeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="自定义部门" min-width="150">
        <template slot-scope="scope">
          <template v-if="scope.row.dataScope === '2'">
            <span>{{ scope.row.deptIds.length }} 个部门</span>
            <el-button type="text" icon="el-icon-more" @click="openDeptDialog(scope.row)">选择部门</el-button>
          </template>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90" align="center">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-delete" @click="removeRule(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-else description="还没有配置功能范围" />

    <template #footer>
      <el-button size="small" @click="visible = false">取 消</el-button>
      <el-button size="small" type="primary" @click="submitForm">确 定</el-button>
    </template>

    <el-dialog
      title="选择部门"
      :visible.sync="deptVisible"
      width="520px"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-checkbox v-model="deptExpand" @change="handleDeptExpand">展开/折叠</el-checkbox>
      <el-checkbox v-model="deptNodeAll" @change="handleDeptNodeAll">全选/全不选</el-checkbox>
      <el-tree
        ref="deptDialogTree"
        class="dept-tree"
        :data="deptOptions"
        show-checkbox
        default-expand-all
        node-key="id"
        :check-strictly="false"
        :props="defaultProps"
        @check="handleDeptCheck"
      />
      <template #footer>
        <el-button size="small" @click="deptVisible = false">取 消</el-button>
        <el-button size="small" type="primary" @click="confirmDept">确 定</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script>
import {
  getRoleDataScopeRules,
  saveRoleDataScopeRules,
  deptTreeSelect
} from '@/api/system/role'

export default {
  name: 'FunctionDataScope',
  data() {
    return {
      visible: false,
      deptVisible: false,
      form: { roleId: null, roleName: '' },
      rules: [],
      candidateScopes: [],
      selectedScopeKey: '',
      activeRow: null,
      deptOptions: [],
      deptExpand: true,
      deptNodeAll: false,
      dataScopeOptions: [
        { value: '1', label: '全部数据权限' },
        { value: '2', label: '自定义数据权限' },
        { value: '3', label: '本部门数据权限' },
        { value: '4', label: '本部门及以下数据权限' },
        { value: '5', label: '仅本人数据权限' }
      ],
      defaultProps: { children: 'children', label: 'label' }
    }
  },
  methods: {
    open(row) {
      this.form = { roleId: row.roleId, roleName: row.roleName }
      getRoleDataScopeRules(row.roleId).then(res => {
        const data = res.data || {}
        this.rules = (data.configuredRules || []).map(item => ({
          scopeKey: item.scopeKey,
          scopeName: item.scopeName,
          dataScope: item.dataScope || '',
          deptIds: item.deptIds || []
        }))
        this.candidateScopes = data.candidateScopes || []
        this.selectedScopeKey = ''
        this.visible = true
      })
      this.loadDeptTree(row.roleId)
    },
    loadDeptTree(roleId) {
      deptTreeSelect(roleId).then(res => {
        this.deptOptions = (res.data && res.data.depts) || []
      })
    },
    addRule(scopeKey) {
      const candidate = this.candidateScopes.find(item => item.scopeKey === scopeKey)
      if (!candidate) {
        this.selectedScopeKey = ''
        return
      }
      this.rules.push({
        scopeKey: candidate.scopeKey,
        scopeName: candidate.scopeName,
        dataScope: '',
        deptIds: []
      })
      this.candidateScopes = this.candidateScopes.filter(item => item.scopeKey !== scopeKey)
      this.selectedScopeKey = ''
    },
    handleScopeChange(row) {
      if (row.dataScope !== '2') {
        row.deptIds = []
      } else if (row.deptIds.length === 0) {
        this.$nextTick(() => this.openDeptDialog(row))
      }
    },
    removeRule(row) {
      this.rules = this.rules.filter(item => item.scopeKey !== row.scopeKey)
      const define = this.candidateScopes.find(item => item.scopeKey === row.scopeKey)
      if (!define) {
        this.candidateScopes.push({ scopeKey: row.scopeKey, scopeName: row.scopeName })
      }
    },
    openDeptDialog(row) {
      this.activeRow = row
      this.deptVisible = true
      this.deptExpand = true
      this.deptNodeAll = false
      this.$nextTick(() => {
        const tree = this.$refs.deptDialogTree
        if (tree) {
          tree.setCheckedKeys(row.deptIds || [])
        }
      })
    },
    handleDeptCheck() {
      const tree = this.$refs.deptDialogTree
      if (tree && this.activeRow) {
        const checked = tree.getCheckedKeys()
        const half = tree.getHalfCheckedKeys()
        this.activeRow.deptIds = Array.from(new Set(checked.concat(half)))
      }
    },
    confirmDept() {
      this.handleDeptCheck()
      this.deptVisible = false
      this.activeRow = null
    },
    handleDeptExpand(value) {
      const tree = this.$refs.deptDialogTree
      if (!tree) {
        return
      }
      const loop = nodes => {
        nodes.forEach(node => {
          tree.store.nodesMap[node.id].expanded = value
          if (node.children) {
            loop(node.children)
          }
        })
      }
      loop(this.deptOptions)
    },
    handleDeptNodeAll(value) {
      const tree = this.$refs.deptDialogTree
      if (tree) {
        tree.setCheckedNodes(value ? this.deptOptions : [])
        this.handleDeptCheck()
      }
    },
    submitForm() {
      const invalid = this.rules.find(item => !item.dataScope)
      if (invalid) {
        this.$modal.msgError('功能「' + invalid.scopeName + '」还未选择数据范围')
        return
      }
      const customInvalid = this.rules.find(item => item.dataScope === '2' && item.deptIds.length === 0)
      if (customInvalid) {
        this.$modal.msgError('功能「' + customInvalid.scopeName + '」为自定义范围，请选择部门')
        return
      }
      const rules = this.rules.map(item => ({
        scopeKey: item.scopeKey,
        dataScope: item.dataScope,
        deptIds: item.dataScope === '2' ? item.deptIds : []
      }))
      saveRoleDataScopeRules({ roleId: this.form.roleId, rules: rules }).then(() => {
        this.$modal.msgSuccess('保存成功')
        this.visible = false
      })
    }
  }
}
</script>

<style scoped>
.scope-toolbar {
  margin-bottom: 12px;
}
.scope-key {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}
.dept-tree {
  max-height: 400px;
  overflow: auto;
  margin-top: 8px;
  border: 1px solid #e5e5e5;
  padding: 6px;
}
</style>
