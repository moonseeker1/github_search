<template>
  <div class="chat-page">
    <div class="chat-scroll-container" ref="messagesContainer">
      <div class="messages-area">
        <div class="chat-header">
          <div class="header-icon">
            <el-icon :size="32"><ChatDotRound /></el-icon>
          </div>
          <div class="header-text">
            <h3>代码问答</h3>
            <p>对已索引的仓库进行智能问答</p>
          </div>
        </div>

        <!-- Messages -->
        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message-row', msg.role]"
        >
          <div class="avatar" :class="msg.role">
            <el-icon v-if="msg.role === 'assistant'"><Monitor /></el-icon>
            <span v-else>{{ userInitial }}</span>
          </div>
          <div class="message-bubble" :class="msg.role">
            <div class="message-content">{{ msg.content }}</div>
          </div>
        </div>

        <div v-if="loading" class="message-row assistant">
          <div class="avatar assistant">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="message-bubble assistant">
            <div class="message-content loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>思考中...</span>
            </div>
          </div>
        </div>

        <!-- Input -->
        <div class="input-area">
          <div class="repo-selector">
            <el-select v-model="selectedRepoId" placeholder="选择已索引的仓库" style="width: 100%">
              <el-option
                v-for="repo in indexedRepos"
                :key="repo.id"
                :label="repo.name"
                :value="repo.id"
              />
            </el-select>
          </div>
          <div class="input-wrapper">
            <el-input
              v-model="inputText"
              placeholder="输入您的代码问题..."
              @keyup.enter="sendMessage"
              :disabled="loading || !selectedRepoId"
            />
            <el-button
              type="primary"
              @click="sendMessage"
              :disabled="!inputText.trim() || loading || !selectedRepoId"
            >
              <el-icon><Promotion /></el-icon>
              发送
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ChatDotRound, Monitor, Loading, Promotion } from '@element-plus/icons-vue'
import request from '../utils/request'

const messagesContainer = ref(null)
const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const selectedRepoId = ref(null)
const indexedRepos = ref([])
const userInitial = ref('U')

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTo({
        top: messagesContainer.value.scrollHeight,
        behavior: 'smooth'
      })
    }
  })
}

onMounted(() => {
  messages.value = [{
    role: 'assistant',
    content: '你好！我是代码问答助手。请先选择一个已索引的仓库，然后提问关于代码的问题。'
  }]

  // Load indexed repos (mock data for now)
  // In production, this would load from backend
  indexedRepos.value = [
    { id: 123456, name: 'example/spring-boot-demo' },
    { id: 789012, name: 'example/python-ml-project' }
  ]

  scrollToBottom()
})

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || loading.value || !selectedRepoId.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const result = await request.post('/rag/ask', {
      query: text,
      repoId: selectedRepoId.value,
      topK: 5
    })

    messages.value.push({ role: 'assistant', content: result.answer })
  } catch (e) {
    messages.value.push({
      role: 'assistant',
      content: '抱歉，处理您的问题时出错了。请确保仓库已正确索引。'
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
.chat-page {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, #f8f9fc 0%, #f0f2f8 100%);
}

.chat-scroll-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow-y: auto;
}

.messages-area {
  padding: 24px;
  max-width: 900px;
  margin: 0 auto;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  margin-bottom: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.25);
}

.header-icon {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.header-text h3 {
  margin: 0 0 4px 0;
  color: white;
  font-size: 20px;
}

.header-text p {
  margin: 0;
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
}

.message-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-row.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 18px;
  font-weight: 600;
}

.avatar.assistant {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.avatar.user {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(17, 153, 142, 0.3);
}

.message-bubble {
  max-width: 70%;
  border-radius: 18px;
  font-size: 15px;
  line-height: 1.6;
}

.message-bubble.assistant {
  background: white;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-bottom-left-radius: 6px;
}

.message-bubble.user {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 6px;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.message-content {
  padding: 14px 18px;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-content.loading {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
}

.input-area {
  margin-top: 24px;
  padding: 20px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.repo-selector {
  margin-bottom: 12px;
}

.input-wrapper {
  display: flex;
  gap: 12px;
}

.input-wrapper :deep(.el-input__wrapper) {
  border-radius: 14px;
}

.input-wrapper :deep(.el-button) {
  border-radius: 14px;
}
</style>
