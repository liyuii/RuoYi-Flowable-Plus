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
        <el-table-column label="状态" align="center" width="120">
          <template slot-scope="scope">
            <el-tag :type="docStatusTag(scope.row.status)">{{ docStatusText(scope.row.status) }}</el-tag>
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
        <el-tag :type="docStatusTag(content.doc.status)">{{ docStatusText(content.doc.status) }}</el-tag>
        <el-input
          v-model="searchWord"
          size="mini"
          clearable
          placeholder="输入关键词复查"
          style="width: 180px"
        />
        <el-button type="warning" size="mini" icon="el-icon-plus" @click="openAddDialog">手动补充</el-button>
        <el-button
          type="success"
          size="mini"
          icon="el-icon-check"
          :disabled="content.doc.status === '2'"
          @click="completeDoc"
        >审核完成</el-button>
      </div>

      <div class="review-tip">
        同一词条会高亮全文所有出现位置；点击侧边词条可定位，点击文档高亮可选中对应词条。
      </div>

      <div class="review-body">
        <div class="doc-panel" @click="onDocClick">
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
                :data-span-id="part.spanId"
              >
                <i v-if="part.spanId" class="hl-badge">{{ part.badge }}</i>{{ part.text }}
              </span>
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
                        :data-span-id="part.spanId"
                      >
                        <i v-if="part.spanId" class="hl-badge">{{ part.badge }}</i>{{ part.text }}
                      </span>
                    </p>
                  </td>
                </tr>
              </tbody>
            </table>
          </template>
        </div>

        <div class="span-panel">
          <div class="span-panel-title">敏感词记录（{{ visibleSpans.length }}）</div>
          <div
            v-for="span in visibleSpans"
            :key="span.id"
            class="span-item"
            :class="{ active: activeSpanId === span.id }"
            :data-span-id="span.id"
            @click="scrollToSpan(span.id)"
          >
            <div class="span-text">
              <i class="side-badge">{{ spanBadge(span.id) }}</i>
              <span class="span-word">{{ span.spanText }}</span>
              <el-tag size="mini" type="warning">{{ span.spanType }}</el-tag>
              <el-tag size="mini" :type="span.source === 'M' ? 'info' : 'success'">
                {{ span.source === 'M' ? '机器' : '人工' }}
              </el-tag>
            </div>
            <div class="span-status">{{ statusText(span.status) }}</div>
            <div class="span-actions" @click.stop>
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
            </div>
          </div>
          <el-empty v-if="visibleSpans.length === 0" description="暂无敏感词记录" />
        </div>
      </div>
    </div>

    <el-dialog title="手动补充敏感词" :visible.sync="dialogVisible" width="460px">
      <el-form label-width="80px">
        <el-form-item label="敏感词">
          <el-input v-model="form.spanText" placeholder="请输入敏感词，将匹配全文所有出现" />
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
  completeReviewDoc
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
      dialogVisible: false,
      dialogTitle: '手动补充敏感词',
      types: ['人名', '地名', '机构名', '电话', '其他'],
      form: {},
      activeSpanId: null,
      searchWord: ''
    }
  },
  computed: {
    visibleSpans() {
      return (this.content.spans || []).filter(s => s.status !== '2')
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
      this.dialogVisible = false
      this.form = {}
      this.activeSpanId = null
      this.searchWord = ''
    },
    reloadContent() {
      getReviewContent(this.content.doc.id).then(res => {
        this.content = res.data
        this.activeSpanId = null
      })
    },
    statusText(status) {
      return status === '0' ? '待确认' : status === '1' ? '已确认' : '已忽略'
    },
    docStatusText(status) {
      return status === '0' ? '待审核' : status === '1' ? '审核中' : '审核完成'
    },
    docStatusTag(status) {
      return status === '0' ? 'warning' : status === '1' ? '' : 'success'
    },
    spanBadge(id) {
      const index = this.visibleSpans.findIndex(s => s.id === id)
      return index >= 0 ? index + 1 : ''
    },
    findOccurrences(text, word) {
      const result = []
      if (!word) {
        return result
      }
      let from = 0
      let index
      while ((index = text.indexOf(word, from)) !== -1) {
        result.push({ start: index, end: index + word.length })
        from = index + word.length
      }
      return result
    },
    paragraphParts(blockId, text) {
      const sensitive = []
      this.visibleSpans.forEach(span => {
        if (!span.spanText) {
          return
        }
        this.findOccurrences(text, span.spanText).forEach(range => {
          sensitive.push({ start: range.start, end: range.end, span: span })
        })
      })
      // 重叠位置优先保留较长的敏感词
      sensitive.sort((a, b) => a.start - b.start || (b.end - b.start) - (a.end - a.start))
      const kept = []
      sensitive.forEach(item => {
        if (!kept.length || item.start >= kept[kept.length - 1].end) {
          kept.push(item)
        }
      })

      const keyword = (this.searchWord || '').trim()
      const searchRanges = keyword ? this.findOccurrences(text, keyword) : []

      const bounds = new Set([0, text.length])
      kept.forEach(item => {
        bounds.add(item.start)
        bounds.add(item.end)
      })
      searchRanges.forEach(item => {
        bounds.add(item.start)
        bounds.add(item.end)
      })
      const sorted = Array.from(bounds).sort((a, b) => a - b)
      const parts = []
      for (let i = 0; i < sorted.length - 1; i++) {
        const start = sorted[i]
        const end = sorted[i + 1]
        if (end <= start) {
          continue
        }
        const hit = kept.find(item => item.start <= start && item.end >= end)
        const isSearch = searchRanges.some(item => item.start <= start && item.end >= end)
        const cls = []
        if (hit) {
          cls.push(hit.span.status === '1' ? 'hl-confirmed' : 'hl-pending')
        }
        if (isSearch) {
          cls.push('search-hit')
        }
        if (!cls.length) {
          cls.push('normal')
        }
        parts.push({
          text: text.slice(start, end),
          cls: cls,
          spanId: hit ? hit.span.id : null,
          badge: hit ? this.spanBadge(hit.span.id) : ''
        })
      }
      return parts
    },
    scrollToSpan(id) {
      this.activeSpanId = id
      this.$nextTick(() => {
        const elements = Array.from(document.querySelectorAll('.doc-panel [data-span-id="' + id + '"]'))
        if (!elements.length) {
          return
        }
        elements[0].scrollIntoView({ behavior: 'smooth', block: 'center' })
        elements.forEach(el => {
          el.classList.add('flash')
          setTimeout(() => el.classList.remove('flash'), 1200)
        })
      })
    },
    onDocClick(event) {
      const el = event.target.closest ? event.target.closest('[data-span-id]') : null
      if (!el) {
        return
      }
      const id = Number(el.getAttribute('data-span-id'))
      this.activeSpanId = id
      this.$nextTick(() => {
        const side = document.querySelector('.span-panel .span-item[data-span-id="' + id + '"]')
        if (side) {
          side.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
        }
      })
    },
    openAddDialog() {
      this.form = {
        docId: this.content.doc.id,
        spanText: '',
        spanType: '人名'
      }
      this.dialogVisible = true
    },
    submitSpan() {
      if (!this.form.spanText || !this.form.spanText.trim()) {
        this.$message.warning('请输入敏感词')
        return
      }
      addReviewSpan(this.form).then(() => {
        this.$message.success('补充成功')
        this.dialogVisible = false
        this.reloadContent()
      }).catch(() => {})
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
    completeDoc() {
      this.$confirm('审核完成后文档状态将变为“审核完成”，确认没有遗漏的待确认记录？', '审核完成', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        completeReviewDoc(this.content.doc.id).then(res => {
          this.$message.success(res.msg || '审核完成')
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
  position: relative;
}
.hl-confirmed {
  background: #b3e19d;
  position: relative;
}
.search-hit {
  outline: 2px dashed #409eff;
  background: rgba(64, 158, 255, 0.15);
}
.hl-pending.flash,
.hl-confirmed.flash {
  outline: 2px solid #409eff;
}
.hl-badge {
  position: absolute;
  top: -8px;
  left: -8px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  font-size: 10px;
  font-style: normal;
  line-height: 16px;
  text-align: center;
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
  cursor: pointer;
}
.span-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}
.span-text {
  margin-bottom: 4px;
}
.side-badge {
  display: inline-block;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #909399;
  color: #fff;
  font-size: 11px;
  font-style: normal;
  line-height: 18px;
  text-align: center;
  margin-right: 6px;
}
.span-word {
  font-weight: 600;
  margin-right: 6px;
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
</style>
