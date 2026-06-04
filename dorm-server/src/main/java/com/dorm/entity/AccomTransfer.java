package com.dorm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("accom_transfer")
public class AccomTransfer {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long studentId;
    private Long oldBedId;
    private Long oldRoomId;
    private Long newBedId;
    private Long newRoomId;
    private LocalDateTime transferTime;
    private String reason;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
