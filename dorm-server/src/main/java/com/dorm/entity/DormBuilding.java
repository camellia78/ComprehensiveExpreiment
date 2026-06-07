package com.dorm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("dorm_building")
public class DormBuilding {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String buildingNo;
    private Integer floors;
    private Integer buildingType;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
