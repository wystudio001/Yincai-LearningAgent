# 因材智学系统 — 后端工程

## 技术栈

- **Java 17** + **Spring Boot 3.4** + **Spring Cloud Gateway**
- **Gradle** 多模块构建
- **MyBatis Plus** + **MySQL 8.0**
- **RabbitMQ** (异步任务)
- **MinIO** (对象存储)

## 项目结构

```
JavaProject/
├── learning-common/              # 公共模块
│   ├── common-core/              # 统一响应、错误码、异常、基础实体
│   ├── common-web/               # Web 配置、全局异常处理、分页
│   ├── common-security/          # JWT、认证上下文、用户持有
│   ├── common-mybatis/           # MyBatis Plus 配置、字段填充
│   ├── common-mq/                # 消息队列事件模型
│   ├── common-file/              # 文件信息模型
│   ├── common-ai/                # AI 调用请求/响应模型
│   └── common-observability/     # 链路追踪 traceId
├── learning-gateway/             # API 网关 (port 8080)
├── learning-auth-service/        # 认证服务 (port 8081)
├── learning-user-service/        # 用户组织服务 (port 8082)
├── learning-course-service/      # 课程知识图谱服务 (port 8083)
├── learning-profile-service/     # 学习画像服务 (port 8084)
├── learning-resource-service/    # 资源管理服务 (port 8085)
├── learning-path-service/        # 学习路径服务 (port 8086)
├── learning-ai-service/          # AI 编排服务 (port 8087)
├── learning-exam-service/        # 测评考试服务 (port 8088)
├── learning-notify-service/      # 通知服务 (port 8089)
├── scripts/
│   ├── init-db.sql               # 数据库初始化脚本
│   └── docker-compose-dev.yml    # 本地开发环境
└── docs/
```

## 快速开始

### 1. 启动依赖服务

```bash
cd scripts
docker-compose -f docker-compose-dev.yml up -d
```

### 2. 构建项目

```bash
# Windows
./gradlew build

# 全量编译
./gradlew clean build -x test
```

### 3. 启动服务

各服务可独立启动，按以下顺序依次启动：

```bash
# 先启动网关
./gradlew :learning-gateway:bootRun

# 再启动业务服务
./gradlew :learning-auth-service:bootRun
./gradlew :learning-user-service:bootRun
./gradlew :learning-course-service:bootRun
./gradlew :learning-profile-service:bootRun
./gradlew :learning-resource-service:bootRun
./gradlew :learning-path-service:bootRun
./gradlew :learning-ai-service:bootRun
./gradlew :learning-exam-service:bootRun
./gradlew :learning-notify-service:bootRun
```

### 4. 验证

```bash
# 登录获取 Token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student01","password":"student123"}'

# 使用返回的 accessToken 访问业务接口
curl http://localhost:8080/api/v1/student/profile \
  -H "Authorization: Bearer <accessToken>"
```

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 平台管理员 |
| teacher01 | teacher123 | 教师 |
| student01 | student123 | 学生 |

## 数据库

MVP 阶段使用单 MySQL 实例多数据库模式：

| 数据库 | 所属服务 |
|--------|----------|
| learning_auth | 认证服务 |
| learning_user | 用户组织服务 |
| learning_course | 课程服务 |
| learning_profile | 画像服务 |
| learning_resource | 资源服务 |
| learning_path | 路径服务 |
| learning_exam | 测评服务 |
| learning_notify | 通知服务 |

## 架构分层

每个微服务采用 DDD 分层：

```
interfaces/     # Controller、请求响应 DTO、参数校验
application/    # 应用服务、用例编排、事务边界
domain/         # 聚合、实体、领域服务、领域事件
infrastructure/ # 数据库、MQ、远程调用实现
```

## 开发规范

- 统一响应格式：`R<T>` (code + message + data + traceId)
- JWT 认证：Bearer Token + Refresh Token
- 逻辑删除：所有表使用 `deleted` 字段
- 乐观锁：核心表使用 `version` 字段
- 雪花 ID：主键使用 ASSIGN_ID 策略
