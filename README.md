# Multi-User Calculator

一个支持多用户、基于角色权限控制的全栈计算器应用。用户可执行基本算术运算，管理员可管理用户权限并查看操作日志，同时支持 WebSocket 实时推送在线用户活动。

## 技术栈

### 后端 (calculator-backend)

| 技术 | 说明 |
|------|------|
| Spring Boot 3.5.1 | 应用框架 |
| Java 25 | 编程语言 |
| Spring Security | 认证与授权 |
| JWT (jjwt 0.12.6) | 无状态身份认证 |
| Spring Data JPA | 数据持久化 |
| H2 Database | 内存数据库（运行时） |
| WebSocket (STOMP) | 实时消息推送 |

### 前端 (calculator-frontend)

| 技术 | 说明 |
|------|------|
| Vue 3.5 | UI 框架 |
| Vite 8 | 构建工具 |
| Vue Router 4 | 路由管理 |
| Pinia 3 | 状态管理 |
| Axios | HTTP 客户端 |
| STOMP.js / SockJS | WebSocket 客户端 |

## 项目结构

```
├── calculator-backend/          # Spring Boot 后端
│   ├── src/main/java/com/calculator/
│   │   ├── config/              # 配置类（Security, CORS, WebSocket, SPA）
│   │   ├── controller/          # REST 控制器
│   │   ├── dto/                 # 数据传输对象
│   │   ├── model/               # JPA 实体与枚举
│   │   ├── repository/          # 数据访问层
│   │   ├── security/            # JWT 过滤器、令牌工具、认证实现
│   │   └── service/             # 业务逻辑层
│   ├── src/main/resources/
│   │   ├── application.yml      # 应用配置
│   │   └── static/              # 前端构建产物（SPA 静态资源）
│   └── pom.xml                  # Maven 依赖配置
├── calculator-frontend/         # Vue 3 前端
│   ├── src/
│   │   ├── api/                 # Axios API 封装
│   │   ├── router/              # 路由配置与导航守卫
│   │   ├── stores/              # Pinia 状态管理（auth, activity）
│   │   └── views/               # 页面组件
│   ├── public/                  # 静态资源
│   ├── vite.config.js           # Vite 配置
│   └── package.json             # NPM 依赖配置
└── README.md
```

## 功能特性

- **用户认证**：注册、登录、JWT 令牌管理
- **算术计算**：加、减、乘、除四则运算
- **角色权限**：USER（普通用户）和 ADMIN（管理员）两种角色
- **管理后台**：用户列表、角色修改、操作日志查看
- **实时活动**：WebSocket 推送在线用户及其最近操作
- **SPA 部署**：后端直接托管前端构建产物，单端口部署

## 快速开始

### 环境要求

- JDK 25
- Maven 3.6+
- Node.js 20+（仅前端开发调试需要）

### 启动后端（包含前端）

后端已内置前端构建产物，可直接运行：

```bash
cd calculator-backend
mvn spring-boot:run
```

启动后访问 `http://localhost:8080` 即可使用完整应用。

### 前端开发调试

如需独立开发前端，可启动 Vite 开发服务器：

```bash
cd calculator-frontend
npm install
npm run dev
```

前端开发服务器运行在 `http://localhost:5174`，API 和 WebSocket 请求会自动代理至后端 `http://localhost:8080`。

### 构建前端并更新后端静态资源

```bash
cd calculator-frontend
npm run build
cp -r dist/* ../calculator-backend/src/main/resources/static/
```

## 默认账户

应用首次启动时会自动创建以下账户：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | ADMIN |
| user1 | password123 | USER |
| user2 | password123 | USER |

## API 接口概要

### 认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录，返回 JWT |

### 计算器

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/calculator/calculate` | 执行算术运算（需认证） |

### 管理员

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/users` | 获取用户列表（ADMIN） |
| PUT | `/api/admin/users/{id}/role` | 修改用户角色（ADMIN） |
| GET | `/api/admin/logs` | 查看操作日志（ADMIN） |

### WebSocket

| 端点 | 说明 |
|------|------|
| `/ws` | STOMP WebSocket 端点 |
| `/topic/activity` | 订阅用户活动广播 |

## 应用配置

`application.yml` 中的关键配置项：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | 8080 | 服务端口 |
| `spring.datasource.url` | jdbc:h2:mem:calculatordb | H2 内存数据库 |
| `app.jwt.secret` | (Base64) | JWT 签名密钥 |
| `app.jwt.expiration-ms` | 3600000 | JWT 过期时间（1小时） |

生产环境请务必修改 JWT 密钥和数据库配置。