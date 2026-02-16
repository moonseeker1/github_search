# API 文档

## 基础信息

- **Base URL**: `http://localhost:9400/api`
- **Content-Type**: `application/json`

---

## 搜索接口

### 1. 搜索仓库

搜索 GitHub 仓库。

**请求**
```
GET /search/repositories
```

**参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| query | string | 是 | 搜索关键词 |
| language | string | 否 | 编程语言过滤 (java, python, javascript 等) |
| page | int | 否 | 页码，默认 1 |
| perPage | int | 否 | 每页数量，默认 20 |

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 1000,
    "items": [
      {
        "id": 123456,
        "name": "repo-name",
        "fullName": "owner/repo-name",
        "owner": "owner",
        "description": "Repository description",
        "language": "Java",
        "stars": 1000,
        "forks": 200,
        "url": "https://github.com/owner/repo-name",
        "avatar": "https://avatars.githubusercontent.com/u/xxx"
      }
    ]
  }
}
```

### 2. 获取仓库详情

**请求**
```
GET /search/repository/{owner}/{repo}
```

**响应**
```json
{
  "code": 200,
  "data": {
    "id": 123456,
    "name": "repo-name",
    "fullName": "owner/repo-name",
    "description": "Description",
    "language": "Java",
    "stars": 1000,
    "forks": 200,
    "url": "https://github.com/owner/repo-name",
    "cloneUrl": "https://github.com/owner/repo-name.git",
    "readme": "# README content...",
    "topics": ["spring", "java"]
  }
}
```

### 3. 获取文件结构

**请求**
```
GET /search/repository/{owner}/{repo}/files?path=
```

**参数**
| 参数 | 类型 | 说明 |
|------|------|------|
| path | string | 目录路径，空字符串表示根目录 |

**响应**
```json
{
  "code": 200,
  "data": [
    {
      "name": "src",
      "path": "src",
      "type": "dir",
      "size": 0
    },
    {
      "name": "README.md",
      "path": "README.md",
      "type": "file",
      "size": 1024
    }
  ]
}
```

### 4. 获取文件内容

**请求**
```
GET /search/repository/{owner}/{repo}/file?path=src/main.java
```

**响应**
```json
{
  "code": 200,
  "data": {
    "path": "src/main.java",
    "content": "public class Main {...}",
    "language": "java"
  }
}
```

### 5. 生成仓库摘要

**请求**
```
GET /search/repository/{owner}/{repo}/summary
```

**响应**
```json
{
  "code": 200,
  "data": "这是一个基于 Spring Boot 的项目，主要功能包括..."
}
```

### 6. 搜索代码

**请求**
```
GET /search/code?query=function&language=python
```

**参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| query | string | 是 | 搜索关键词 |
| language | string | 否 | 编程语言 |
| page | int | 否 | 页码 |
| perPage | int | 否 | 每页数量 |

---

## RAG 接口

### 1. 索引仓库

将仓库代码索引到向量数据库。

**请求**
```
POST /rag/index
```

**Body**
```json
{
  "owner": "owner-name",
  "repo": "repo-name",
  "repoId": 123456
}
```

**响应**
```json
{
  "code": 200,
  "data": "Indexed 50 code files"
}
```

### 2. 代码问答

对已索引的仓库进行智能问答。

**请求**
```
POST /rag/ask
```

**Body**
```json
{
  "query": "如何实现用户登录功能？",
  "repoId": 123456,
  "topK": 5
}
```

**响应**
```json
{
  "code": 200,
  "data": {
    "answer": "根据代码分析，用户登录功能是通过...",
    "sources": [
      {
        "chunkId": 1,
        "text": "// 代码片段...",
        "score": 0.95
      }
    ]
  }
}
```

### 3. 查找相似代码

**请求**
```
POST /rag/similar
```

**Body**
```json
{
  "code": "public void login(String user, String pass) {...}",
  "topK": 10
}
```

**响应**
```json
{
  "code": 200,
  "data": [
    {
      "chunkId": 1,
      "text": "// 相似代码...",
      "score": 0.92,
      "repoId": 123456
    }
  ]
}
```

### 4. 代码摘要

**请求**
```
POST /rag/summarize
```

**Body**
```json
{
  "code": "public class UserService {...}",
  "language": "java"
}
```

**响应**
```json
{
  "code": 200,
  "data": "该类提供用户管理功能，包括用户注册、登录和信息查询..."
}
```

### 5. 检查索引状态

**请求**
```
GET /rag/indexed/{repoId}
```

**响应**
```json
{
  "code": 200,
  "data": true
}
```

### 6. 清除索引

**请求**
```
DELETE /rag/index/{repoId}
```

---

## 模型服务接口

### Embedding 服务 (端口 8000)

**获取向量**
```
POST /embedding
```

**Body**
```json
{
  "texts": ["文本1", "文本2"],
  "normalize": true
}
```

**响应**
```json
{
  "embeddings": [[0.1, 0.2, ...], [0.3, 0.4, ...]],
  "dimension": 1024,
  "model": "BAAI/bge-m3"
}
```

### Reranker 服务 (端口 8001)

**重排序**
```
POST /rerank
```

**Body**
```json
{
  "query": "搜索词",
  "documents": ["文档1", "文档2"],
  "top_k": 10
}
```

**响应**
```json
{
  "results": [
    {"index": 0, "score": 0.95, "document": "文档1"},
    {"index": 1, "score": 0.82, "document": "文档2"}
  ],
  "model": "BAAI/bge-reranker-v2-m3"
}
```

---

## 错误响应

```json
{
  "code": 500,
  "message": "错误描述",
  "data": null,
  "timestamp": 1708000000000
}
```

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
