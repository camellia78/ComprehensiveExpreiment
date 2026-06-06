# 学生宿舍管理系统 — 综合设计

## 一、需求分析

本系统服务于高校宿舍管理场景，核心角色为**管理员**和**学生**两类。

**管理员**：管理楼栋/寝室/床位信息，办理学生入住、调宿、退宿，处置报修工单，查看统计报表。
**学生**：查看个人信息，提交报修申请，查看维修进度。

## 二、技术架构

| 层 | 选型 |
|---|---|
| 架构 | B/S 前后端分离 |
| 数据库 | openGauss |
| 后端 | SpringBoot 2.7 + MyBatis-Plus 3.5 |
| 前端 | Vue 3 + Element Plus + Axios |
| 认证 | JWT（access token，过期 2h） |
| 主键 | 雪花算法（MyBatis-Plus IdType.ASSIGN_ID） |

## 三、数据库设计

### 3.1 ER 图（概念结构）

管理员 ——1:N—— 维修处置记录 ——N:1—— 报修单 ——N:1—— 学生（提交报修）
学生 ——1:N—— 入住记录 ——N:1—— 床位 ——N:1—— 寝室 ——N:1—— 楼栋
学生 ——1:N—— 调宿记录（关联新旧床位/寝室）
学生 ——1:N—— 退宿记录 ——N:1—— 入住记录

关系：
- 楼栋 : 寝室 = 1 : N
- 寝室 : 床位 = 1 : N
- 床位 : 入住记录 = 1 : N
- 学生 : 入住记录 = 1 : N
- 学生 : 调宿记录 = 1 : N
- 学生 : 退宿记录 = 1 : N
- 学生 : 报修单 = 1 : N
- 报修单 : 维修处置记录 = 1 : N

### 3.2 逻辑结构（关系模式）

所有表均包含公共字段：id BIGINT PRIMARY KEY（雪花ID）、is_deleted TINYINT DEFAULT 0、create_time DATETIME、update_time DATETIME。

sys_user(id, username, password, real_name, role, student_no, phone, gender)
dorm_building(id, name, building_no, floors, building_type)
dorm_room(id, room_no, building_id, floor, total_beds, occupied_beds, status)
dorm_bed(id, bed_no, room_id, status)
accom_checkin(id, student_id, bed_id, room_id, building_id, checkin_time, status)
accom_transfer(id, student_id, old_bed_id, old_room_id, new_bed_id, new_room_id, transfer_time, reason)
accom_checkout(id, student_id, checkin_id, checkout_time, reason)
repair_request(id, student_id, building_id, room_id, description, status, create_time)
repair_process(id, request_id, admin_id, action, remark, process_time)

规范化：所有表满足 3NF。

### 3.3 物理结构

索引：
- sys_user: uk_username(username), uk_student_no(student_no)
- dorm_room: idx_building_id(building_id)
- dorm_bed: idx_room_id(room_id), idx_status(status, is_deleted)
- accom_checkin: idx_student_id(student_id), idx_bed_id(bed_id)
- repair_request: idx_student_id(student_id), idx_status(status, is_deleted)
- repair_process: idx_request_id(request_id)

### 3.4 安全性设计

- SQL注入防护：MyBatis-Plus 默认参数化查询
- 认证：JWT Token + 拦截器校验
- 密码：BCrypt 加盐哈希
- 权限：角色级别拦截，接口级别 RoleCheck 注解
- 完整性：外键约束 + CHECK 约束 + 业务层事务

## 四、项目结构

dorm-server/ (SpringBoot)
  config/ controller/ service/ mapper/ entity/ dto/ common/ interceptor/

dorm-web/ (Vue3)
  views/ (login/ dashboard/ dorm/ accom/ repair/ system/)
  router/ api/ stores/ utils/

## 五、API 接口

登录 POST /api/auth/login 公开
楼栋CRUD /api/buildings 管理员
寝室CRUD /api/rooms 管理员
床位查询 GET /api/rooms/:id/beds 管理员
空闲床位 GET /api/buildings/:id/free-beds 管理员
入住 POST /api/checkins 管理员
入住修改 PUT /api/checkins/:id 管理员
调宿 POST /api/transfers 管理员
退宿 POST /api/checkouts 管理员
我的报修 POST/GET /api/repairs 学生
全部报修 GET /api/repairs/all 管理员
维修处置 PUT /api/repairs/:id/process 管理员
个人信息 GET /api/profile 全部

## 六、前端页面

登录 /login 公开
工作台 /dashboard 全部
楼栋管理 /dorm/buildings 管理员
寝室管理 /dorm/rooms 管理员
床位查看 /dorm/rooms/:id/beds 管理员
入住登记 /accom/checkin 管理员
调宿处理 /accom/transfer 管理员
退宿登记 /accom/checkout 管理员
报修申请 /repair/my 学生
工单处置 /repair/manage 管理员
用户管理 /system/users 管理员
个人中心 /profile 全部
