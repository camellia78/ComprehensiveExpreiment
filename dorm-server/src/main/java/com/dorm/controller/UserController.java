package com.dorm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.annotation.RequireRole;
import com.dorm.common.BizException;
import com.dorm.common.R;
import com.dorm.config.SecurityConfig.PasswordEncoder;
import com.dorm.dto.CreateUserDTO;
import com.dorm.dto.ResetPasswordDTO;
import com.dorm.dto.UpdateUserDTO;
import com.dorm.entity.AccomCheckin;
import com.dorm.entity.DormBed;
import com.dorm.entity.DormBuilding;
import com.dorm.entity.DormRoom;
import com.dorm.entity.SysUser;
import com.dorm.mapper.AccomCheckinMapper;
import com.dorm.mapper.DormBedMapper;
import com.dorm.mapper.DormBuildingMapper;
import com.dorm.mapper.DormRoomMapper;
import com.dorm.mapper.SysUserMapper;
import com.dorm.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final SysUserMapper userMapper;
    private final AccomCheckinMapper checkinMapper;
    private final DormBedMapper bedMapper;
    private final DormRoomMapper roomMapper;
    private final DormBuildingMapper buildingMapper;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public R<SysUser> profile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        SysUser user = userMapper.selectById(userId);
        if (user != null) user.setPassword(null);
        return R.ok(user);
    }

    @GetMapping("/users")
    @RequireRole({0, 2})
    public R<Page<SysUser>> listUsers(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Integer role) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getStudentNo, keyword)
                    .or().like(SysUser::getPhone, keyword));
        }
        if (role != null) wrapper.eq(SysUser::getRole, role);
        wrapper.orderByAsc(SysUser::getRole).orderByAsc(SysUser::getCreateTime);
        Page<SysUser> result = userMapper.selectPage(new Page<>(page, size), wrapper);

        // Enrich student users with dormitory info
        if (!result.getRecords().isEmpty()) {
            Set<Long> studentIds = result.getRecords().stream()
                    .filter(u -> u.getRole() != null && u.getRole() == 1)
                    .map(SysUser::getId).collect(Collectors.toSet());
            if (!studentIds.isEmpty()) {
                List<AccomCheckin> activeCheckins = checkinMapper.selectList(
                        new LambdaQueryWrapper<AccomCheckin>()
                                .in(AccomCheckin::getStudentId, studentIds)
                                .eq(AccomCheckin::getStatus, 1));
                if (!activeCheckins.isEmpty()) {
                    Map<Long, AccomCheckin> checkinMap = activeCheckins.stream()
                            .collect(Collectors.toMap(AccomCheckin::getStudentId, c -> c, (a, b) -> a));
                    Set<Long> buildingIds = activeCheckins.stream().map(AccomCheckin::getBuildingId).collect(Collectors.toSet());
                    Set<Long> roomIds = activeCheckins.stream().map(AccomCheckin::getRoomId).collect(Collectors.toSet());
                    Set<Long> bedIds = activeCheckins.stream().map(AccomCheckin::getBedId).collect(Collectors.toSet());
                    Map<Long, String> buildingMap = buildingMapper.selectBatchIds(buildingIds).stream()
                            .collect(Collectors.toMap(DormBuilding::getId, DormBuilding::getName, (a, b) -> a));
                    Map<Long, String> roomMap = roomMapper.selectBatchIds(roomIds).stream()
                            .collect(Collectors.toMap(DormRoom::getId, DormRoom::getRoomNo, (a, b) -> a));
                    Map<Long, String> bedMap = bedMapper.selectBatchIds(bedIds).stream()
                            .collect(Collectors.toMap(DormBed::getId, DormBed::getBedNo, (a, b) -> a));
                    result.getRecords().forEach(u -> {
                        if (u.getRole() != null && u.getRole() == 1) {
                            AccomCheckin checkin = checkinMap.get(u.getId());
                            if (checkin != null) {
                                u.setBuildingName(buildingMap.getOrDefault(checkin.getBuildingId(), ""));
                                u.setRoomNo(roomMap.getOrDefault(checkin.getRoomId(), ""));
                                u.setBedNo(bedMap.getOrDefault(checkin.getBedId(), ""));
                            }
                        }
                    });
                }
            }
        }

        result.getRecords().forEach(u -> u.setPassword(null));
        return R.ok(result);
    }

    @PostMapping("/users")
    @RequireRole({0, 2})
    public R<?> createUser(@Valid @RequestBody CreateUserDTO dto, HttpServletRequest request) {
        Integer operatorRole = (Integer) request.getAttribute("role");
        if (operatorRole == 2 && dto.getRole() != 1) {
            throw new BizException("二级管理员只能创建学生账号");
        }
        if (dto.getRole() == 0) {
            long superCount = userMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, 0));
            if (superCount >= 1) throw new BizException("总管理员已存在，系统仅允许一个总管理员");
        }
        SysUser exist = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (exist != null) throw new BizException("用户名已存在");
        if (dto.getStudentNo() != null && !dto.getStudentNo().isEmpty()) {
            SysUser existNo = userMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getStudentNo, dto.getStudentNo()));
            if (existNo != null) throw new BizException("学号已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setRole(dto.getRole());
        user.setStudentNo(dto.getStudentNo());
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender() != null ? dto.getGender() : 0);
        userMapper.insert(user);
        return R.ok(null);
    }

    @PutMapping("/users/{id}")
    @RequireRole({0, 2})
    public R<?> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserDTO dto,
                           HttpServletRequest request) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BizException("用户不存在");
        Integer operatorRole = (Integer) request.getAttribute("role");
        if (operatorRole == 2 && user.getRole() != 1) {
            throw new BizException("二级管理员只能编辑学生信息");
        }
        if (dto.getStudentNo() != null && !dto.getStudentNo().isEmpty()
                && !dto.getStudentNo().equals(user.getStudentNo())) {
            SysUser existNo = userMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getStudentNo, dto.getStudentNo()));
            if (existNo != null) throw new BizException("学号已被其他用户使用");
        }
        if (dto.getRealName() != null) user.setRealName(dto.getRealName());
        if (dto.getStudentNo() != null) user.setStudentNo(dto.getStudentNo());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getRole() != null) {
            if (operatorRole == 2) throw new BizException("二级管理员不能修改用户角色");
            if (dto.getRole() == 0 && user.getRole() != 0) {
                long superCount = userMapper.selectCount(
                        new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, 0));
                if (superCount >= 1) throw new BizException("总管理员已存在，不能将其他用户提升为总管理员");
            }
            user.setRole(dto.getRole());
        }
        userMapper.updateById(user);
        return R.ok(null);
    }

    @DeleteMapping("/users/{id}")
    @RequireRole(0)
    public R<?> deleteUser(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BizException("用户不存在");
        if (user.getRole() == 0) throw new BizException("不能删除总管理员账号");
        userMapper.deleteById(id);
        return R.ok(null);
    }

    @PutMapping("/users/{id}/reset-password")
    @RequireRole({0, 2})
    public R<?> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordDTO dto,
                              HttpServletRequest request) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BizException("用户不存在");
        Integer operatorRole = (Integer) request.getAttribute("role");
        if (operatorRole == 2 && user.getRole() != 1) {
            throw new BizException("二级管理员只能重置学生密码");
        }
        authService.resetPassword(id, dto.getNewPassword());
        return R.ok(null);
    }
}