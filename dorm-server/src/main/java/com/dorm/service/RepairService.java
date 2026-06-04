package com.dorm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.BizException;
import com.dorm.dto.RepairDTO;
import com.dorm.dto.RepairProcessDTO;
import com.dorm.entity.*;
import com.dorm.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RepairService {

    private final RepairRequestMapper requestMapper;
    private final RepairProcessMapper processMapper;

    public RepairRequest submit(RepairDTO dto, Long studentId) {
        RepairRequest req = new RepairRequest();
        req.setStudentId(studentId); req.setBuildingId(dto.getBuildingId());
        req.setRoomId(dto.getRoomId()); req.setDescription(dto.getDescription()); req.setStatus(0);
        requestMapper.insert(req); return req;
    }

    public Page<RepairRequest> listMyRepairs(int page, int size, Long studentId) {
        return requestMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<RepairRequest>().eq(RepairRequest::getStudentId, studentId)
                        .orderByDesc(RepairRequest::getCreateTime));
    }

    public Page<RepairRequest> listAllRepairs(int page, int size, Integer status) {
        LambdaQueryWrapper<RepairRequest> w = new LambdaQueryWrapper<RepairRequest>()
                .orderByAsc(RepairRequest::getStatus).orderByDesc(RepairRequest::getCreateTime);
        if (status != null) w.eq(RepairRequest::getStatus, status);
        return requestMapper.selectPage(new Page<>(page, size), w);
    }

    public void processRepair(Long requestId, RepairProcessDTO dto, Long adminId) {
        RepairRequest req = requestMapper.selectById(requestId);
        if (req == null) throw new BizException("报修单不存在");
        req.setStatus(dto.getStatus()); requestMapper.updateById(req);
        RepairProcess p = new RepairProcess();
        p.setRequestId(requestId); p.setAdminId(adminId);
        p.setAction(dto.getAction()); p.setRemark(dto.getRemark());
        p.setProcessTime(LocalDateTime.now()); processMapper.insert(p);
    }

    public void cancelRepair(Long requestId, Long studentId) {
        RepairRequest req = requestMapper.selectById(requestId);
        if (req == null) throw new BizException("报修单不存在");
        if (!req.getStudentId().equals(studentId)) throw new BizException("无权操作");
        if (req.getStatus() != 0) throw new BizException("只能取消待处理的报修");
        req.setStatus(3); requestMapper.updateById(req);
    }
}
