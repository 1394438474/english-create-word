# 智绘记 SmartVocab — AI 图文背单词系统

> 高端企业级、可直接用于优秀毕业设计的智能图文背单词 Web 系统。对标百词斩完整体验。
> Spring Boot 3 + Spring Security + JWT + Redis + MyBatis-Plus + MySQL 8 · Vue 3 + Vite + Pinia + Element Plus + ECharts

## 项目结构

```
英语单词/
├── backend/                    # SpringBoot3 后端
│   ├── src/main/java/com/smartvocab
│   │   ├── SmartVocabApplication.java
│   │   ├── common/             # 统一响应、异常、安全上下文
│   │   ├── config/             # Security / Redis / MyBatisPlus / WebMvc / TraceId
│   │   ├── security/           # JWT 工具 + 过滤器
│   │   └── module/
│   │       ├── auth/           # 登录注册
│   │       ├── user/           # 用户
│   │       ├── book/           # 词书
│   │       ├── word/           # 单词 + 释义 + 例句
│   │       ├── learn/          # 学习记录 + 复习队列
│   │       ├── errorbook/      # 错题
│   │       ├── vocab/          # 生词本
│   │       ├── quiz/           # 测验
│   │       ├── checkin/        # 打卡 + 勋章
│   │       ├── stat/           # 数据大屏
│   │       ├── algorithm/      # ⭐ 艾宾浩斯优化算法引擎
│   │       └── upload/         # 头像上传
│   └── pom.xml
├── frontend/                   # Vue3 前端
│   ├── src
│   │   ├── api/                # Axios 封装 + 各模块 API
│   │   ├── components/Layout.vue
│   │   ├── router/             # 路由 + 守卫
│   │   ├── stores/             # Pinia
│   │   ├── styles/             # 全局样式 / 动画
│   │   ├── utils/request.ts
│   │   ├── views/              # 9 个页面：Login/Register/Dashboard/Books/BookDetail/Study/Review/ErrorBook/Quiz/Vocabulary/CheckIn/Profile
│   │   ├── App.vue
│   │   └── main.ts
│   ├── vite.config.ts
│   ├── package.json
│   └── tsconfig.json
├── db/
│   ├── smartvocab.sql          # 完整建库脚本 + 初始数据
│   └── er_diagram.md           # E-R 图说明
├── docker-compose.yml          # 一键编排：MySQL + Redis + 后端 + 前端
├── nginx.conf                  # 前端 Nginx 配置
└── README.md
```

## 核心亮点

### 1. 智能学习核心
- 全图文场景化记单词：每个单词配高清场景图、音标、释义、例句
- 三种记忆状态：熟悉 / 模糊 / 陌生
- 智能复习池：系统按艾宾浩斯算法自动筛选今日复习词

### 2. 自研艾宾浩斯优化算法（毕设核心答辩点）
文件：`backend/.../module/algorithm/EbbinghausEngine.java`

```
R = e^(-Δt / S)             记忆保留率
nextInterval = -S * ln(0.9) 下次间隔
S_new = S * (1 + α·(statusScore - 0.5))   稳定性更新
D_new = D + β·(1 - recallQuality)         难度更新
```

- 综合 停留时长、错题率、复习次数，使用指数平滑
- 输出：稳定性 S、难度 D、下次复习时间、置信度
- 真正"因人而异"的连续可微间隔模型，远胜传统 1/3/7/15/30 天固定表

### 3. 商业化词书体系
- 内置 7 大分类 / 26,600 词：四级(2,600) / 六级(5,500) / 考研(5,500) / 高中(3,500) / 初中(2,000) / 雅思(4,500) / 专升本(3,000)
- 每词配高清场景图、美/英音标、多级释义、真题例句
- 词书进度可视化
- 词书详情 + 词条预览

### 4. 高级用户系统
- JWT 无状态登录、BCrypt 密码加密、Spring Security 权限拦截
- 头像上传、资料修改、密码修改
- 学习档案：连续打卡、累计学习时长、掌握量统计

### 5. 智能测试与错题
- 英译汉 / 汉译英 / 选词填空 三大题型
- 随机组卷、自动判分
- 错题自动归类、薄弱词专项复习

### 6. 生词本 / 笔记 / 星级
- 富文本笔记
- 5 星评级
- 批量复习、批量清空、JSON 导出

### 7. ECharts 数据可视化大屏
- 4 个 KPI 卡 + 6 个图表
- 学习趋势折线图、能力雷达图、掌握度环形图、词书进度条、薄弱词排行

### 8. 打卡 / 勋章 / 日历
- 月历 + 30 天热力图
- 7 枚勋章（连续 / 累计 / 完美）

### 9. 响应式 & 移动端
- 桌面端：左侧固定导航 + 顶栏
- 移动端：底部 Tab Bar 自适应
- 全站响应式 CSS

### 10. 企业级规范
- 全局异常处理 + Logback MDC `traceId`
- 接口限流预留（Redis 滑动窗口）
- 统一响应结构 `R<T>{code, message, data}`
- CORS、参数校验、SQL 注入防护
- Dockerfile + docker-compose 一键部署

## 快速启动

### 一、准备环境
- MySQL 8.0+
- Redis 7+（可选，但推荐）
- JDK 17+、Maven 3.8+
- Node.js 18+、npm/pnpm

### 二、初始化数据库
```bash
mysql -uroot -p --default-character-set=utf8mb4 < db/smartvocab.sql
```
默认 root/root，端口 3306，库名 `smartvocab`。

> 初始化脚本包含 12 张表 + 7 本词书共 26,600 词（带音标、释义、例句），首次导入约需 30~60 秒。
> 若已有库，脚本会先 DROP 重建，确保环境干净。

### 三、启动后端
```bash
cd backend
mvn spring-boot:run
```
后端运行在 `http://localhost:8080/api`

### 四、启动前端
```bash
cd frontend
npm install
npm run dev
```
前端运行在 `http://localhost:5173`

### 五、登录
演示账号：`demo@smartvocab.com` / `123456`

## Docker 一键启动
```bash
docker compose up -d
```
访问 `http://localhost`

## 答辩讲解路线建议
1. 项目概述 → 业务价值（5min）
2. 架构总览 → 前后端分离 + 分层（5min）
3. **艾宾浩斯算法详解（15min 重点）**：
   - 公式推导
   - 与传统固定间隔的对比
   - 代码逐行讲解
4. 智能复习池调度逻辑（5min）
5. 数据大屏与可视化（5min）
6. 演示系统功能（5min）
7. 总结与展望（5min）

## 关键文件
- 算法核心：[EbbinghausEngine.java](backend/src/main/java/com/smartvocab/module/algorithm/EbbinghausEngine.java)
- 数据库脚本：[smartvocab.sql](db/smartvocab.sql)
- E-R 图：[er_diagram.md](db/er_diagram.md)
- 技术架构：[Technical_Architecture.md](.trae/documents/Technical_Architecture.md)
- PRD：[PRD.md](.trae/documents/PRD.md)

## License
仅供学习与毕设使用。
