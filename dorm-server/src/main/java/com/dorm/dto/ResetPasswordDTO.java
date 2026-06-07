package com.dorm.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;
@Data
public class ResetPasswordDTO {
    @NotBlank private String newPassword;
}