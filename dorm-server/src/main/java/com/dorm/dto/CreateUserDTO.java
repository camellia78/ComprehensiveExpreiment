package com.dorm.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;
@Data
public class CreateUserDTO {
    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank private String realName;
    private Integer role;
    private String studentNo;
    private String phone;
    private Integer gender;
}