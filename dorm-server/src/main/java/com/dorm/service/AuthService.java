package com.dorm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dorm.common.BizException;
import com.dorm.common.JwtUtils;
import com.dorm.dto.LoginDTO;
import com.dorm.dto.LoginResultDTO;
import com.dorm.entity.SysUser;
import com.dorm.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final JwtUtils jwtUtils;

    public LoginResultDTO login(LoginDTO dto) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (user == null) throw new BizException("用户名或密码错误");
        if (!verifyPassword(dto.getPassword(), user.getPassword())) throw new BizException("用户名或密码错误");
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        LoginResultDTO result = new LoginResultDTO();
        result.setToken(token);
        result.setUserId(user.getId());
        result.setUsername(user.getUsername());
        result.setRealName(user.getRealName());
        result.setRole(user.getRole());
        return result;
    }

    private boolean verifyPassword(String input, String stored) {
        if (stored.startsWith("{SHA256}")) {
            String hash = sha256Base64(input);
            return stored.equals("{SHA256}" + hash);
        }
        return stored.equals(input);
    }

    private String sha256Base64(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 error", e);
        }
    }

    public SysUser getUserById(Long id) { return userMapper.selectById(id); }
}