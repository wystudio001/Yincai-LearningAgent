<h1 align="center">因材智学（Yincai Learning Agent）</h1>

<p align="center">
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-Apache--2.0-blue.svg" alt="License"></a>
  <a href="JavaProject"><img src="https://img.shields.io/badge/Spring%20Boot-3.4.7-6DB33F" alt="Spring Boot"></a>
  <a href="WebProject"><img src="https://img.shields.io/badge/Vue-3.5-42B883" alt="Vue"></a>
  <a href="AndroidProject"><img src="https://img.shields.io/badge/Android-SDK%2036-3DDC84" alt="Android"></a>
</p>

因材智学是面向高等教育场景的 AI 个性化学习平台。系统以学生画像为基础，连接学习资源、学习路径、练习评估与智能辅导，提供 Android 学生端、Web 教学管理端和 Java 后端。

## 系统架构

```text
Android 学生端 ─┐
                ├─ REST API / SSE ─ Java Spring 后端 ─ MySQL
Web 管理端 ─────┘                         ├─ RabbitMQ / MinIO
                                         └─ OpenAI-compatible 模型服务
```

当前后端采用 Gradle 多模块组织，以模块化单体方式启动，并保留向独立服务演进的模块边界。

## 功能特性

- 用户登录、JWT 会话、角色与权限管理
- 学生画像采集、查看与持续更新
- 个性化学习路径规划与节点管理
- 学习资源管理、检索、阅读、收藏与反馈
- AI 学习资源生成及任务进度跟踪
- 智能辅导会话、新建对话、历史消息与推荐问题
- SSE 流式回答与 Markdown 内容渲染
- 练习测评、错题记录与学习评估报告
- 课程、用户、资源、路径、通知、审核与数据分析管理
- OpenAI-compatible 模型供应商、API Key 和默认模型配置

## 技术栈

| 模块 | 主要技术 |
| --- | --- |
| Android 学生端 | Java、Android SDK、Material Components、Retrofit、OkHttp、Gson、CommonMark |
| Web 管理端 | Vue 3、TypeScript、Vite、Pinia、Vue Router、Element Plus、Axios、ECharts |
| Java 后端 | Java 17、Spring Boot 3.4.7、Gradle、MyBatis Plus、JWT、SSE |
| 基础设施 | MySQL 8、RabbitMQ、MinIO、Docker Compose |
| AI 接入 | OpenAI-compatible Chat Completions API |

## 项目结构

```text
yincai-learning-agent/
├── AndroidProject/   # Android 学生端
├── JavaProject/      # Java 后端与基础设施脚本
├── WebProject/       # Web 教学管理端
├── LICENSE
└── README.md
```

## 快速开始

### 环境要求

- JDK 17（Java 后端）
- Node.js 20 LTS 及 npm
- Docker 与 Docker Compose
- Android Studio、JDK 21、Android SDK 36（Android 端）

### 1. 启动基础设施

进入后端目录并启动 MySQL、RabbitMQ 和 MinIO：

```bash
cd JavaProject
docker compose --project-directory . -f scripts/docker-compose-dev.yml up -d
```

首次创建 MySQL 容器时会自动执行 `scripts/init-db.sql`。开发环境默认连接信息如下：

| 服务 | 地址或端口 | 默认凭据 |
| --- | --- | --- |
| MySQL | `localhost:3306` | `root / root` |
| RabbitMQ | `localhost:5672` | `guest / guest` |
| RabbitMQ 管理台 | `http://localhost:15672` | `guest / guest` |
| MinIO API | `http://localhost:9000` | `minioadmin / minioadmin` |
| MinIO 管理台 | `http://localhost:9001` | `minioadmin / minioadmin` |

停止基础设施：

```bash
docker compose --project-directory . -f scripts/docker-compose-dev.yml down
```

### 2. 启动 Java 后端

当前推荐从 `JavaProject` 根目录启动聚合应用，默认端口为 `8081`。

Windows：

```powershell
cd JavaProject
.\gradlew.bat bootRun
```

Linux / macOS：

```bash
cd JavaProject
./gradlew bootRun
```

构建后端：

```powershell
.\gradlew.bat clean build -x test
```

登录接口示例：

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student01","password":"student123"}'
```

### 3. 启动 Web 管理端

```bash
cd WebProject
npm install
npm run dev
```

浏览器访问 `http://localhost:5173`。开发环境默认请求 `http://127.0.0.1:8081/api/v1`，如需连接其他后端，请修改 `WebProject/.env.development`。

生产构建：

```bash
npm run build
```

### 4. 启动 Android 学生端

1. 使用 Android Studio 打开 `AndroidProject`。
2. 等待 Gradle 同步完成，选择 Android 10（API 29）及以上的模拟器或设备。
3. 确保 Java 后端已启动，然后运行 `app` 模块。
4. 在登录页右上角的设置按钮中填写后端地址。

Android Studio 模拟器使用：

```text
http://10.0.2.2:8081/
```

真实设备使用电脑的局域网地址，并确保设备与电脑处于同一网络：

```text
http://<电脑局域网IP>:8081/
```

命令行构建 Debug APK：

```powershell
cd AndroidProject
.\gradlew.bat assembleDebug
```

APK 输出目录为 `AndroidProject/app/build/outputs/apk/debug/`。

## 默认账号

| 用户名 | 密码 | 角色 |
| --- | --- | --- |
| `admin` | `admin123` | 平台管理员 |
| `teacher01` | `teacher123` | 教师 |
| `student01` | `student123` | 学生 |

## 配置大模型

1. 使用管理员账号登录 Web 管理端。
2. 进入“AI 配置”，新增 OpenAI-compatible 模型供应商。
3. 填写服务 `baseURL`、API Key 和默认模型并启用配置。
4. 使用 Android 学生端进入“辅导”页面，即可发起流式对话。

大模型密钥由 Java 后端统一管理，Android 端只连接本项目后端。

## 开发提示

- 当前配置用于本地开发，请勿在生产环境继续使用默认数据库密码、MinIO 密码或 JWT 密钥。
- Web 与 Android 端连接的后端地址必须指向运行中的 Java 服务。
- 若 Docker 中已存在旧的 MySQL 数据卷，初始化脚本不会重复执行，可在确认无需保留数据后重新创建数据卷。

## License

本项目基于 [Apache License 2.0](LICENSE) 开源。
