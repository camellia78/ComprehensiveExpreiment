# 学生宿舍管理系统

## 环境要求

| 工具 | 版本 | 用途 |
|------|------|------|
| JDK | 21（推荐 Eclipse Temurin） | 后端运行 |
| Maven | 3.8+ | 后端构建 |
| Node.js | 18+ | 前端构建 |
| npm | 9+（随 Node 自带） | 前端依赖管理 |
| Docker & Docker Compose | 最新版 | 数据库（PostgreSQL 15） |
| Git | 任意版本 | 代码管理 |

> Windows 用户注意：JDK 21 建议通过 `JAVA_HOME` 环境变量指定路径。

---

## 1. 克隆代码

```bash
git clone https://github.com/camellia78/ComprehensiveExpreiment.git
cd ComprehensiveExpreiment
```

---

## 2. 数据库（Docker + PostgreSQL）

确保 Docker Desktop 已启动并运行。

```bash
# 启动 PostgreSQL 容器，自动建库并导入初始数据
docker compose up -d database

# 验证数据库是否正常（容器状态为 healthy）
docker ps --filter name=dorm
```

初始化数据包含：

| 表 | 初始数据 |
|----|----------|
| sys_user | admin(管理员)、zhangsan(学生)、lisi(学生) |
| dorm_building | Bldg 1 (6层)、Bldg 2 (6层) |
| dorm_room | 101/102(Bldg 1)、201(Bldg 2)，共 12 个床位 |

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

**Windows：** 下载 [Eclipse Temurin JDK 21](https://adoptium.net/temurin/releases/?version=21) 并安装，然后设置：

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.x.x-hotspot"
```

**macOS / Linux：**

```bash
brew install openjdk@21    # macOS
sudo apt install openjdk-21-jdk   # Linux
```

### 3.2 启动后端

```bash
cd dorm-server
mvn spring-boot:run -q
```

首次启动会自动下载 Maven 依赖。启动成功后控制台输出 `Started DormApplication in ... seconds`。

验证后端运行：

```bash
curl http://localhost:8080/api/auth/login -X POST -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"123456\"}"
```

返回包含 token 的 JSON 即为正常。

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
```

---

## 4. 前端

### 4.1 安装 Node.js

下载 [Node.js 20 LTS](https://nodejs.org/) 并安装。

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

浏览器打开 `http://localhost:3000` 即可进入登录页。

---

## 5. 一键部署（Docker Compose 生产环境）

```bash
docker compose up -d --build
```

| 容器 | 端口 | 说明 |
|------|------|------|
| dorm-db | 15432 | PostgreSQL 15 数据库 |
| dorm-backend | 8080 | Spring Boot API |
| dorm-frontend | 80 | Nginx + Vue 前端 |

部署后直接访问服务器 IP 即可。

---

## 6. 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 学生 | zhangsan | 123456 |
| 学生 | lisi | 123456 |

---

## 7. 添加更多入住人员

### 方式一：通过页面逐个添加（推荐）

适合少量、零散添加，无需改代码。

1. 管理员登录系统
2. **系统管理 → 用户管理** → 添加学生账号（用户名、学号必填）
3. **基础数据 → 楼栋管理** → 添加楼栋（如有需要）
4. **基础数据 → 房间管理** → 添加房间和床位
5. **住宿管理 → 入住登记** → 选择学生、楼栋、房间、床位完成入住

### 方式二：修改 init.sql 批量添加

适合一次性大量添加。编辑 `dorm-server/src/main/resources/init.sql`，在末尾追加 INSERT 语句。

**添加学生用户：**

```sql
INSERT INTO sys_user(id, username, password, real_name, role, student_no, phone, gender, is_deleted)
VALUES (4, 'wangwu', '123456', '王五', 1, '2024003', '13800000003', 1, 0);
```

> - `role`: 0=管理员, 1=学生
> - `gender`: 0=女, 1=男
> - `is_deleted`: 固定为 0
> - 密码为明文 `123456`（后端自动加密）
> - `id` 为手动指定，和已有数据不冲突即可

**添加楼栋：**

```sql
INSERT INTO dorm_building(id, name, building_no, floors, building_type, is_deleted)
VALUES (3, 'Bldg 3', 'A03', 6, 0, 0);
```

**添加房间（会自动创建 4 个床位）：**

```sql
INSERT INTO dorm_room(id, room_no, building_id, floor, total_beds, occupied_beds, status, is_deleted)
VALUES (4, '103', 1, 1, 4, 0, 0, 0);
```

> 房间添加后还需手动插入床位。详见 `init.sql` 末尾的示例。

### 修改后部署

改完 init.sql 后，需重建数据库容器才能生效：

```bash
# 注意：这会清空所有已有的入住/调寝/报修数据！
docker compose down -v
docker compose up -d --build
```

> 如果已有正式入住数据，请勿直接改 init.sql。应通过页面操作或编写 SQL 迁移脚本。

---

## 8. 修改项目的注意事项

### 工作流程

```
本地开发 → Git 提交 → GitHub 推送 → 服务器拉取 → 重新部署
```

不需要先关闭服务器。`docker compose up -d --build` 会先构建新镜像，再自动替换旧容器。

### 前端修改后

```bash
docker compose up -d --build frontend
```
仅重建前端容器（几秒钟），不重启后端和数据库。

### 后端修改后

```bash
docker compose up -d --build backend
```
仅重建后端容器。Maven 依赖有缓存，首次修改会慢一些。

### 数据库变动

| 情况 | 操作 |
|------|------|
| 改 init.sql（新部署） | `docker compose down -v && docker compose up -d --build` |
| 改表结构（已有数据） | 写 SQL 迁移脚本，手动 `docker exec dorm-db psql ... ` 执行 |
| 仅加数据 | 页面操作, 或 `docker exec dorm-db psql ...` 执行 INSERT |

### 数据持久化

数据库文件保存在 Docker 卷 `exp_pgdata` 中。执行 `docker compose down -v` 会删除该卷导致数据丢失！仅在新部署、数据不重要时才加 `-v`。

### 内存限制

云服务器（1.6G 内存）同时运行三个容器会接近资源上限，不要同时重建多个服务。

---

## 9. 常见问题

### 9.1 JDBC 连接失败

确保 `application.yml` 中 url 前缀为 `jdbc:postgresql://`。

### 9.2 编译报错 com.sun.tools.javac.code.TypeTag :: UNKNOWN

pom.xml 中已配置 `--add-opens` 参数，使用 `mvn spring-boot:run` 启动即可。

### 9.3 Git push 时报 DNS 解析失败

```powershell
ipconfig /flushdns
```

### 9.4 页面访问显示 401

确认已登录、后端正常运行。检查浏览器控制台网络请求。

### 9.5 宿舍/床位 ID 为什么是 19 位数字？

使用雪花算法生成主键，避免分布式下 ID 冲突。前端 Long 已自动转字符串。

---

## 10. API 概览

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/auth/login | 登录 | 无需 |
| GET | /api/buildings | 楼栋列表 | 登录 |
| POST | /api/buildings | 新建楼栋 | 登录 |
| PUT | /api/buildings/{id} | 编辑楼栋 | 登录 |
| DELETE | /api/buildings/{id} | 删除楼栋 | 登录 |
| GET | /api/rooms | 房间列表（分页） | 登录 |
| POST | /api/rooms | 新建房间（自动生成床位） | 登录 |
| PUT | /api/rooms/{id} | 编辑房间 | 登录 |
| DELETE | /api/rooms/{id} | 删除房间 | 登录 |
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
