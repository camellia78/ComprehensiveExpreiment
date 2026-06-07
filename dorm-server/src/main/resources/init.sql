-- ========================================
-- 学生宿舍管理系统 - 完整初始化脚本
-- ========================================

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    role INT DEFAULT 1,
    student_no VARCHAR(30),
    phone VARCHAR(20),
    gender INT DEFAULT 0,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX uk_username ON sys_user(username) WHERE is_deleted = 0;
CREATE UNIQUE INDEX uk_student_no ON sys_user(student_no) WHERE is_deleted = 0;

CREATE TABLE dorm_building (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    building_no VARCHAR(20) NOT NULL,
    floors INT DEFAULT 6,
    building_type INT DEFAULT 0,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE dorm_room (
    id BIGINT PRIMARY KEY,
    room_no VARCHAR(20) NOT NULL,
    building_id BIGINT NOT NULL REFERENCES dorm_building(id),
    floor INT DEFAULT 1,
    total_beds INT DEFAULT 4,
    occupied_beds INT DEFAULT 0,
    status INT DEFAULT 0,
    repair_type VARCHAR(20) DEFAULT '其他',
    urgency INT DEFAULT 0,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_beds CHECK (occupied_beds <= total_beds)
);
CREATE INDEX idx_room_building ON dorm_room(building_id);

CREATE TABLE dorm_bed (
    id BIGINT PRIMARY KEY,
    bed_no VARCHAR(10) NOT NULL,
    room_id BIGINT NOT NULL REFERENCES dorm_room(id),
    status INT DEFAULT 0,
    repair_type VARCHAR(20) DEFAULT '其他',
    urgency INT DEFAULT 0,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_bed_room ON dorm_bed(room_id);
CREATE INDEX idx_bed_status ON dorm_bed(status, is_deleted);

CREATE TABLE accom_checkin (
    id BIGINT PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES sys_user(id),
    bed_id BIGINT NOT NULL REFERENCES dorm_bed(id),
    room_id BIGINT NOT NULL REFERENCES dorm_room(id),
    building_id BIGINT NOT NULL REFERENCES dorm_building(id),
    checkin_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status INT DEFAULT 1,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_checkin_student ON accom_checkin(student_id);
CREATE INDEX idx_checkin_bed ON accom_checkin(bed_id);

CREATE TABLE accom_transfer (
    id BIGINT PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES sys_user(id),
    old_bed_id BIGINT NOT NULL,
    old_room_id BIGINT NOT NULL,
    new_bed_id BIGINT NOT NULL,
    new_room_id BIGINT NOT NULL,
    transfer_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reason VARCHAR(500),
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE accom_checkout (
    id BIGINT PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES sys_user(id),
    checkin_id BIGINT NOT NULL REFERENCES accom_checkin(id),
    checkout_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reason VARCHAR(500),
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE repair_request (
    id BIGINT PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES sys_user(id),
    building_id BIGINT,
    room_id BIGINT,
    description VARCHAR(1000) NOT NULL,
    status INT DEFAULT 0,
    repair_type VARCHAR(20) DEFAULT '其他',
    urgency INT DEFAULT 0,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_repair_student ON repair_request(student_id);
CREATE INDEX idx_repair_status ON repair_request(status, is_deleted);

CREATE TABLE repair_process (
    id BIGINT PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES repair_request(id),
    admin_id BIGINT NOT NULL REFERENCES sys_user(id),
    action VARCHAR(50) NOT NULL,
    remark VARCHAR(500),
    process_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_process_request ON repair_process(request_id);

-- ===== 序列 =====
CREATE SEQUENCE sys_user_seq START 100;
ALTER TABLE sys_user ALTER COLUMN id SET DEFAULT nextval('sys_user_seq');
CREATE SEQUENCE dorm_building_seq START 100;
ALTER TABLE dorm_building ALTER COLUMN id SET DEFAULT nextval('dorm_building_seq');
CREATE SEQUENCE dorm_room_seq START 100;
ALTER TABLE dorm_room ALTER COLUMN id SET DEFAULT nextval('dorm_room_seq');
CREATE SEQUENCE dorm_bed_seq START 1000;
ALTER TABLE dorm_bed ALTER COLUMN id SET DEFAULT nextval('dorm_bed_seq');
CREATE SEQUENCE accom_checkin_seq START 100;
ALTER TABLE accom_checkin ALTER COLUMN id SET DEFAULT nextval('accom_checkin_seq');
CREATE SEQUENCE accom_transfer_seq START 1;
ALTER TABLE accom_transfer ALTER COLUMN id SET DEFAULT nextval('accom_transfer_seq');
CREATE SEQUENCE accom_checkout_seq START 1;
ALTER TABLE accom_checkout ALTER COLUMN id SET DEFAULT nextval('accom_checkout_seq');
CREATE SEQUENCE repair_request_seq START 1;
ALTER TABLE repair_request ALTER COLUMN id SET DEFAULT nextval('repair_request_seq');
CREATE SEQUENCE repair_process_seq START 1;
ALTER TABLE repair_process ALTER COLUMN id SET DEFAULT nextval('repair_process_seq');

-- ===== 初始数据 =====
-- 管理员
INSERT INTO sys_user(id, username, password, real_name, role, phone, gender)
VALUES (1, 'admin', '123456', 'system admin', 0, '13800000000', 0);

-- 4栋楼 (2男2女)
INSERT INTO dorm_building(id, name, building_no, floors, building_type) VALUES
(1, '男生楼A', 'M01', 3, 0),
(2, '男生楼B', 'M02', 3, 0),
(3, '女生楼A', 'F01', 3, 1),
(4, '女生楼B', 'F02', 3, 1);

-- 48间宿舍 (每栋3层×4间)
INSERT INTO dorm_room(id, room_no, building_id, floor, total_beds) VALUES
(1,'101',1,1,6),(2,'102',1,1,6),(3,'103',1,1,6),(4,'104',1,1,6),
(5,'201',1,2,6),(6,'202',1,2,6),(7,'203',1,2,6),(8,'204',1,2,6),
(9,'301',1,3,6),(10,'302',1,3,6),(11,'303',1,3,6),(12,'304',1,3,6),
(13,'101',2,1,6),(14,'102',2,1,6),(15,'103',2,1,6),(16,'104',2,1,6),
(17,'201',2,2,6),(18,'202',2,2,6),(19,'203',2,2,6),(20,'204',2,2,6),
(21,'301',2,3,6),(22,'302',2,3,6),(23,'303',2,3,6),(24,'304',2,3,6),
(25,'101',3,1,6),(26,'102',3,1,6),(27,'103',3,1,6),(28,'104',3,1,6),
(29,'201',3,2,6),(30,'202',3,2,6),(31,'203',3,2,6),(32,'204',3,2,6),
(33,'301',3,3,6),(34,'302',3,3,6),(35,'303',3,3,6),(36,'304',3,3,6),
(37,'101',4,1,6),(38,'102',4,1,6),(39,'103',4,1,6),(40,'104',4,1,6),
(41,'201',4,2,6),(42,'202',4,2,6),(43,'203',4,2,6),(44,'204',4,2,6),
(45,'301',4,3,6),(46,'302',4,3,6),(47,'303',4,3,6),(48,'304',4,3,6);

-- 288个床位
INSERT INTO dorm_bed(id, bed_no, room_id, status)
SELECT (r.id-1)*6 + s.rn, chr((65 + s.rn - 1)::int), r.id, 0
FROM dorm_room r CROSS JOIN generate_series(1,6) AS s(rn);
