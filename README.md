# ML_Platform

一个基于 **SpringCloud** 的机器学习平台后端工程，致力于构建多功能、可扩展的 AI 服务支撑平台。

> 💡 技术栈：SpringCloud + SpringBoot3.x · Nacos · Mybatis-Plus · Redis · MySQL · Kafka · Elasticsearch · Nginx · Langchain4j · MongoDB

---
> ![SpringBoot](https://img.shields.io/badge/SpringBoot-3.x-brightgreen?logo=spring-boot)
> ![SpringCloud](https://img.shields.io/badge/SpringCloud-2023-blue?logo=spring)
> ![Nacos](https://img.shields.io/badge/Nacos-Config-blueviolet?logo=apache)
> ![MyBatis--Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.x-important)
> ![Redis](https://img.shields.io/badge/Redis-Cache-red?logo=redis)
> ![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)
> ![Kafka](https://img.shields.io/badge/Kafka-Streaming-black?logo=apachekafka)
> ![Elasticsearch](https://img.shields.io/badge/Elasticsearch-Search-orange?logo=elasticsearch)
> ![Nginx](https://img.shields.io/badge/Nginx-ReverseProxy-brightgreen?logo=nginx)
> ![LangChain4j](https://img.shields.io/badge/LangChain4j-AI%20Agent-yellow)
> ![MongoDB](https://img.shields.io/badge/MongoDB-NoSQL-brightgreen?logo=mongodb)

---

## 🧱 项目结构

本项目各个模块如下所示：

- **`agent/`：AI陪伴服务（施工中） **
  - 基于SenseVoice的语音识别，可以识别用户情绪
  - 自动更新记忆，实现个性化


- **`ai-game/`：AI 体验服务模块**
  - 基于 Kafka + Python 构建模拟大模型提示词攻击靶场
  - 提供与 Ollama 模型的 API 交互（通过 WebSocket 实现）

- **`ai-service/`：AI 服务模块**
  - 基于SpringAi实现的交流模块，支持长期记忆存储与刷新记忆
  - 基于LangChain4j实现的平台智能助手「小依」

- **`auth-server/`：认证服务模块**
  - 使用 Session 实现用户登录鉴权
  - 支持基于 OAuth2.0 的 GitHub 第三方登录功能

- **`blog/`：博客服务模块**
  - 支持博客发布、展示、点赞、评论等功能
  - 集成 Elasticsearch + IK 分词，实现全文关键词搜索

- **`common/`：公共模块**
  - 封装通用工具类、枚举、响应对象、异常处理等

- **`gateway/`：API 网关模块**
  - 使用 SpringCloud Gateway 统一接入路由
  - 集成 Sentinel 实现限流、熔断、降级
  - 支持 Zipkin 链路追踪

- **`member/`：会员服务模块**
  - 管理用户信息、登录状态、积分系统等

- **`third-party/`：第三方服务模块**
  - 封装阿里云 OSS 文件上传、短信验证码等功能接口

---

## 🚀 快速开始

### 环境要求

- JDK 8+
- Maven 3.6+
- ElasticSearch 7.6.2
- Nacos 2.2.3

### 克隆项目

```bash
git clone https://github.com/ZixinYan/ML_Platform.git
```

### 🔧 配置环境

- 本项目使用 **Nacos** 作为注册中心与配置中心
- 所有服务配置（如数据库、Redis、OSS 等）均已托管在 Nacos 中
- 如需本地运行，请联系作者或在 Issue 中留言获取示例配置文件

---

## 🛠️ 项目未来规划
🚀 **业务相关计划**

🔲 完善微信登录和微博登录功能

🔲 在 Ai-game 服务添加更多有意思的 Ai 体验

🔲 支持同学上传源码（在线运行？）

🔲 在 agent添加更多功能，语音识别等

✅ 通过实现本地知识库的制作

✅ 实现平台智能助手

✅ 添加RAG增强检索和Embedding数据库

🔲 添加代码商城功能，集成高并发秒杀

🔲 为智能助手添加记忆更新功能，以blog等数据为知识实时更新

🔲 为代码商城添加下单支付模块，添加风控系统

🚀 **系统功能计划**

✅ 升级整体架构至 Spring Boot 3.x

✅ 引入 Spring AI

🔲 通过 Spring security + 双token实现分权【现在是spring session】

🔲 微服务间通信使用rpc

🔲 使用db连接池与线程池减少系统开销


---

## 📬 联系

如有建议或使用问题，欢迎提交 [Issues](https://github.com/ZixinYan/ML_Platform/issues) 或 Pull Requests 帮我改进一下项目捏

