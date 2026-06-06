CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    real_name VARCHAR(50),
    role INT DEFAULT 1,
    student_no VARCHAR(20),
    phone VARCHAR(20),
    gender INT DEFAULT 0,
    login_fail_count INT DEFAULT 0,
    lock_until TIMESTAMP,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dorm_building (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100),
    building_no VARCHAR(20),
    floors INT DEFAULT 1,
    building_type INT DEFAULT 0,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dorm_room (
    id BIGINT PRIMARY KEY,
    room_no VARCHAR(20),
    building_id BIGINT,
    floor INT DEFAULT 1,
    total_beds INT DEFAULT 4,
    occupied_beds INT DEFAULT 0,
    status INT DEFAULT 0,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dorm_bed (
    id BIGINT PRIMARY KEY,
    bed_no VARCHAR(5),
    room_id BIGINT,
    status INT DEFAULT 0,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accom_checkin (
    id BIGINT PRIMARY KEY,
    student_id BIGINT,
    bed_id BIGINT,
    room_id BIGINT,
    building_id BIGINT,
    checkin_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status INT DEFAULT 1,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accom_transfer (
    id BIGINT PRIMARY KEY,
    student_id BIGINT,
    old_bed_id BIGINT,
    old_room_id BIGINT,
    new_bed_id BIGINT,
    new_room_id BIGINT,
    transfer_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reason VARCHAR(500),
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accom_checkout (
    id BIGINT PRIMARY KEY,
    student_id BIGINT,
    checkin_id BIGINT,
    checkout_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reason VARCHAR(500),
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS repair_request (
    id BIGINT PRIMARY KEY,
    student_id BIGINT,
    room_id BIGINT,
    repair_type VARCHAR(50),
    description VARCHAR(500),
    status INT DEFAULT 0,
    submit_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    repair_user_id BIGINT,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS repair_process (
    id BIGINT PRIMARY KEY,
    repair_id BIGINT,
    process_user_id BIGINT,
    action VARCHAR(50),
    remark VARCHAR(500),
    process_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);