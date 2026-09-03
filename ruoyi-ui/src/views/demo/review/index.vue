<template>
  <div class="app-container">
    <!-- 文档列表 -->
    <template v-if="mode === 'list'">
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="el-icon-refresh" size="mini" @click="getList">刷新</el-button>
        </el-col>
      </el-row>

      <el-table v-loading="loading" :data="docList">
        <el-table-column label="ID" align="center" prop="id" width="80" />
        <el-table-column label="文档名称" align="center" prop="docName" />
        <el-table-column label="文件路径" align="center" prop="filePath" show-overflow-tooltip />
        <el-table-column label="状态" align="center" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === '1' ? 'success' : 'warning'">
              {{ scope.row.status === '1' ? '已审核' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="180" />
        <el-table-column label="操作" align="center" width="120">
          <template slot-scope="scope">
            <el-button type="primary" size="mini" @click="openReview(scope.row)">审核</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
    </template>

    <!-- 审核页 -->
    <div v-else class="review-layout">
      <div class="review-header">
        <el-button icon="el-icon-back" size="mini" @click="backList">返回</el-button>
        <span class="review-title">{{ content.doc.docName }}</span>
        <el-button
          :type="addMode ? 'danger' : 'warning'"
          size="mini"
          @click="toggleAddMode"
        >{{ addMode ? '取消补充' : '手动补充' }}</el-button>
        <el-button
          v-if="adjustId"
          type="danger"
          size="mini"
          @click="cancelAdjust"
        >取消调整</el-button>
        <el-button type="success" size="mini" icon="el-icon-check" @click="applyDoc">执行脱敏</el-button>
      </div>

      <div class="review-tip">
        <span v-if="addMode" class="tip-text">请在左侧文档中划选需要补充的敏感内容</span>
        <span v-else-if="adjustId" class="tip-text">请重新划选该记录的准确范围</span>
        <span v-else class="tip-text">点击右侧记录可确认或忽略；黄色为机器识别，绿色为已确认</span>
      </div>

      <div class="review-body">
        <div class="doc-panel" @mouseup="onDocMouseUp">
          <template v-for="(node, ni) in content.nodes">
            <p
              v-if="node.type === 'PARAGRAPH'"
              :key="ni"
              class="doc-block"
              :data-block="String(node.blockId)"
            >
              <span
                v-for="(part, pi) in paragraphParts(node.blockId, node.text)"
                :key="pi"
                :class="part.cls"
              >{{ part.text }}</span>
            </p>
            <table v-else-if="node.type === 'TABLE'" :key="ni" class="doc-table">
              <tbody>
                <tr v-for="(row, ri) in node.rows" :key="ri">
                  <td v-for="(cell, ci) in row" :key="ci">
                    <p
                      v-for="(para, pi) in cell.paragraphs"
                      :key="pi"
                      class="doc-block"
                      :data-block="String(para.blockId)"
                    >
                      <span
                        v-for="(part, pj) in paragraphParts(para.blockId, para.text)"
                        :key="pj"
                        :class="part.cls"
                      >{{ part.text }}</span>
                    </p>
                  </td>
                </tr>
              </tbody>
            </table>
          </template>
        </div>

        <div class="span-panel">
          <div class="span-panel-title">敏感识别记录（{{ content.spans.length }}）</div>
          <div v-for="span in content.spans" :key="span.id" class="span-item">
            <div class="span-text">
              {{ span.spanText }}
              <el-tag size="mini" type="warning">{{ span.spanType }}</el-tag>
              <el-tag size="mini" :type="span.source === 'M' ? 'info' : 'success'">
                {{ span.source === 'M' ? '机器' : '人工' }}
              </el-tag>
            </div>
            <div class="span-status">{{ statusText(span.status) }}</div>
            <div class="span-actions">
              <el-button
                v-if="span.status === '0'"
                type="success"
                size="mini"
                @click="confirmSpan(span.id)"
              >确认</el-button>
              <el-button
                v-if="span.status === '0'"
                type="info"
                size="mini"
                @click="ignoreSpan(span.id)"
              >忽略</el-button>
              <el-button
                v-if="span.status !== '2'"
                type="warning"
                size="mini"
                @click="startAdjust(span)"
              >调整</el-button>
            </div>
          </div>
          <el-empty v-if="content.spans.length === 0" description="暂无识别记录" />
        </div>
      </div>
    </div>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="460px">
      <el-form label-width="80px">
        <el-form-item label="原文">
          <span class="dialog-text">{{ form.spanText }}</span>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.spanType" placeholder="请选择类型" style="width: 100%">
            <el-option v-for="t in types" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" size="mini" @click="submitSpan">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listReviewDoc,
  getReviewContent,
  confirmReviewSpan,
  ignoreReviewSpan,
  addReviewSpan,
  updateReviewSpan,
  applyReviewDoc
} from '@/api/demo/review'

export default {
  name: 'DemoReview',
  data() {
    return {
      mode: 'list',
      loading: false,
      docList: [],
      total: 0,
      queryParams: { pageNum: 1, pageSize: 10 },
      content: { doc: {}, nodes: [], spans: [] },
      addMode: false,
      adjustId: null,
      dialogVisible: false,
      dialogTitle: '手动补充',
      types: ['人名', '地名', '机构名', '电话', '其他'],
      form: {}
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listReviewDoc(this.queryParams).then(res => {
        this.docList = res.rows
        this.total = res.total
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    openReview(row) {
      this.loading = true
      getReviewContent(row.id).then(res => {
        this.content = res.data
        this.mode = 'review'
        this.resetReviewState()
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    backList() {
      this.mode = 'list'
      this.resetReviewState()
      this.getList()
    },
    resetReviewState() {
      this.addMode = false
      this.adjustId = null
      this.dialogVisible = false
      this.form = {}
    },
    reloadContent() {
      getReviewContent(this.content.doc.id).then(res => {
        this.content = res.data
        this.resetReviewState()
      })
    },
    statusText(status) {
      return status === '0' ? '待确认' : status === '1' ? '已确认' : '已忽略'
    },
    paragraphParts(blockId, text) {
      const spans = (this.content.spans || []).filter(s => String(s.blockId) === String(blockId) && s.status !== '2')
      if (!spans.length) {
        return [{ text: text, cls: 'normal' }]
      }
      const len = text.length
      const bounds = new Set([0, len])
      spans.forEach(s => {
        bounds.add(Math.max(0, Math.min(len, s.startChar)))
        bounds.add(Math.max(0, Math.min(len, s.endChar)))
      })
      const sorted = Array.from(bounds).sort((a, b) => a - b)
      const parts = []
      for (let i = 0; i < sorted.length - 1; i++) {
        const start = sorted[i]
        const end = sorted[i + 1]
        if (end <= start) {
          continue
        }
        const covered = spans.filter(s => s.startChar <= start && s.endChar >= end)
        if (covered.length) {
          const cls = covered.some(s => s.status === '1') ? 'hl-confirmed' : 'hl-pending'
          parts.push({ text: text.slice(start, end), cls: cls })
        } else {
          parts.push({ text: text.slice(start, end), cls: 'normal' })
        }
      }
      return parts
    },
    confirmSpan(id) {
      confirmReviewSpan(id).then(() => {
        this.$message.success('已确认')
        this.reloadContent()
      })
    },
    ignoreSpan(id) {
      ignoreReviewSpan(id).then(() => {
        this.$message.success('已忽略')
        this.reloadContent()
      })
    },
    toggleAddMode() {
      this.addMode = !this.addMode
      this.adjustId = null
    },
    startAdjust(span) {
      this.addMode = false
      this.adjustId = span.id
      this.$message.info('请在文档中重新划选范围')
    },
    cancelAdjust() {
      this.adjustId = null
    },
    onDocMouseUp() {
      if (!this.addMode && !this.adjustId) {
        return
      }
      this.$nextTick(() => {
        const selection = window.getSelection()
        if (!selection || selection.isCollapsed) {
          return
        }
        const range = selection.getRangeAt(0)
        const blockEl = this.findBlockElement(range.startContainer)
        const endBlockEl = this.findBlockElement(range.endContainer)
        if (!blockEl || blockEl !== endBlockEl) {
          return
        }
        const start = this.textOffset(blockEl, range.startContainer, range.startOffset)
        const end = this.textOffset(blockEl, range.endContainer, range.endOffset)
        const text = selection.toString()
        if (!text) {
          return
        }
        const blockId = Number(blockEl.getAttribute('data-block'))
        if (this.adjustId) {
          const old = this.content.spans.find(s => s.id === this.adjustId)
          if (old) {
            this.form = {
              id: old.id,
              docId: this.content.doc.id,
              blockId: blockId,
              startChar: start,
              endChar: end,
              spanText: text,
              spanType: old.spanType
            }
            this.dialogTitle = '调整范围'
          }
        } else {
          this.form = {
            docId: this.content.doc.id,
            blockId: blockId,
            startChar: start,
            endChar: end,
            spanText: text,
            spanType: '人名'
          }
          this.dialogTitle = '手动补充'
        }
        selection.removeAllRanges()
        this.dialogVisible = true
      })
    },
    findBlockElement(node) {
      let el = node.nodeType === 3 ? node.parentElement : node
      while (el && el !== document.body) {
        if (el.getAttribute && el.getAttribute('data-block') !== null) {
          return el
        }
        el = el.parentElement
      }
      return null
    },
    textOffset(blockEl, container, offset) {
      const walker = document.createTreeWalker(blockEl, NodeFilter.SHOW_TEXT)
      let count = 0
      let node
      while ((node = walker.nextNode())) {
        if (node === container) {
          return count + offset
        }
        count += (node.nodeValue || '').length
      }
      return count
    },
    submitSpan() {
      const action = this.form.id ? updateReviewSpan(this.form) : addReviewSpan(this.form)
      action.then(() => {
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.reloadContent()
      })
    },
    applyDoc() {
      this.$confirm('将按所有“已确认”记录替换敏感内容，并生成 _masked.docx 文件，是否继续？', '执行脱敏', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        applyReviewDoc(this.content.doc.id).then(res => {
          this.$message.success(res.msg || '处理成功')
          this.$alert(res.data || '处理完成', '结果', { confirmButtonText: '确定' })
          this.reloadContent()
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.review-layout {
  background: #fff;
  padding: 12px;
  border-radius: 4px;
}
.review-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.review-title {
  flex: 1;
  font-weight: 600;
  font-size: 16px;
}
.review-tip {
  padding: 6px 10px;
  background: #f8f8f8;
  margin-bottom: 8px;
  font-size: 12px;
  color: #666;
}
.tip-text {
  color: #e6a23c;
}
.review-body {
  display: flex;
  gap: 12px;
  min-height: 500px;
}
.doc-panel {
  flex: 1;
  border: 1px solid #e5e5e5;
  padding: 16px;
  min-width: 0;
  max-height: 720px;
  overflow: auto;
  line-height: 1.8;
}
.doc-block {
  margin: 0 0 8px;
  word-break: break-all;
}
.normal {
  background: transparent;
}
.hl-pending {
  background: #f7d674;
  cursor: pointer;
}
.hl-confirmed {
  background: #b3e19d;
}
.doc-table {
  border-collapse: collapse;
  margin: 8px 0;
  width: 100%;
}
.doc-table td {
  border: 1px solid #d0d0d0;
  padding: 6px 8px;
}
.span-panel {
  width: 320px;
  border: 1px solid #e5e5e5;
  padding: 10px;
  max-height: 720px;
  overflow: auto;
}
.span-panel-title {
  font-weight: 600;
  margin-bottom: 8px;
}
.span-item {
  border: 1px solid #eee;
  padding: 8px;
  margin-bottom: 8px;
  background: #fafafa;
}
.span-text {
  margin-bottom: 4px;
}
.span-status {
  color: #888;
  font-size: 12px;
  margin-bottom: 6px;
}
.span-actions {
  display: flex;
  gap: 4px;
}
.dialog-text {
  word-break: break-all;
}
</style>
