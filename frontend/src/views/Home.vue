<template>
  <div class="home-container">
    <div class="hero-section">
      <div class="hero-content">
        <div class="hero-icon">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"/>
          </svg>
        </div>
        <h1>GitHub 代码搜索</h1>
        <p>搜索开源仓库，智能分析代码，获取 AI 驱动的洞察</p>

        <div class="search-box">
          <el-input
            v-model="searchQuery"
            placeholder="搜索 GitHub 仓库..."
            size="large"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" size="large" @click="handleSearch">
            搜索
          </el-button>
        </div>
      </div>
    </div>

    <div class="features-section">
      <div class="feature-card" @click="router.push('/search')">
        <div class="feature-icon search">
          <el-icon :size="32"><Search /></el-icon>
        </div>
        <h3>仓库搜索</h3>
        <p>搜索 GitHub 上的开源仓库，按语言、星标筛选</p>
      </div>

      <div class="feature-card" @click="router.push('/chat')">
        <div class="feature-icon chat">
          <el-icon :size="32"><ChatDotRound /></el-icon>
        </div>
        <h3>代码问答</h3>
        <p>对索引的仓库进行智能问答，获取代码解释</p>
      </div>

      <div class="feature-card">
        <div class="feature-icon code">
          <el-icon :size="32"><Document /></el-icon>
        </div>
        <h3>代码分析</h3>
        <p>自动生成代码摘要和文档</p>
      </div>

      <div class="feature-card">
        <div class="feature-icon recommend">
          <el-icon :size="32"><Connection /></el-icon>
        </div>
        <h3>相似推荐</h3>
        <p>发现相似的代码仓库和实现</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, ChatDotRound, Document, Connection } from '@element-plus/icons-vue'

const router = useRouter()
const searchQuery = ref('')

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ path: '/search', query: { q: searchQuery.value } })
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f9fc 0%, #f0f2f8 100%);
}

.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 80px 20px;
  text-align: center;
  color: white;
}

.hero-content {
  max-width: 700px;
  margin: 0 auto;
}

.hero-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
}

.hero-icon svg {
  width: 48px;
  height: 48px;
}

.hero-content h1 {
  font-size: 42px;
  font-weight: 700;
  margin: 0 0 16px;
}

.hero-content p {
  font-size: 18px;
  opacity: 0.9;
  margin: 0 0 40px;
}

.search-box {
  display: flex;
  gap: 12px;
  max-width: 600px;
  margin: 0 auto;
}

.search-box :deep(.el-input__wrapper) {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.search-box :deep(.el-button) {
  border-radius: 12px;
  padding: 0 32px;
}

.features-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 24px;
  max-width: 1100px;
  margin: -40px auto 40px;
  padding: 0 20px;
}

.feature-card {
  background: white;
  border-radius: 20px;
  padding: 32px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}

.feature-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  color: white;
}

.feature-icon.search {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.feature-icon.chat {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.feature-icon.code {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.feature-icon.recommend {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.feature-card h3 {
  font-size: 18px;
  color: #303133;
  margin: 0 0 12px;
}

.feature-card p {
  font-size: 14px;
  color: #909399;
  margin: 0;
  line-height: 1.6;
}
</style>
