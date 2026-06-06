package com.dorm.vo;

import com.dorm.entity.RepairRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RepairRequestVO extends RepairRequest {
    private String studentName;
    private String buildingName;
    private String roomNo;
}
