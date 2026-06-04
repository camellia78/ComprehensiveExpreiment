# 学生宿舍管理系统

## 环境要求

| 工具 | 版本 | 用途 |
|------|------|------|
| JDK | 21（推荐 Eclipse Temurin） | 后端运行 |
| Maven | 3.8+ | 后端构建 |
| Node.js | 18+ | 前端构建 |
| npm | 9+（随 Node 自带） | 前端依赖管理 |
| Docker & Docker Compose | 最新版 | 数据库（openGauss） |
| Git | 任意版本 | 代码管理 |

> Windows 用户注意：JDK 21 不支持直接在系统 PATH 中配置，建议通过 `JAVA_HOME` 环境变量指定路径，或使用 SDKMAN、Chocolatey 等工具管理。

---

## 1. 克隆代码

```bash
git clone https://github.com/camellia78/EXP.git
cd EXP
```

---

## 2. 数据库（Docker + openGauss）

确保 Docker Desktop 已启动并运行。

```bash
# 启动 openGauss 容器，自动建库并导入初始数据
docker compose up -d

# 验证数据库是否正常（容器状态为 healthy）
docker ps --filter name=dorm
```

初始化数据包含：

| 表 | 初始数据 |
|----|----------|
| sys_user | admin(管理员)、zhangsan(学生)、lisi(学生) |
| dorm_building | Bldg 1 (6层)、Bldg 2 (6层) |
| dorm_room | 101/102(Bldg 1)、201(Bldg 2)，含床位数据 |

数据库参数（`docker-compose.yml` 中配置）：

| 参数 | 值 |
|------|-----|
| 主机 | localhost |
| 端口 | 15432 |
| 数据库名 | dormitory |
| 用户名 | gaussdb |
| 密码 | Enmo@123 |

---

## 3. 后端

### 3.1 安装 JDK 21

**Windows：**

1. 下载 [Eclipse Temurin JDK 21](https://adoptium.net/temurin/releases/?version=21)（选 Windows x64 MSI 安装包）
2. 安装后设置环境变量：

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.x.x-hotspot"
```

**macOS / Linux：**

```bash
# macOS（Homebrew）
brew install openjdk@21

# Linux（apt）
sudo apt install openjdk-21-jdk
```

### 3.2 启动后端

```bash
cd dorm-server
mvn spring-boot:run -q
```

首次启动会自动下载 Maven 依赖。启动成功后控制台输出：

```
Started DormApplication in 1.2 seconds
```

验证后端运行：

```bash
curl http://localhost:8080/api/auth/login -X POST -H "Content-Type: application/json" -d '{"username":"admin","password":"123456"}'
```

返回包含 token 的 JSON 即为正常：

```json
{"code":200,"message":"success","data":{"token":"eyJ...","role":0,"username":"admin"}}
```

### 3.3 后端配置说明

配置文件 `dorm-server/src/main/resources/application.yml`：

```yaml
server:
  port: 8080

spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:15432/dormitory
    username: gaussdb
    password: Enmo@123

jwt:
  secret: dormitory-system-secret-key-2026
  expiration: 7200000   # 2小时
```

> 连接数据库时使用 `jdbc:postgresql://` 前缀而非 `jdbc:opengauss://`，因为实际使用的是 PostgreSQL JDBC 驱动。openGauss 兼容 PostgreSQL 协议。

---

## 4. 前端

### 4.1 安装 Node.js

下载 [Node.js 20 LTS](https://nodejs.org/) 并安装（npm 随附）。

验证安装：

```bash
node -v   # v20.x.x
npm -v    # 10.x.x
```

### 4.2 启动前端

```bash
cd dorm-web
npm install
npm run dev
```

启动成功输出：

```
VITE v5.x.x  ready in xxx ms
  ➜  Local:   http://localhost:3000/
```

浏览器打开 `http://localhost:3000` 即可进入登录页。

### 4.3 前端说明

- 开发模式下 Vite 自动代理 `/api` 请求到 `http://localhost:8080`（配置见 `vite.config.js`）
- 前端登录后 token 保存在 `localStorage`，每次 API 请求自动附带 `Authorization: Bearer <token>` 头

---

## 5. 一键部署（Docker Compose 生产环境）

项目根目录已配置完整的 Docker Compose 部署，三容器一键启动：

```bash
docker compose up -d --build
```

| 容器 | 端口 | 说明 |
|------|------|------|
| dorm-opengauss | 15432 | openGauss 数据库 |
| dorm-backend | 8080 | Spring Boot API |
| dorm-frontend | 80 | Nginx + Vue 前端 |

部署后直接访问服务器 IP 即可，Nginx 已配置 SPA fallback 和 `/api` 反向代理。

---

## 6. 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 学生 | zhangsan | 123456 |
| 学生 | lisi | 123456 |

---

## 7. 常见问题

### 7.1 JDBC 连接失败：Driver does not accept jdbcUrl

确保 `application.yml` 中 url 前缀是 `jdbc:postgresql://` 而非 `jdbc:opengauss://`。openGauss 兼容 PostgreSQL 协议，使用 PostgreSQL JDBC 驱动即可。

### 7.2 MySQL 还是 openGauss？

本项目默认使用 openGauss（通过 Docker 运行）。如果你更熟悉 MySQL，只需：
1. 启动一个 MySQL 容器
2. 修改 `application.yml` 的 `driver-class-name`、`url`、`username`、`password`
3. 修改 `init.sql` 语法为 MySQL 兼容

### 7.3 Git push 时报 DNS 解析失败

```bash
# Windows: 添加 hosts 记录
Add-Content -Path "$env:SystemRoot\System32\drivers\etc\hosts" -Value "`n20.205.243.166 github.com"
# 或刷新 DNS
ipconfig /flushdns
```

### 7.4 前端的验证码在哪儿？

本项目无验证码模块。登录只需要用户名和密码。

### 7.5 页面访问时显示 401 Unauthorized

- 确认已登录（前端 localStorage 中有 token）
- 确认后端启动且数据库正常运行
- 如果 jwt 生成时正常但拦截器返回 401，检查后端启动日志是否有异常栈

### 7.6 编译报错：com.sun.tools.javac.code.TypeTag :: UNKNOWN

原因为 Lombok 在 JDK 21 下需要 --add-opens JVM 参数。pom.xml 中 `maven-compiler-plugin` 已配置，确保使用 `mvn spring-boot:run`（而非直接 `java -jar`）启动。

### 7.7 宿舍/床位 ID 为什么是 19 位数字？

使用雪花算法（Snowflake ID）生成主键，确保分布式环境下 ID 唯一。前端接收到的 Long 类型 ID 已自动转为字符串，避免 JavaScript 精度丢失。

---

## 8. API 概览

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/auth/login | 登录 | 无需 |
| GET | /api/buildings | 楼栋列表 | 登录 |
| POST | /api/buildings | 新建楼栋 | 登录 |
| PUT | /api/buildings/{id} | 编辑楼栋 | 登录 |
| DELETE | /api/buildings/{id} | 删除楼栋（级联删除房间） | 登录 |
| GET | /api/rooms | 房间列表（分页） | 登录 |
| POST | /api/rooms | 新建房间（自动生成床位） | 登录 |
| PUT | /api/rooms/{id} | 编辑房间 | 登录 |
| DELETE | /api/rooms/{id} | 删除房间（级联删除床位） | 登录 |
| GET | /api/rooms/{id}/beds | 房间床位列表 | 登录 |
| GET | /api/buildings/{id}/free-beds | 楼栋空闲床位 | 登录 |
| GET | /api/checkins | 入住记录 | 登录 |
| POST | /api/checkins | 入住登记 | 登录 |
| POST | /api/transfers | 调寝 | 登录 |
| POST | /api/checkouts | 退宿 | 登录 |
| GET | /api/repairs | 学生报修列表 | 登录 |
| POST | /api/repairs | 提交报修 | 登录 |
| GET | /api/repairs/all | 全部报修（管理员） | 登录 |
| PUT | /api/repairs/{id}/process | 处理报修 | 登录 |
| PUT | /api/repairs/{id}/cancel | 取消报修 | 登录 |
| GET | /api/users | 用户列表 | 登录 |
| GET | /api/profile | 个人信息 | 登录 |
