-- ========================================
-- 报修工单优化迁移
-- ========================================
ALTER TABLE repair_request ADD COLUMN IF NOT EXISTS repair_type VARCHAR(20) DEFAULT '其他';
ALTER TABLE repair_request ADD COLUMN IF NOT EXISTS urgency INT DEFAULT 0;
