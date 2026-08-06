

# ML_Platform

A backend project for a machine learning platform built on **SpringCloud**, dedicated to constructing a multifunctional and scalable AI service infrastructure.

> 💡 Tech Stack: SpringCloud + SpringBoot3.x · Nacos · Mybatis-Plus · Redis · MySQL · Kafka · Elasticsearch · Nginx · Langchain4j · MongoDB

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

## 🧱 Project Structure

The modules in this project are as follows:

- **`agent/`**: AI Companion Service (Under Construction)
  - Voice recognition based on SenseVoice, capable of identifying user emotions
  - Automatically updates memory for personalized experiences

- **`ai-game/`**: AI Experience Service Module
  - Built with Kafka + Python to simulate a prompt attack training ground for large language models
  - Provides API interaction with Ollama models (implemented via WebSocket)

- **`ai-service/`**: AI Service Module
  - Communication module implemented based on Spring AI, supporting long-term memory storage and memory refresh
  - Platform AI assistant "Xiao Yi" implemented based on LangChain4j

- **`auth-server/`**: Authentication Service Module
  - User login and authorization implemented using Sessions
  - Supports GitHub third-party login based on OAuth2.0

- **`blog/`**: Blog Service Module
  - Supports blog publishing, display, liking, commenting, etc.
  - Integrates Elasticsearch + IK Analyzer for full-text keyword search

- **`common/`**: Common Module
  - Encapsulates general utility classes, enums, response objects, exception handling, etc.

- **`gateway/`**: API Gateway Module
  - Unified access routing using SpringCloud Gateway
  - Integrates Sentinel for rate limiting, circuit breaking, and degradation
  - Supports Zipkin for distributed tracing

- **`member/`**: Member Service Module
  - Manages user information, login status, point systems, etc.

- **`third-party/`**: Third-Party Service Module
  - Encapsulates interfaces for Alibaba Cloud OSS file uploads, SMS verification codes, etc.

---

## 🚀 Quick Start

### Environment Requirements

- JDK 8+
- Maven 3.6+
- ElasticSearch 7.6.2
- Nacos 2.2.3

### Clone the Project

```bash
git clone https://github.com/ZixinYan/ML_Platform.git
```

### 🔧 Configure Environment

- This project uses **Nacos** as the registry and configuration center
- All service configurations (such as database, Redis, OSS, etc.) are hosted in Nacos
- If you need to run locally, please contact the author or leave a message in the Issues to obtain example configuration files

---

## 🛠️ Project Future Roadmap
🚀 **Business-Related Plans**

🔲 Implement WeChat and Weibo login functionalities

🔲 Add more interesting AI experiences to the Ai-game service

🔲 Support students uploading source code (online execution?)

🔲 Add more features to the agent, such as voice recognition, etc.

✅ Completed local knowledge base creation implementation

✅ Implemented platform AI assistant

✅ Added RAG (Retrieval-Augmented Generation) and Embedding database support

🔲 Add a code marketplace feature, integrating high-concurrency flash sales

🔲 Add memory update functionality to the AI assistant to update knowledge in real-time using data like blogs

🔲 Add order payment module and risk control system for the code marketplace

🚀 **System Functionality Plans**

✅ Upgraded overall architecture to Spring Boot 3.x

✅ Introduced Spring AI

🔲 Implement permission separation using Spring Security + dual tokens [Currently using Spring Session]

🔲 Use RPC for inter-service communication

🔲 Use DB connection pools and thread pools to reduce system overhead


---

## 📬 Contact

If you have any suggestions or usage questions, feel free to submit [Issues](https://github.com/ZixinYan/ML_Platform/issues) or Pull Requests to help improve the project.
