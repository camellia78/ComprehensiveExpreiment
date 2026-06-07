package com.dorm.controller;

import com.dorm.common.R;
import com.dorm.dto.ChangePasswordDTO;
import com.dorm.dto.LoginDTO;
import com.dorm.dto.LoginResultDTO;
import com.dorm.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public R<LoginResultDTO> login(@Valid @RequestBody LoginDTO dto) { return R.ok(authService.login(dto)); }

    @PutMapping("/change-password")
    public R<?> changePassword(@Valid @RequestBody ChangePasswordDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        authService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return R.ok(null);
    }
}