package com.dorm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("repair_process")
public class RepairProcess {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long requestId;
    private Long adminId;
    private String action;
    private String remark;
    private LocalDateTime processTime;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
