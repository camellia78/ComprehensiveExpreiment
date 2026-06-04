# 学生宿舍管理系统

基于 B/S 架构的宿舍综合管理平台，前后端分离设计。

## 技术栈
- 后端：SpringBoot 2.7 + MyBatis-Plus 3.5
- 前端：Vue 3 + Element Plus + Axios
- 数据库：openGauss (Docker)
- 认证：JWT + BCrypt
- 主键：雪花算法 (MyBatis-Plus IdType.ASSIGN_ID)

## 功能模块
1. 登录鉴权：管理员/学生角色分离，路由权限守卫
2. 宿舍管控：楼栋 CRUD、寝室 CRUD、床位查询、空闲床位
3. 住宿业务：入住登记、调宿处理、退宿登记（事务保障）
4. 报修服务：学生提交报修、管理员处置工单、报修取消

## 快速启动

### 1. 启动 openGauss
```bash
docker-compose up -d
```
数据库自动创建 dormitory 库并执行 init.sql 初始数据。

### 2. 后端
```bash
cd dorm-server
mvn spring-boot:run
```

### 3. 前端
```bash
cd dorm-web
npm install
npm run dev
```

访问 http://localhost:3000

## 默认账号
- 管理员：admin / 123456
- 学生：zhangsan / 123456
- 学生：lisi / 123456
