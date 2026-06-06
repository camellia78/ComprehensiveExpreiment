package com.dorm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dorm.common.BizException;
import com.dorm.common.JwtUtils;
import com.dorm.config.SecurityConfig.PasswordEncoder;
import com.dorm.dto.ChangePasswordDTO;
import com.dorm.dto.LoginDTO;
import com.dorm.dto.LoginResultDTO;
import com.dorm.entity.SysUser;
import com.dorm.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    private static final int MAX_LOGIN_FAILS = 5;
    private static final long LOCK_MINUTES = 15;

    public LoginResultDTO login(LoginDTO dto) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (user == null) throw new BizException("用户名或密码错误");

        if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
            throw new BizException("账户已被锁定，请" + LOCK_MINUTES + "分钟后再试");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            int fails = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
            user.setLoginFailCount(fails);
            if (fails >= MAX_LOGIN_FAILS) {
                user.setLockUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
            }
            userMapper.updateById(user);
            int remaining = MAX_LOGIN_FAILS - fails;
            throw new BizException("用户名或密码错误" + (remaining > 0 ? "，剩余尝试次数：" + remaining : "，账户已锁定"));
        }

        user.setLoginFailCount(0);
        user.setLockUntil(null);
        userMapper.updateById(user);

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        LoginResultDTO result = new LoginResultDTO();
        result.setToken(token);
        result.setUserId(user.getId());
        result.setUsername(user.getUsername());
        result.setRealName(user.getRealName());
        result.setRole(user.getRole());
        return result;
    }

    public SysUser getUserById(Long id) {
        return userMapper.selectById(id);
    }

    public void changePassword(Long userId, ChangePasswordDTO dto) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BizException("用户不存在");
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BizException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    public void resetPassword(Long userId, String newPassword) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BizException("用户不存在");
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
}
