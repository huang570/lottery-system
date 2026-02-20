## Lottery System 抽奖系统

一个基于 Spring Boot 的抽奖系统，包含活动管理、奖品管理、人员管理以及抽奖展示页面，前端采用静态 HTML + Bootstrap + jQuery，后端提供 REST 接口并集成短信验证码、邮件、Redis、RabbitMQ 等能力。

---

## 功能概览

- **用户管理**
    - **用户注册**：支持管理员与普通用户两种角色（`ADMIN` / `NORMAL`）
    - **登录方式**：
        - 密码登录 `/password/login`
        - 短信验证码登录 `/message/login`
    - **验证码发送**：通过 `/verification-code/send` 发送手机验证码
    - **用户列表查询**：根据身份查询基础用户信息 `/base-user/find-list`

- **活动管理**
    - **创建抽奖活动**：`/activity/create`
    - **活动列表查询**：`/activity/find-list`
    - **活动详情查询**：`/activity-detail/find`（含参与用户与奖品配置）

- **奖品管理**
    - **奖品创建**：支持图片上传 `/prize/create`
    - **奖品图片上传**：`/pic/upload`
    - **奖品列表查询**：`/prize/find-list`

- **抽奖与中奖记录**
    - **抽奖接口**：`/draw-prize`
    - **中奖记录查询**：`/winning-records/show`
    - 前端 `draw.html` 提供抽奖大屏展示、逐步开奖和中奖名单展示

- **管理后台前端页面（静态 HTML）**
    - `admin.html`：后台主界面（左侧菜单 + iframe 内容区）
    - `activities-list.html`：活动列表
    - `create-activity.html`：新建活动
    - `prizes-list.html`：奖品列表
    - `create-prizes.html`：创建奖品
    - `user-list.html`：人员列表
    - `register.html`：注册页面（根据 URL 参数区分管理员 / 普通用户）
    - `blogin.html`：后台登录页
    - `draw.html`：抽奖展示页面

---

## 技术栈

- **后端**
    - Java 17
    - Spring Boot 3.5.10
    - Spring Web
    - MyBatis（基于注解 Mapper）
    - MySQL（`lottery_system` 数据库）
    - Redis（缓存 / 验证码 / 活动抽奖信息等）
    - RabbitMQ（消息队列，用于异步处理）
    - JJWT（JWT 令牌）
    - Hutool 工具库
    - Spring Mail（邮件发送）

- **前端**
    - 静态 HTML + CSS
    - Bootstrap 4
    - jQuery
    - Toastr（消息提示）

---

## 环境准备

- **JDK**：17
- **Maven**：3.8+ 推荐
- **MySQL**：8.x（建议）
    - 创建数据库：`lottery_system`
- **Redis**：配置redis服务
- **RabbitMQ**：根据配置提供 MQ 服务
- **SMTP 邮箱**：用于邮件发送（可根据需要关闭或更改配置）
- **阿里云短信**：使用 dypnsapi（如不需要，可清理相关配置和调用）
---

## 配置说明（`application-dev.properties`）

主要配置项说明：

- **数据库**
    - **`spring.datasource.url`**：MySQL 连接地址
    - **`spring.datasource.username`**
    - **`spring.datasource.password`**

- **MyBatis**
    - **`mybatis.configuration.map-underscore-to-camel-case=true`**
    - **`mybatis.type-handlers-package`**：自定义 type handler（如 `EncryptTypeHandler`）

- **Redis**
    - **`spring.data.redis.host` / `port`**：Redis 地址与端口
    - 连接池与超时相关配置

- **图片上传**
    - **`pic.local-path`**：本地图片存储路径
    - **`spring.web.resources.static-locations`**：将该路径映射为静态资源访问

- **RabbitMQ**
    - **`spring.rabbitmq.host` / `port` / `username` / `password`**
    - 消费者重试、ack 模式等

- **邮件**
    - `spring.mail.*`：SMTP 服务器、账号、授权码、SSL 配置等

- **线程池**
    - `async.executor.thread.*`：异步线程池参数

---

## 启动项目

1. **配置 application-dev.properties**

    - 修改数据库、Redis、RabbitMQ、邮件、短信等配置为你本地或测试环境可用的值。
    - 确保 `spring.profiles.active=dev`（在 `application.properties` 中已设置）。

2. **初始化数据库**

    - 在 MySQL 中创建数据库：`lottery_system`
    - 根据你的 SQL 脚本创建表结构（项目中暂未附带建表 SQL，可根据实体和 Mapper 自行建表）。

3. **构建并启动**

   在项目根目录执行：

    - **方式一：直接运行**

      ```bash
      mvn spring-boot:run
      ```

    - **方式二：打包后运行**

      ```bash
      mvn clean package
      java -jar target/lottery-system-0.0.1-SNAPSHOT.jar
      ```

   默认端口为 `8080`（如未修改）。

---

## 前端页面入口

- **后台登录页**：  
  `http://localhost:8080/blogin.html`

- **后台管理页**（左侧菜单 + 内容区 iframe）：  
  `http://localhost:8080/admin.html`  
  - 活动管理：活动列表 / 新建抽奖活动
  - 奖品管理：奖品列表 / 创建奖品
  - 人员管理：人员列表 / 注册用户

- **注册页**：  
  - 管理员入口（从登录页“去注册”）：`register.html?admin=true`  
  - 普通用户入口（从后台侧边栏“注册用户”）：`register.html?admin=false&jumpList=true&param=...`

- **抽奖展示页**：  
  `http://localhost:8080/draw.html?activityId=xxx`  
  - 根据活动详情接口 `/activity-detail/find` 加载用户与奖品数据
  - 支持按奖项顺序抽取、展示中奖名单

---

## 接口概览（后端）

- **用户相关**
  - `POST /register`：用户注册
  - `POST /password/login`：密码登录
  - `POST /message/login`：短信验证码登录
  - `GET /verification-code/send`：发送验证码
  - `GET /base-user/find-list`：按身份查询基础用户列表

- **活动相关**
  - `POST /activity/create`：创建活动
  - `GET /activity/find-list`：分页查询活动列表
  - `GET /activity-detail/find`：查询活动详情（含奖品、人员）

- **奖品相关**
  - `POST /pic/upload`：上传奖品图片
  - `POST /prize/create`：创建奖品
  - `GET /prize/find-list`：分页查询奖品列表

- **抽奖相关**
  - `POST /draw-prize`：执行抽奖并记录中奖信息
  - `POST /winning-records/show`：按活动 / 奖项展示中奖记录

---

## 注意事项

- **安全**：
  - 当前注册逻辑中，通过登录页“去注册”默认会注册为管理员（`identity=ADMIN`），通过后台侧边栏“注册用户”则为普通用户（`identity=NORMAL`）。在对外环境使用前，请根据实际需要审查并调整该策略。
  - 不要在公开仓库中保留真实的数据库密码、短信密钥、邮件授权码等敏感信息。

- **前端调用**：
  - 部分前端 AJAX 调用目前仍为占位（如 `blogin.html` 的登录请求 URL、method 等），可以根据后端接口 `/password/login` 和 `/message/login` 自行完善。

