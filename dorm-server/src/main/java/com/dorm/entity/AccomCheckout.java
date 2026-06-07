package com.dorm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("accom_checkout")
public class AccomCheckout {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long checkinId;
    private LocalDateTime checkoutTime;
    private String reason;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
