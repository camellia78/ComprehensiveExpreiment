erDiagram
    系统用户 ||--o{ 入住记录 : "1 入住 n"
    系统用户 ||--o{ 退宿记录 : "1 退宿 n"
    系统用户 ||--o{ 调寝记录 : "1 调寝 n"
    系统用户 ||--o{ 报修工单 : "1 提交 n"
    系统用户 ||--o{ 报修处理记录 : "1 处理 n"
    宿舍楼   ||--o{ 宿舍房间 : "1 包含 n"
    宿舍楼   ||--o{ 入住记录 : "1 入住 n"
    宿舍房间 ||--o{ 床位     : "1 包含 n"
    宿舍房间 ||--o{ 入住记录 : "1 入住 n"
    床位     ||--o{ 入住记录 : "1 占用 n"
    入住记录 ||--|| 退宿记录 : "1 对应 1"
    报修工单 ||--o{ 报修处理记录 : "1 流水 n"

    系统用户 {
        bigint 用户编号 PK "雪花ID"
        varchar 登录账号 UK
        varchar 登录密码
        varchar 真实姓名
        int 角色 "0管理员 1学生"
        varchar 学号 UK "学生唯一"
        varchar 联系电话
        int 性别 "0女 1男"
    }

    宿舍楼 {
        bigint 楼栋编号 PK
        varchar 楼栋名称
        varchar 楼栋代号
        int 总楼层 "默认6"
        int 楼栋类型 "0男楼 1女楼"
    }

    宿舍房间 {
        bigint 房间编号 PK
        varchar 门牌号 "如101"
        bigint 所属楼栋 FK
        int 所在楼层
        int 床位总数 "默认4"
        int 已入住床位 "≤总数"
        int 房间状态 "0可用"
    }

    床位 {
        bigint 床位编号 PK
        varchar 床位序号 "A B C D"
        bigint 所属房间 FK
        int 床位状态 "0空闲 1占用"
    }

    入住记录 {
        bigint 入住编号 PK
        bigint 学生编号 FK
        bigint 床位编号 FK
        bigint 房间编号 FK
        bigint 楼栋编号 FK
        timestamp 入住时间
        int 状态 "1在住 0已退"
    }

    退宿记录 {
        bigint 退宿编号 PK
        bigint 学生编号 FK
        bigint 入住编号 FK
        timestamp 退宿时间
        varchar 退宿原因
    }

    调寝记录 {
        bigint 调寝编号 PK
        bigint 学生编号 FK
        bigint 原床位编号
        bigint 原房间编号
        bigint 新床位编号
        bigint 新房间编号
        timestamp 调寝时间
        varchar 调寝原因
    }

    报修工单 {
        bigint 工单编号 PK
        bigint 学生编号 FK
        bigint 楼栋编号
        bigint 房间编号
        varchar 故障描述
        varchar 故障类型 "水电 家具 门窗 网络 其他"
        int 紧急程度 "0普通 1紧急"
        int 工单状态 "0待处理 1处理中 2已完成 3已取消"
    }

    报修处理记录 {
        bigint 记录编号 PK
        bigint 工单编号 FK
        bigint 处理人 FK "管理员"
        varchar 处理动作 "接单 维修完成 无法修复"
        varchar 备注
        timestamp 处理时间
    }
