# ML_Platform

一个基于 **SpringCloud** 的机器学习平台后端工程，致力于构建多功能、可扩展的 AI 服务支撑平台。

> 💡 技术栈：SpringCloud + SpringBoot3.x · Nacos · Mybatis-Plus · Redis · MySQL · Kafka · Elasticsearch · Nginx

---

## 🧱 项目结构

本项目采用微服务架构，各个模块功能明确，职责分离：

- **`ai-game/`：AI 体验服务模块**  
  - 基于 Kafka + Python 构建模拟大模型提示词攻击靶场  
  - 提供与 Ollama 模型的 API 交互（通过 WebSocket 实现）
    
- **`ai-service/`：AI 服务模块**  
  - 基于SpringAi实现的与deepseek进行角色扮演游戏，支持长期记忆存储与刷新记忆
    

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

- 当前使用的是 Spring Boot 2.x 版本，由于 SpringAI 仅支持 3.x+，部分功能（如 Ollama 接入）使用 WebSocket 自行实现
- **未来计划：**
  - [√]升级整体架构至 Spring Boot 3.x
  - [√]引入 [Spring AI](https://spring.io/projects/spring-ai)
  - [ ]完善微信登录和微博登录功能
  - [ ]引入refresh_token做更安全的验证
  - [ ]在Ai-game服务添加更多有意思的Ai体验
  - [ ]支持同学上传源码（在线运行？），并提供通过积分进行下载源码的功能
  - [ ]在Ai-service的角色扮演模块添加语音生成，实现比较真实流畅的对话体验

---

## 📬 联系

如有建议或使用问题，欢迎提交 [Issues](https://github.com/ZixinYan/ML_Platform/issues) 或 Pull Requests 帮我改进一下项目捏

