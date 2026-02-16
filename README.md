# GitHub 代码搜索系统

一个基于 RAG 的 GitHub 仓库搜索和代码分析系统，支持智能问答、代码摘要和相似推荐。

## 功能特性

- **仓库搜索**: 搜索 GitHub 仓库，按语言、星标筛选
- **代码问答 (RAG)**: 对索引的仓库进行智能问答
- **代码摘要**: AI 自动生成代码摘要和文档
- **相似推荐**: 发现相似的代码仓库

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.2
- MyBatis Plus 3.5.5
- MySQL 8.0
- Redis

### 前端
- Vue 3
- Vite
- Element Plus
- Pinia

### AI 模型
- BGE-M3 (Embedding)
- BGE-Reranker-v2-m3
- Qwen3-Max (LLM)

## 项目结构

```
github-search/
├── backend/                    # Spring Boot 后端
│   └── src/main/
│       ├── java/com/githubsearch/
│       │   ├── client/        # GitHub/LLM/Model 客户端
│       │   ├── controller/    # REST 控制器
│       │   ├── service/       # 业务逻辑
│       │   ├── mapper/        # 数据访问
│       │   ├── entity/        # 实体类
│       │   └── util/          # 工具类
│       └── resources/
│           ├── application.yml
│           └── schema.sql
├── frontend/                   # Vue 3 前端
│   └── src/
│       ├── views/             # 页面组件
│       ├── components/        # 通用组件
│       ├── router/            # 路由配置
│       └── utils/             # 工具函数
└── model-service/             # Python 模型服务
    ├── embedding_service.py   # BGE-M3 Embedding 服务
    ├── reranker_service.py    # Reranker 服务
    └── requirements.txt
```

## 快速开始

### 1. 环境要求

- Java 17+
- Node.js 18+
- Python 3.10+
- MySQL 8.0+
- Redis

### 2. 数据库配置

```sql
# 创建数据库
mysql -u root -p < backend/src/main/resources/schema.sql
```

### 3. 配置 GitHub Token

```bash
# 设置环境变量
export GITHUB_TOKEN=your_github_token
```

或在 `application.yml` 中配置:

```yaml
github:
  api:
    token: your_github_token
```

### 4. 启动模型服务

```bash
cd model-service

# 安装依赖
pip install -r requirements.txt

# 启动 Embedding 服务 (端口 8000)
python embedding_service.py &

# 启动 Reranker 服务 (端口 8001)
python reranker_service.py &
```

### 5. 启动后端

```bash
cd backend
./mvnw spring-boot:run
```

后端服务将在 `http://localhost:9400` 启动。

### 6. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 `http://localhost:3001` 启动。

## API 文档

### 搜索接口

```
GET /api/search/repositories?query=xxx&language=java&page=1&perPage=20
GET /api/search/repository/{owner}/{repo}
GET /api/search/repository/{owner}/{repo}/files?path=
GET /api/search/repository/{owner}/{repo}/file?path=xxx
GET /api/search/repository/{owner}/{repo}/summary
GET /api/search/code?query=xxx&language=python
```

### RAG 接口

```
POST /api/rag/index
POST /api/rag/ask
POST /api/rag/similar
POST /api/rag/summarize
GET  /api/rag/indexed/{repoId}
DELETE /api/rag/index/{repoId}
```

## 配置说明

### application.yml

```yaml
# 服务端口
server:
  port: 9400

# 数据库
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/github_search
    username: root
    password: 123456

# GitHub API
github:
  api:
    base-url: https://api.github.com
    token: ${GITHUB_TOKEN:}

# 模型服务
model:
  embedding:
    url: http://127.0.0.1:8000
  reranker:
    url: http://127.0.0.1:8001

# LLM (Qwen)
llm:
  api-key: your_api_key
  base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
  model: qwen3-max
```

## 扩展性

系统设计支持以下扩展：

1. **向量数据库**: 当前使用内存存储，可替换为 Milvus/Pinecone
2. **模型服务**: 支持替换为其他 Embedding/LLM 模型
3. **微服务化**: 可拆分为独立的搜索/向量/RAG 服务

## License

MIT
