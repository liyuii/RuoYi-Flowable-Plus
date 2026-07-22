<template>
  <div id="onlyoffice-editor" style="width:100%;height:100vh;"></div>
</template>

<script>
import { getEditorConfig } from "@/api/lims/sysFile"

export default {
  name: "OnlineEditor",
  data() {
    return {
      docEditor: null
    }
  },
  mounted() {
    const fileId = this.$route.params.fileId
    if (!fileId) {
      this.$modal.msgError("缺少文件ID")
      return
    }
    getEditorConfig(fileId).then(res => {
      if (res.code !== 200) {
        this.$modal.msgError(res.msg || "获取编辑配置失败")
        return
      }
      this.loadEditor(res.data)
    }).catch(() => {
      this.$modal.msgError("获取编辑配置失败")
    })
  },
  methods: {
    loadEditor(cfg) {
      var script = document.createElement("script")
      script.src = cfg.editorUrl
      script.type = "text/javascript"
      var vm = this
      script.onload = function() {
        vm.docEditor = new DocsAPI.DocEditor("onlyoffice-editor", {
          document: {
            url: cfg.documentUrl,
            fileType: cfg.fileType,
            key: cfg.key,
            title: cfg.title
          },
          editorConfig: {
            callbackUrl: cfg.callbackUrl,
            lang: "zh-CN",
            user: {
              id: cfg.userId,
              name: cfg.userName
            }
          },
          token: cfg.token
        })
      }
      document.head.appendChild(script)
    }
  },
  beforeDestroy() {
    if (this.docEditor) {
      try { this.docEditor.destroyEditor() } catch(e) {}
      this.docEditor = null
    }
  }
}
</script>

<style>
body { margin: 0; overflow: hidden; }
</style>
