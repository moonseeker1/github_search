<template>
  <div class="search-page">
    <div class="search-header">
      <div class="search-form">
        <el-input
          v-model="query"
          placeholder="搜索 GitHub 仓库..."
          size="large"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="language" placeholder="语言" size="large" clearable style="width: 150px">
          <el-option label="全部" value="" />
          <el-option label="Java" value="java" />
          <el-option label="Python" value="python" />
          <el-option label="JavaScript" value="javascript" />
          <el-option label="TypeScript" value="typescript" />
          <el-option label="Go" value="go" />
          <el-option label="Rust" value="rust" />
          <el-option label="C++" value="cpp" />
        </el-select>
        <el-button type="primary" size="large" @click="handleSearch" :loading="loading">
          搜索
        </el-button>
      </div>
      <div class="result-info" v-if="total > 0">
        找到 {{ total.toLocaleString() }} 个仓库
      </div>
    </div>

    <div class="search-results" v-loading="loading">
      <div v-if="repositories.length === 0 && !loading" class="empty-state">
        <el-empty description="输入关键词搜索 GitHub 仓库" />
      </div>

      <div v-else class="repo-list">
        <div
          v-for="repo in repositories"
          :key="repo.id"
          class="repo-card"
          @click="goToRepo(repo)"
        >
          <div class="repo-header">
            <img :src="repo.avatar" class="avatar" />
            <div class="repo-info">
              <h3 class="repo-name">{{ repo.fullName }}</h3>
              <p class="repo-desc">{{ repo.description || '暂无描述' }}</p>
            </div>
          </div>
          <div class="repo-meta">
            <span class="meta-item" v-if="repo.language">
              <span class="lang-dot" :class="repo.language.toLowerCase()"></span>
              {{ repo.language }}
            </span>
            <span class="meta-item">
              <el-icon><Star /></el-icon>
              {{ formatNumber(repo.stars) }}
            </span>
            <span class="meta-item">
              <el-icon><Fork /></el-icon>
              {{ formatNumber(repo.forks) }}
            </span>
          </div>
        </div>
      </div>

      <div class="pagination" v-if="total > perPage">
        <el-pagination
          v-model:current-page="page"
          :page-size="perPage"
          :total="Math.min(total, 1000)"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, Star, Fork } from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const route = useRoute()

const query = ref('')
const language = ref('')
const page = ref(1)
const perPage = ref(20)
const total = ref(0)
const loading = ref(false)
const repositories = ref([])

onMounted(() => {
  if (route.query.q) {
    query.value = route.query.q
    handleSearch()
  }
})

const handleSearch = async () => {
  if (!query.value.trim()) return

  loading.value = true
  try {
    const result = await request.get('/search/repositories', {
      params: {
        query: query.value,
        language: language.value || undefined,
        page: page.value,
        perPage: perPage.value
      }
    })
    repositories.value = result.items || []
    total.value = result.total || 0
  } catch (e) {
    console.error('Search failed:', e)
  } finally {
    loading.value = false
  }
}

const handlePageChange = () => {
  handleSearch()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goToRepo = (repo) => {
  router.push(`/repository/${repo.owner}/${repo.name}`)
}

const formatNumber = (num) => {
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num
}
</script>

<style scoped>
.search-page {
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
  min-height: calc(100vh - 64px);
  background: linear-gradient(180deg, #f8f9fc 0%, #f0f2f8 100%);
}

.search-header {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.search-form {
  display: flex;
  gap: 12px;
}

.search-form :deep(.el-input__wrapper) {
  border-radius: 10px;
}

.result-info {
  margin-top: 16px;
  color: #606266;
  font-size: 14px;
}

.search-results {
  min-height: 400px;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.repo-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.repo-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.repo-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.repo-header {
  display: flex;
  gap: 16px;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 12px;
}

.repo-info {
  flex: 1;
}

.repo-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px;
}

.repo-name:hover {
  color: #667eea;
}

.repo-desc {
  font-size: 14px;
  color: #909399;
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.repo-meta {
  display: flex;
  gap: 16px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f2f5;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
}

.lang-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ccc;
}

.lang-dot.java { background: #b07219; }
.lang-dot.python { background: #3572A5; }
.lang-dot.javascript { background: #f1e05a; }
.lang-dot.typescript { background: #2b7489; }
.lang-dot.go { background: #00ADD8; }
.lang-dot.rust { background: #dea584; }
.lang-dot.cpp { background: #f34b7d; }

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
