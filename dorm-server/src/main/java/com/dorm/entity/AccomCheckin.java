package com.dorm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("accom_checkin")
public class AccomCheckin {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long bedId;
    private Long roomId;
    private Long buildingId;
    private LocalDateTime checkinTime;
    private Integer status;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
