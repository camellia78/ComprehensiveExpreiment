package com.dorm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("dorm_room")
public class DormRoom {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String roomNo;
    private Long buildingId;
    private Integer floor;
    private Integer totalBeds;
    private Integer occupiedBeds;
    private Integer status;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
