-- ========================================
-- openGauss / H2 Compatible
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
    login_fail_count INT DEFAULT 0,
    lock_until TIMESTAMP,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_username ON sys_user(username);
CREATE UNIQUE INDEX IF NOT EXISTS uk_student_no ON sys_user(student_no);

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
    building_id BIGINT NOT NULL,
    floor INT DEFAULT 1,
    total_beds INT DEFAULT 4,
    occupied_beds INT DEFAULT 0,
    status INT DEFAULT 0,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE dorm_bed (
    id BIGINT PRIMARY KEY,
    bed_no VARCHAR(10) NOT NULL,
    room_id BIGINT NOT NULL,
    status INT DEFAULT 0,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE accom_checkin (
    id BIGINT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    bed_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    building_id BIGINT NOT NULL,
    checkin_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status INT DEFAULT 1,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE accom_transfer (
    id BIGINT PRIMARY KEY,
    student_id BIGINT NOT NULL,
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
    student_id BIGINT NOT NULL,
    checkin_id BIGINT NOT NULL,
    checkout_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reason VARCHAR(500),
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE repair_request (
    id BIGINT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    building_id BIGINT,
    room_id BIGINT,
    description VARCHAR(1000) NOT NULL,
    status INT DEFAULT 0,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE repair_process (
    id BIGINT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    admin_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    remark VARCHAR(500),
    process_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO sys_user(id, username, password, real_name, role, student_no, phone, gender, is_deleted)
VALUES (1, 'admin', '123456', 'system admin', 0, NULL, '13800000000', 0, 0);
INSERT INTO sys_user(id, username, password, real_name, role, student_no, phone, gender, is_deleted)
VALUES (2, 'zhangsan', '123456', 'zhangsan', 1, '2024001', '13800000001', 1, 0);
INSERT INTO sys_user(id, username, password, real_name, role, student_no, phone, gender, is_deleted)
VALUES (3, 'lisi', '123456', 'lisi', 1, '2024002', '13800000002', 0, 0);

INSERT INTO dorm_building(id, name, building_no, floors, building_type, is_deleted)
VALUES (1, 'Bldg 1', 'A01', 6, 0, 0);
INSERT INTO dorm_building(id, name, building_no, floors, building_type, is_deleted)
VALUES (2, 'Bldg 2', 'A02', 6, 1, 0);

INSERT INTO dorm_room(id, room_no, building_id, floor, total_beds, occupied_beds, status, is_deleted)
VALUES (1, '101', 1, 1, 4, 0, 0, 0);
INSERT INTO dorm_room(id, room_no, building_id, floor, total_beds, occupied_beds, status, is_deleted)
VALUES (2, '102', 1, 1, 4, 0, 0, 0);
INSERT INTO dorm_room(id, room_no, building_id, floor, total_beds, occupied_beds, status, is_deleted)
VALUES (3, '201', 2, 2, 4, 0, 0, 0);

INSERT INTO dorm_bed(id, bed_no, room_id, status, is_deleted) VALUES (1, 'A', 1, 0, 0);
INSERT INTO dorm_bed(id, bed_no, room_id, status, is_deleted) VALUES (2, 'B', 1, 0, 0);
INSERT INTO dorm_bed(id, bed_no, room_id, status, is_deleted) VALUES (3, 'C', 1, 0, 0);
INSERT INTO dorm_bed(id, bed_no, room_id, status, is_deleted) VALUES (4, 'D', 1, 0, 0);
INSERT INTO dorm_bed(id, bed_no, room_id, status, is_deleted) VALUES (5, 'A', 2, 0, 0);
INSERT INTO dorm_bed(id, bed_no, room_id, status, is_deleted) VALUES (6, 'B', 2, 0, 0);
INSERT INTO dorm_bed(id, bed_no, room_id, status, is_deleted) VALUES (7, 'C', 2, 0, 0);
INSERT INTO dorm_bed(id, bed_no, room_id, status, is_deleted) VALUES (8, 'D', 2, 0, 0);
INSERT INTO dorm_bed(id, bed_no, room_id, status, is_deleted) VALUES (9, 'A', 3, 0, 0);
INSERT INTO dorm_bed(id, bed_no, room_id, status, is_deleted) VALUES (10, 'B', 3, 0, 0);
INSERT INTO dorm_bed(id, bed_no, room_id, status, is_deleted) VALUES (11, 'C', 3, 0, 0);
INSERT INTO dorm_bed(id, bed_no, room_id, status, is_deleted) VALUES (12, 'D', 3, 0, 0);