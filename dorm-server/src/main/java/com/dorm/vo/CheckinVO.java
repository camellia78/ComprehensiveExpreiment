package com.dorm.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CheckinVO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private Long bedId;
    private String bedNo;
    private Long roomId;
    private String roomNo;
    private Long buildingId;
    private String buildingName;
    private LocalDateTime checkinTime;
    private Integer status;
}