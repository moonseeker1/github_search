# 开发指南

## 环境要求

| 软件 | 版本 | 说明 |
|------|------|------|
| Java | 17+ | 后端运行环境 |
| Node.js | 18+ | 前端运行环境 |
| Python | 3.10+ | 模型服务 |
| MySQL | 8.0+ | 数据库 |
| Redis | 6.0+ | 缓存 |
| Maven | 3.8+ | Java 构建工具 |

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/moonseeker1/github_search.git
cd github_search
```

### 2. 初始化数据库

```bash
mysql -u root -p < backend/src/main/resources/schema.sql
```

### 3. 配置环境变量

```bash
# GitHub Token
export GITHUB_TOKEN=your_github_token

# 或者在 application.yml 中配置
```

### 4. 启动模型服务

```bash
cd model-service
pip install -r requirements.txt

# 启动 Embedding 服务
python embedding_service.py &

# 启动 Reranker 服务
python reranker_service.py &
```

### 5. 启动后端

```bash
cd backend
./mvnw spring-boot:run
```

### 6. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 7. 访问应用

打开浏览器访问: http://localhost:3001

## 项目结构

```
github-search/
├── backend/                          # Spring Boot 后端
│   ├── pom.xml                       # Maven 配置
│   └── src/main/
│       ├── java/com/githubsearch/
│       │   ├── GitHubSearchApplication.java  # 启动类
│       │   ├── client/               # 外部服务客户端
│       │   │   ├── GitHubClient.java # GitHub API 客户端
│       │   │   ├── LLMClient.java    # LLM (Qwen) 客户端
│       │   │   └── ModelClient.java  # 模型服务客户端
│       │   ├── controller/           # REST 控制器
│       │   │   ├── SearchController.java
│       │   │   └── RagController.java
│       │   ├── entity/               # 数据实体
│       │   ├── mapper/               # MyBatis Mapper
│       │   ├── service/              # 业务逻辑
│       │   └── util/                 # 工具类
│       └── resources/
│           ├── application.yml       # 配置文件
│           └── schema.sql            # 数据库脚本
│
├── frontend/                         # Vue 3 前端
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── main.js                   # 入口文件
│       ├── App.vue                   # 根组件
│       ├── router/index.js           # 路由配置
│       ├── views/                    # 页面组件
│       │   ├── Home.vue              # 首页
│       │   ├── Search.vue            # 搜索页
│       │   ├── Repository.vue        # 仓库详情页
│       │   └── Chat.vue              # 代码问答页
│       ├── components/               # 通用组件
│       │   └── NavBar.vue            # 导航栏
│       └── utils/
│           └── request.js            # Axios 封装
│
├── model-service/                    # Python 模型服务
│   ├── embedding_service.py          # BGE-M3 Embedding
│   ├── reranker_service.py           # BGE Reranker
│   └── requirements.txt              # Python 依赖
│
├── docs/                             # 文档
│   └── API.md                        # API 文档
│
└── README.md                         # 项目说明
```

## 配置说明

### application.yml

```yaml
# 服务端口
server:
  port: 9400

# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/github_search
    username: root
    password: your_password

  # Redis 配置
  data:
    redis:
      host: 127.0.0.1
      port: 6379

# GitHub API 配置
github:
  api:
    base-url: https://api.github.com
    token: ${GITHUB_TOKEN:}
    connect-timeout: 30
    read-timeout: 60

# 模型服务配置
model:
  embedding:
    url: http://127.0.0.1:8000
  reranker:
    url: http://127.0.0.1:8001

# LLM 配置
llm:
  api-key: your_qwen_api_key
  base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
  model: qwen3-max
```

## 开发命令

### 后端

```bash
# 编译
./mvnw compile

# 运行
./mvnw spring-boot:run

# 打包
./mvnw package

# 测试
./mvnw test
```

### 前端

```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview
```

### 模型服务

```bash
# 安装依赖
pip install -r requirements.txt

# 启动 Embedding 服务 (端口 8000)
python embedding_service.py

# 启动 Reranker 服务 (端口 8001)
python reranker_service.py
```

## 扩展开发

### 添加新的 API 接口

1. 在 `controller/` 下创建新的 Controller
2. 在 `service/` 下实现业务逻辑
3. 在 `mapper/` 下添加数据访问（如需要）
4. 在 `entity/` 下定义实体类

### 添加新的前端页面

1. 在 `views/` 下创建 Vue 组件
2. 在 `router/index.js` 添加路由
3. 在 `NavBar.vue` 添加导航链接

### 添加新的模型服务

1. 在 `model-service/` 下创建 Python 文件
2. 使用 FastAPI 创建 REST 接口
3. 在后端 `ModelClient.java` 中添加调用方法

## 部署

### Docker 部署 (推荐)

```bash
# 构建镜像
docker build -t github-search-backend ./backend
docker build -t github-search-frontend ./frontend
docker build -t github-search-model ./model-service

# 运行容器
docker-compose up -d
```

### 传统部署

1. 打包后端: `./mvnw package`
2. 构建前端: `npm run build`
3. 部署到服务器

## 常见问题

### Q: GitHub API 限流怎么办？

A: 配置 GitHub Token 可以提高限流阈值（从 60/h 到 5000/h）。

### Q: 模型加载慢怎么办？

A: 首次启动会下载 BGE-M3 模型（约 2GB），可以预先下载到 `~/.cache/huggingface/`。

### Q: 数据库连接失败？

A: 检查 MySQL 服务是否启动，用户名密码是否正确。

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add amazing feature'`)
4. 推送分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request
