package com.dorm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.BizException;
import com.dorm.dto.RepairDTO;
import com.dorm.dto.RepairProcessDTO;
import com.dorm.entity.*;
import com.dorm.mapper.*;
import com.dorm.vo.RepairRequestVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepairService {

    private final RepairRequestMapper requestMapper;
    private final RepairProcessMapper processMapper;
    private final SysUserMapper userMapper;
    private final DormBuildingMapper buildingMapper;
    private final DormRoomMapper roomMapper;

    public RepairRequest submit(RepairDTO dto, Long studentId) {
        RepairRequest req = new RepairRequest();
        req.setStudentId(studentId);
        req.setBuildingId(dto.getBuildingId());
        req.setRoomId(dto.getRoomId());
        req.setDescription(dto.getDescription());
        req.setRepairType(dto.getRepairType() != null ? dto.getRepairType() : "其他");
        req.setUrgency(0);
        req.setStatus(0);
        requestMapper.insert(req);
        return req;
    }

    public Page<RepairRequestVO> listMyRepairs(int page, int size, Long studentId) {
        Page<RepairRequest> raw = requestMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<RepairRequest>().eq(RepairRequest::getStudentId, studentId)
                        .orderByDesc(RepairRequest::getCreateTime));
        return toVOPage(raw);
    }

    public Page<RepairRequestVO> listAllRepairs(int page, int size, Integer status) {
        LambdaQueryWrapper<RepairRequest> w = new LambdaQueryWrapper<RepairRequest>()
                .orderByAsc(RepairRequest::getStatus).orderByDesc(RepairRequest::getCreateTime);
        if (status != null) w.eq(RepairRequest::getStatus, status);
        Page<RepairRequest> raw = requestMapper.selectPage(new Page<>(page, size), w);
        return toVOPage(raw);
    }

    public List<RepairProcess> getProcessHistory(Long requestId) {
        return processMapper.selectList(
                new LambdaQueryWrapper<RepairProcess>().eq(RepairProcess::getRequestId, requestId)
                        .orderByDesc(RepairProcess::getProcessTime));
    }

    public void processRepair(Long requestId, RepairProcessDTO dto, Long adminId) {
        RepairRequest req = requestMapper.selectById(requestId);
        if (req == null) throw new BizException("报修单不存在");
        // 状态校验
        int cur = req.getStatus();
        int target = dto.getStatus();
        if (cur == 2) throw new BizException("该工单已完成，不可操作");
        if (cur == 3) throw new BizException("该工单已取消，不可操作");
        if (target != 1 && target != 2) throw new BizException("无效的目标状态");
        if (cur == 0 && target == 2) throw new BizException("请先接单处理");
        req.setStatus(target);
        requestMapper.updateById(req);
        RepairProcess p = new RepairProcess();
        p.setRequestId(requestId);
        p.setAdminId(adminId);
        p.setAction(dto.getAction());
        p.setRemark(dto.getRemark());
        p.setProcessTime(LocalDateTime.now());
        processMapper.insert(p);
    }

    public void cancelRepair(Long requestId, Long studentId) {
        RepairRequest req = requestMapper.selectById(requestId);
        if (req == null) throw new BizException("报修单不存在");
        if (!req.getStudentId().equals(studentId)) throw new BizException("无权操作");
        if (req.getStatus() != 0) throw new BizException("只能取消待处理的报修");
        req.setStatus(3);
        requestMapper.updateById(req);
    }

    // ---------- 内部辅助 ----------
    private Page<RepairRequestVO> toVOPage(Page<RepairRequest> raw) {
        List<RepairRequestVO> voList = raw.getRecords().stream().map(r -> {
            RepairRequestVO vo = new RepairRequestVO();
            BeanUtils.copyProperties(r, vo);
            return vo;
        }).collect(Collectors.toList());
        if (!voList.isEmpty()) {
            Set<Long> studentIds = voList.stream().map(RepairRequestVO::getStudentId).collect(Collectors.toSet());
            Set<Long> buildingIds = voList.stream().map(RepairRequestVO::getBuildingId).filter(Objects::nonNull).collect(Collectors.toSet());
            Set<Long> roomIds = voList.stream().map(RepairRequestVO::getRoomId).filter(Objects::nonNull).collect(Collectors.toSet());
            Map<Long, String> studentMap = userMapper.selectBatchIds(studentIds).stream()
                    .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName, (a, b) -> a));
            Map<Long, String> buildingMap = buildingMapper.selectBatchIds(buildingIds).stream()
                    .collect(Collectors.toMap(DormBuilding::getId, DormBuilding::getName, (a, b) -> a));
            Map<Long, String> roomMap = roomMapper.selectBatchIds(roomIds).stream()
                    .collect(Collectors.toMap(DormRoom::getId, DormRoom::getRoomNo, (a, b) -> a));
            voList.forEach(vo -> {
                vo.setStudentName(studentMap.getOrDefault(vo.getStudentId(), ""));
                vo.setBuildingName(buildingMap.getOrDefault(vo.getBuildingId(), ""));
                vo.setRoomNo(roomMap.getOrDefault(vo.getRoomId(), ""));
            });
        }
        Page<RepairRequestVO> voPage = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }
}
