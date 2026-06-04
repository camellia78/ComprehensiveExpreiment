package com.dorm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.R;
import com.dorm.entity.SysUser;
import com.dorm.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final SysUserMapper userMapper;

    @GetMapping("/profile")
    public R<SysUser> profile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        SysUser user = userMapper.selectById(userId);
        user.setPassword(null); return R.ok(user);
    }

    @GetMapping("/users")
    public R<Page<SysUser>> listUsers(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        Page<SysUser> result = userMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SysUser>().orderByAsc(SysUser::getRole));
        result.getRecords().forEach(u -> u.setPassword(null));
        return R.ok(result);
    }
}
