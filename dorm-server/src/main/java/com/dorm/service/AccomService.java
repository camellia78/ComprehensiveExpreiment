package com.dorm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.BizException;
import com.dorm.dto.CheckinDTO;
import com.dorm.dto.CheckoutDTO;
import com.dorm.dto.TransferDTO;
import com.dorm.entity.*;
import com.dorm.mapper.*;
import com.dorm.vo.CheckinVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccomService {

    private final AccomCheckinMapper checkinMapper;
    private final AccomTransferMapper transferMapper;
    private final AccomCheckoutMapper checkoutMapper;
    private final DormBedMapper bedMapper;
    private final DormRoomMapper roomMapper;
    private final DormBuildingMapper buildingMapper;
    private final SysUserMapper userMapper;

    @Transactional
    public AccomCheckin checkin(CheckinDTO dto) {
        SysUser student = userMapper.selectById(dto.getStudentId());
        if (student == null || student.getRole() != 1) throw new BizException("学生不存在或非学生角色");
        Long count = checkinMapper.selectCount(new LambdaQueryWrapper<AccomCheckin>()
                .eq(AccomCheckin::getStudentId, dto.getStudentId()).eq(AccomCheckin::getStatus, 1));
        if (count > 0) throw new BizException("该学生已入住，请先退宿");
        DormBed bed = bedMapper.selectById(dto.getBedId());
        if (bed == null || bed.getStatus() == 1) throw new BizException("床位已被占用");
        if (!bed.getRoomId().equals(dto.getRoomId())) throw new BizException("床位不属于所选房间");
        DormRoom room = roomMapper.selectById(dto.getRoomId());
        if (room == null) throw new BizException("房间不存在");
        if (!room.getBuildingId().equals(dto.getBuildingId())) throw new BizException("房间不属于所选楼栋");
        if (room.getOccupiedBeds() >= room.getTotalBeds()) throw new BizException("房间已满");
        bed.setStatus(1); bedMapper.updateById(bed);
        room.setOccupiedBeds(room.getOccupiedBeds() + 1); roomMapper.updateById(room);
        AccomCheckin checkin = new AccomCheckin();
        checkin.setStudentId(dto.getStudentId()); checkin.setBedId(dto.getBedId());
        checkin.setRoomId(dto.getRoomId()); checkin.setBuildingId(dto.getBuildingId());
        checkin.setCheckinTime(LocalDateTime.now()); checkin.setStatus(1);
        checkinMapper.insert(checkin); return checkin;
    }

    public Page<CheckinVO> listCheckins(int page, int size, Integer status) {
        LambdaQueryWrapper<AccomCheckin> w = new LambdaQueryWrapper<AccomCheckin>()
                .orderByDesc(AccomCheckin::getCheckinTime);
        if (status != null) w.eq(AccomCheckin::getStatus, status);
        Page<AccomCheckin> raw = checkinMapper.selectPage(new Page<>(page, size), w);
        return toVOPage(raw);
    }

    @Transactional
    public AccomTransfer transfer(TransferDTO dto) {
        AccomCheckin current = checkinMapper.selectOne(new LambdaQueryWrapper<AccomCheckin>()
                .eq(AccomCheckin::getStudentId, dto.getStudentId()).eq(AccomCheckin::getStatus, 1));
        if (current == null) throw new BizException("该学生未入住");
        DormBed newBed = bedMapper.selectById(dto.getNewBedId());
        if (newBed == null || newBed.getStatus() == 1) throw new BizException("目标床位已被占用");
        if (!newBed.getRoomId().equals(dto.getNewRoomId())) throw new BizException("床位不属于目标房间");
        DormBed oldBed = bedMapper.selectById(current.getBedId());
        oldBed.setStatus(0); bedMapper.updateById(oldBed);
        DormRoom oldRoom = roomMapper.selectById(current.getRoomId());
        oldRoom.setOccupiedBeds(oldRoom.getOccupiedBeds() - 1); roomMapper.updateById(oldRoom);
        newBed.setStatus(1); bedMapper.updateById(newBed);
        DormRoom newRoom = roomMapper.selectById(dto.getNewRoomId());
        newRoom.setOccupiedBeds(newRoom.getOccupiedBeds() + 1); roomMapper.updateById(newRoom);
        Long oldBedId = current.getBedId(); Long oldRoomId = current.getRoomId();
        current.setBedId(dto.getNewBedId()); current.setRoomId(dto.getNewRoomId());
        current.setBuildingId(newRoom.getBuildingId()); checkinMapper.updateById(current);
        AccomTransfer transfer = new AccomTransfer();
        transfer.setStudentId(dto.getStudentId()); transfer.setOldBedId(oldBedId);
        transfer.setOldRoomId(oldRoomId); transfer.setNewBedId(dto.getNewBedId());
        transfer.setNewRoomId(dto.getNewRoomId()); transfer.setTransferTime(LocalDateTime.now());
        transfer.setReason(dto.getReason()); transferMapper.insert(transfer); return transfer;
    }

    @Transactional
    public AccomCheckout checkout(CheckoutDTO dto) {
        AccomCheckin checkin = checkinMapper.selectOne(new LambdaQueryWrapper<AccomCheckin>()
                .eq(AccomCheckin::getStudentId, dto.getStudentId()).eq(AccomCheckin::getStatus, 1));
        if (checkin == null) throw new BizException("该学生未入住");
        DormBed bed = bedMapper.selectById(checkin.getBedId());
        bed.setStatus(0); bedMapper.updateById(bed);
        DormRoom room = roomMapper.selectById(checkin.getRoomId());
        room.setOccupiedBeds(room.getOccupiedBeds() - 1); roomMapper.updateById(room);
        checkin.setStatus(0); checkinMapper.updateById(checkin);
        AccomCheckout checkout = new AccomCheckout();
        checkout.setStudentId(dto.getStudentId()); checkout.setCheckinId(checkin.getId());
        checkout.setCheckoutTime(LocalDateTime.now()); checkout.setReason(dto.getReason());
        checkoutMapper.insert(checkout); return checkout;
    }

    private Page<CheckinVO> toVOPage(Page<AccomCheckin> raw) {
        List<CheckinVO> voList = raw.getRecords().stream().map(r -> {
            CheckinVO vo = new CheckinVO();
            vo.setId(r.getId());
            vo.setStudentId(r.getStudentId());
            vo.setBedId(r.getBedId());
            vo.setRoomId(r.getRoomId());
            vo.setBuildingId(r.getBuildingId());
            vo.setCheckinTime(r.getCheckinTime());
            vo.setStatus(r.getStatus());
            return vo;
        }).collect(Collectors.toList());
        if (!voList.isEmpty()) {
            Set<Long> studentIds = voList.stream().map(CheckinVO::getStudentId).collect(Collectors.toSet());
            Set<Long> bedIds = voList.stream().map(CheckinVO::getBedId).collect(Collectors.toSet());
            Set<Long> roomIds = voList.stream().map(CheckinVO::getRoomId).collect(Collectors.toSet());
            Set<Long> buildingIds = voList.stream().map(CheckinVO::getBuildingId).collect(Collectors.toSet());
            Map<Long, SysUser> userMap = userMapper.selectBatchIds(studentIds).stream()
                    .collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));
            Map<Long, String> bedMap = bedMapper.selectBatchIds(bedIds).stream()
                    .collect(Collectors.toMap(DormBed::getId, DormBed::getBedNo, (a, b) -> a));
            Map<Long, String> roomMap = roomMapper.selectBatchIds(roomIds).stream()
                    .collect(Collectors.toMap(DormRoom::getId, DormRoom::getRoomNo, (a, b) -> a));
            Map<Long, String> buildingMap = buildingMapper.selectBatchIds(buildingIds).stream()
                    .collect(Collectors.toMap(DormBuilding::getId, DormBuilding::getName, (a, b) -> a));
            voList.forEach(vo -> {
                SysUser u = userMap.get(vo.getStudentId());
                if (u != null) { vo.setStudentName(u.getRealName()); vo.setStudentNo(u.getStudentNo()); }
                vo.setBedNo(bedMap.getOrDefault(vo.getBedId(), ""));
                vo.setRoomNo(roomMap.getOrDefault(vo.getRoomId(), ""));
                vo.setBuildingName(buildingMap.getOrDefault(vo.getBuildingId(), ""));
            });
        }
        Page<CheckinVO> voPage = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }
    public List<CheckinVO> listStudentsByRoomId(Long roomId) {
        List<AccomCheckin> checkins = checkinMapper.selectList(
                new LambdaQueryWrapper<AccomCheckin>()
                        .eq(AccomCheckin::getRoomId, roomId)
                        .eq(AccomCheckin::getStatus, 1));
        if (checkins.isEmpty()) return java.util.Collections.emptyList();
        Set<Long> studentIds = checkins.stream().map(AccomCheckin::getStudentId).collect(Collectors.toSet());
        Set<Long> bedIds = checkins.stream().map(AccomCheckin::getBedId).collect(Collectors.toSet());
        Set<Long> roomIds = checkins.stream().map(AccomCheckin::getRoomId).collect(Collectors.toSet());
        Set<Long> buildingIds = checkins.stream().map(AccomCheckin::getBuildingId).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = userMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));
        Map<Long, String> bedMap = bedMapper.selectBatchIds(bedIds).stream()
                .collect(Collectors.toMap(DormBed::getId, DormBed::getBedNo, (a, b) -> a));
        Map<Long, String> roomMap = roomMapper.selectBatchIds(roomIds).stream()
                .collect(Collectors.toMap(DormRoom::getId, DormRoom::getRoomNo, (a, b) -> a));
        Map<Long, String> buildingMap = buildingMapper.selectBatchIds(buildingIds).stream()
                .collect(Collectors.toMap(DormBuilding::getId, DormBuilding::getName, (a, b) -> a));
        return checkins.stream().map(c -> {
            CheckinVO vo = new CheckinVO();
            vo.setId(c.getId());
            vo.setStudentId(c.getStudentId());
            vo.setBedId(c.getBedId());
            vo.setRoomId(c.getRoomId());
            vo.setBuildingId(c.getBuildingId());
            vo.setCheckinTime(c.getCheckinTime());
            vo.setStatus(c.getStatus());
            SysUser u = userMap.get(c.getStudentId());
            if (u != null) { vo.setStudentName(u.getRealName()); vo.setStudentNo(u.getStudentNo()); }
            vo.setBedNo(bedMap.getOrDefault(c.getBedId(), ""));
            vo.setRoomNo(roomMap.getOrDefault(c.getRoomId(), ""));
            vo.setBuildingName(buildingMap.getOrDefault(c.getBuildingId(), ""));
            return vo;
        }).collect(Collectors.toList());
    }
}