# AICalendarMemo — AI 日程助手

一个集**日程管理、纪念日提醒、AI 智能助手**于一体的全栈应用。用户通过 UniApp 前台管理个人日程与纪念日，与 AI 对话即可直接查/建日程（Function Calling）；管理员通过 Vue3 后台管理全局用户与业务数据，并可使用可拖拽的全局 AI 智能体查询全部用户数据。

## 技术栈

| 端 | 技术 |
|---|---|
| 后端 | SpringBoot 3.4.1 · JDK 17 · MyBatis-Plus 3.5.16 · PageHelper · Spring AI 1.0.0 · WebSocket · Redis · MySQL · JWT |
| 前台 | UniApp · Vue3（script setup）· 原生组件 |
| 后台 | Vue3 · Element-Plus · Pinia · Vue-Router · Axios · ECharts · Vite · SCSS |

## 项目结构

```
AICalendarMemo/
├── schedule-server/ai-schedule/       # SpringBoot 后端服务（端口 8080）
│   └── src/main/java/com/aicalendar/
│       ├── controller/app/            # 前台接口 /api/**
│       ├── controller/admin/          # 后台接口 /api/admin/**（@RequireRole ADMIN）
│       ├── service/ai/                # AI @Tool 工具服务（AiToolService）
│       ├── websocket/                 # WebSocket 处理器 + 握手鉴权拦截器
│       ├── interceptor/               # JwtInterceptor、RoleAspect
│       ├── config/                    # AiConfig、JacksonConfig、WebSocketConfig 等
│       ├── entity / dto / mapper / common / util
│   └── src/main/resources/
│       ├── application.yml            # 主配置
│       └── mapper/                    # MyBatis XML SQL
├── ai-schedule-front/                 # UniApp 前台
│   └── pages/                         # home / schedule / memorial / ai / profile / login
│   └── api/                           # 接口封装（utils/request.js 统一拦截）
├── ai-schedule-admin/                 # Vue3 后台管理端
│   └── src/views/                     # dashboard / user / schedule / memorial / chat / layout
│   └── src/components/AiAgentFloat.vue# 全局悬浮 AI 智能体
│   └── src/directives/                # v-draggable 自定义拖拽指令
└── AGENTS.md                          # 项目开发规范
```

## 核心功能

### 前台（UniApp）
- **首页**：今日日程、即将到来的纪念日聚合展示
- **日程管理**：按日/类型查询、搜索、增删改
- **纪念日管理**：类型（普通/恋爱/结婚/生日/其他）、倒数提醒、头像上传
- **AI 智能助手**：WebSocket 流式对话，AI 通过 SpringAI `@Tool` 直接查/建日程与纪念日，数据自动关联当前用户
- **个人中心**：资料编辑、统计数据

### 后台（Vue3 管理端）
- **登录认证**：JWT（含 userId + role，2 小时过期），路由守卫 + `meta.roles` 角色拦截
- **仪表盘**：近 7 日新增趋势、日程类型分布（ECharts）
- **用户管理**：分页查询、启用/禁用（二次确认）
- **日程/纪念日管理**：分页、详情、删除（二次确认）
- **AI 会话管理**：按会话聚合分页，查看完整消息记录
- **全局 AI 智能体**：悬浮窗（从屏幕中心平滑滑至右侧）、`v-draggable` 头部拖拽 + 边界限制、WebSocket 单例不随路由销毁、以 ADMIN 身份查询全部用户日程/纪念日

## 快速启动

### 环境要求
- JDK 17+、Maven 3.8+
- MySQL 8.x、Redis
- Node.js 20.19+ / 22.12+
- HBuilderX（运行 UniApp）或微信开发者工具

### 1. 数据库
创建数据库 `ai-schedule`（utf8mb4），执行初始化 SQL 建表（user / schedule / memorial / ai_chat / user_setting）。

### 2. 后端
```bash
cd schedule-server/ai-schedule
# 配置环境变量（或直接改 application.yml）
#   DS_PASSWORD        MySQL 密码
#   SILIFLOW_API_KEY   SiliconFlow API Key（AI 模型用）
#   JWT_SECRET         JWT 签名密钥
./mvnw spring-boot:run
```
服务启动于 `http://localhost:8080`。

### 3. 后台管理端
```bash
cd ai-schedule-admin
npm install
npm run dev
```
Vite 已代理 `/api`、`/ws` 到 `http://localhost:8080`，访问 `http://localhost:5173`，用 ADMIN 角色账号登录。

### 4. UniApp 前台
HBuilderX 导入 `ai-schedule-front`，`config/baseUrl.js` 配置后端地址，运行到浏览器/小程序/真机即可。

## 接口规范

- 统一返回体：`{ "code": 200, "message": "...", "data": {...} }`
- 分页返回：`{ list, total, page, pageSize }`（PageHelper 后端分页）
- 时间格式：`yyyy-MM-dd HH:mm:ss`（JacksonConfig 全局配置）

| 模块 | 路径前缀 | 说明 |
|---|---|---|
| 前台业务 | `/api/**` | 登录用户可用，JWT 校验，数据按 userId 隔离 |
| 后台管理 | `/api/admin/**` | 仅 ADMIN，JwtInterceptor + RoleAspect 双重校验 |
| AI 聊天 | `ws://host/ws/ai/chat?token=xxx` | WebSocket 握手鉴权，流式返回 chunk/end/error 帧 |

主要前台接口：

```
POST /api/login                    登录（返回 token + 用户信息）
GET  /api/home                     首页聚合数据
GET  /api/schedule/date/{date}     按日期查日程
GET  /api/memorial/upcoming        即将到来的纪念日
GET  /api/profile                  用户信息 + 统计
WS   /ws/ai/chat                   AI 流式对话
```

## AI 智能体说明

- 基于 **Spring AI 1.0.0 Function Calling**，`@Tool` 方法由框架自动调度，不手动解析 tool_calls
- 用户上下文（userId / role）通过 **ToolContext** 注入，跨线程可靠，保证 AI 增查的数据与登录用户绑定
- 系统人设 Prompt 统一在 `AiPromptConfig`，模型为 SiliconFlow `Qwen/Qwen3-8B`
- 管理端智能体额外具备 `adminGetAllSchedulesByDate`、`adminGetAllMemorials` 两个 ADMIN 专属工具，非 ADMIN 角色调用返回无权限

## 安全说明

- 密码使用 BCrypt 加盐哈希存储
- JWT 仅含 userId + role，2 小时过期
- 后台接口 `@RequireRole({"ADMIN"})`，前端路由守卫仅做体验层拦截，权限以后端为准
- API Key、数据库密码等敏感信息均通过环境变量注入，不入库不入 Git
