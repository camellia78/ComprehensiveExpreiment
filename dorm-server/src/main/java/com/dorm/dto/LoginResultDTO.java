package com.dorm.dto;
import lombok.Data;

@Data
public class LoginResultDTO {
    private String token;
    private Long userId;
    private String username;
    private String realName;
    private Integer role;
}
