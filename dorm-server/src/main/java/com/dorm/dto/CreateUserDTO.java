package com.dorm.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class CreateUserDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 30)
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20)
    private String password;
    @NotBlank(message = "姓名不能为空")
    private String realName;
    @NotNull(message = "角色不能为空")
    private Integer role;
    private String studentNo;
    private String phone;
    private Integer gender;
}
