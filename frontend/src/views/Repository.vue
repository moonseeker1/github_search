<template>
  <div class="repo-page" v-loading="loading">
    <div class="repo-header" v-if="repo">
      <div class="repo-title">
        <img :src="repo.avatar" class="avatar" />
        <div class="title-info">
          <h1>{{ repo.fullName }}</h1>
          <p>{{ repo.description || '暂无描述' }}</p>
        </div>
      </div>
      <div class="repo-stats">
        <div class="stat">
          <el-icon><Star /></el-icon>
          <span>{{ formatNumber(repo.stars) }}</span>
        </div>
        <div class="stat">
          <el-icon><Fork /></el-icon>
          <span>{{ formatNumber(repo.forks) }}</span>
        </div>
        <div class="stat" v-if="repo.language">
          <span class="lang-dot" :class="repo.language.toLowerCase()"></span>
          <span>{{ repo.language }}</span>
        </div>
        <el-button type="primary" @click="openGitHub">
          <el-icon><Link /></el-icon>
          GitHub
        </el-button>
      </div>
    </div>

    <div class="repo-content" v-if="repo">
      <!-- Tabs -->
      <el-tabs v-model="activeTab">
        <el-tab-pane label="文件结构" name="files">
          <div class="files-panel">
            <div class="file-tree">
              <div
                v-for="file in files"
                :key="file.path"
                :class="['file-item', { active: selectedFile?.path === file.path, folder: file.type === 'dir' }]"
                @click="handleFileClick(file)"
              >
                <el-icon v-if="file.type === 'dir'"><Folder /></el-icon>
                <el-icon v-else><Document /></el-icon>
                <span>{{ file.name }}</span>
              </div>
            </div>
            <div class="file-content">
              <div v-if="selectedFile" class="code-viewer">
                <div class="file-path">{{ selectedFile.path }}</div>
                <pre v-if="fileContent"><code :class="'language-' + fileLanguage">{{ fileContent }}</code></pre>
                <el-empty v-else description="无法读取文件内容" />
              </div>
              <el-empty v-else description="选择文件查看内容" />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="AI 摘要" name="summary">
          <div class="summary-panel">
            <el-button type="primary" @click="generateSummary" :loading="summaryLoading">
              生成 AI 摘要
            </el-button>
            <div class="summary-content" v-if="summary">
              <div v-html="renderedSummary"></div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="代码问答" name="qa">
          <div class="qa-panel">
            <div class="index-section" v-if="!isIndexed">
              <el-alert title="请先索引仓库" type="info" :closable="false" show-icon>
                索引仓库后可以对代码进行智能问答
              </el-alert>
              <el-button type="primary" @click="indexRepo" :loading="indexLoading" style="margin-top: 16px">
                索引仓库
              </el-button>
            </div>
            <div v-else class="qa-section">
              <el-input
                v-model="question"
                placeholder="输入关于代码的问题..."
                @keyup.enter="askQuestion"
              >
                <template #append>
                  <el-button @click="askQuestion" :loading="qaLoading">提问</el-button>
                </template>
              </el-input>
              <div class="answer" v-if="answer">
                <h4>回答：</h4>
                <div class="answer-content">{{ answer }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Star, Fork, Link, Folder, Document } from '@element-plus/icons-vue'
import { marked } from 'marked'
import request from '../utils/request'

const route = useRoute()
const loading = ref(true)
const repo = ref(null)
const files = ref([])
const selectedFile = ref(null)
const fileContent = ref('')
const activeTab = ref('files')
const summary = ref('')
const summaryLoading = ref(false)
const isIndexed = ref(false)
const indexLoading = ref(false)
const question = ref('')
const answer = ref('')
const qaLoading = ref(false)

const fileLanguage = computed(() => {
  if (!selectedFile.value) return 'text'
  const path = selectedFile.value.path
  const ext = path.split('.').pop().toLowerCase()
  const langMap = {
    js: 'javascript', ts: 'typescript', py: 'python', java: 'java',
    go: 'go', rs: 'rust', c: 'c', cpp: 'cpp', h: 'c',
    vue: 'vue', html: 'html', css: 'css', json: 'json',
    md: 'markdown', sh: 'bash', yaml: 'yaml', yml: 'yaml'
  }
  return langMap[ext] || 'text'
})

const renderedSummary = computed(() => {
  return summary.value ? marked(summary.value) : ''
})

onMounted(async () => {
  const { owner, repo: name } = route.params
  await loadRepo(owner, name)
})

const loadRepo = async (owner, name) => {
  loading.value = true
  try {
    repo.value = await request.get(`/search/repository/${owner}/${name}`)
    files.value = await request.get(`/search/repository/${owner}/${name}/files`, { params: { path: '' } })

    // Check if indexed
    try {
      isIndexed.value = await request.get(`/rag/indexed/${repo.value.id}`)
    } catch (e) {
      isIndexed.value = false
    }
  } catch (e) {
    console.error('Failed to load repo:', e)
  } finally {
    loading.value = false
  }
}

const handleFileClick = async (file) => {
  if (file.type === 'dir') {
    // Navigate into directory
    const { owner, repo: name } = route.params
    files.value = await request.get(`/search/repository/${owner}/${name}/files`, { params: { path: file.path } })
  } else {
    selectedFile.value = file
    try {
      const result = await request.get(`/search/repository/${route.params.owner}/${route.params.repo}/file`, {
        params: { path: file.path }
      })
      fileContent.value = result.content || ''
    } catch (e) {
      fileContent.value = ''
    }
  }
}

const openGitHub = () => {
  if (repo.value?.url) {
    window.open(repo.value.url, '_blank')
  }
}

const generateSummary = async () => {
  summaryLoading.value = true
  try {
    summary.value = await request.get(`/search/repository/${route.params.owner}/${route.params.repo}/summary`)
  } catch (e) {
    console.error('Failed to generate summary:', e)
  } finally {
    summaryLoading.value = false
  }
}

const indexRepo = async () => {
  indexLoading.value = true
  try {
    await request.post('/rag/index', {
      owner: route.params.owner,
      repo: route.params.repo,
      repoId: repo.value.id
    })
    isIndexed.value = true
  } catch (e) {
    console.error('Failed to index:', e)
  } finally {
    indexLoading.value = false
  }
}

const askQuestion = async () => {
  if (!question.value.trim()) return
  qaLoading.value = true
  try {
    const result = await request.post('/rag/ask', {
      query: question.value,
      repoId: repo.value.id,
      topK: 5
    })
    answer.value = result.answer
  } catch (e) {
    console.error('Failed to ask:', e)
  } finally {
    qaLoading.value = false
  }
}

const formatNumber = (num) => {
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num
}
</script>

<style scoped>
.repo-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.repo-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 32px;
  color: white;
  margin-bottom: 24px;
}

.repo-title {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 16px;
}

.title-info h1 {
  font-size: 24px;
  margin: 0 0 8px;
}

.title-info p {
  margin: 0;
  opacity: 0.9;
}

.repo-stats {
  display: flex;
  align-items: center;
  gap: 24px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 6px;
}

.lang-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #ccc;
}

.lang-dot.java { background: #b07219; }
.lang-dot.python { background: #3572A5; }
.lang-dot.javascript { background: #f1e05a; }
.lang-dot.typescript { background: #2b7489; }
.lang-dot.go { background: #00ADD8; }

.repo-content {
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.files-panel {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 24px;
  min-height: 500px;
}

.file-tree {
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  overflow: auto;
  max-height: 600px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.file-item:hover {
  background: #f5f7fa;
}

.file-item.active {
  background: #ecf5ff;
  color: #409eff;
}

.file-item.folder {
  color: #909399;
}

.file-content {
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  overflow: auto;
}

.code-viewer {
  height: 100%;
}

.file-path {
  background: #f5f7fa;
  padding: 12px 16px;
  font-size: 13px;
  color: #606266;
  border-bottom: 1px solid #e4e7ed;
}

pre {
  margin: 0;
  padding: 16px;
  overflow: auto;
  max-height: 560px;
}

code {
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  line-height: 1.6;
}

.summary-panel {
  min-height: 300px;
}

.summary-content {
  margin-top: 24px;
  padding: 24px;
  background: #f9fafc;
  border-radius: 12px;
  line-height: 1.8;
}

.qa-panel {
  min-height: 300px;
}

.index-section {
  text-align: center;
  padding: 40px;
}

.qa-section {
  max-width: 800px;
}

.answer {
  margin-top: 24px;
  padding: 20px;
  background: #f9fafc;
  border-radius: 12px;
}

.answer h4 {
  margin: 0 0 12px;
  color: #303133;
}

.answer-content {
  white-space: pre-wrap;
  line-height: 1.8;
}
</style>
