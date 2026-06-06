package com.dorm.dto;
import lombok.Data;

@Data
public class UpdateUserDTO {
    private String realName;
    private String studentNo;
    private String phone;
    private Integer gender;
    private Integer role;
}
