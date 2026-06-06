package com.dorm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("repair_request")
public class RepairRequest {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long studentId;
    private Long buildingId;
    private Long roomId;
    private String description;
    private String repairType;
    private Integer urgency;
    private Integer status;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
