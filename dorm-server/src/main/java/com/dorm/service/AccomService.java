package com.dorm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dorm.common.BizException;
import com.dorm.dto.CheckinDTO;
import com.dorm.dto.CheckoutDTO;
import com.dorm.dto.TransferDTO;
import com.dorm.entity.*;
import com.dorm.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccomService {

    private final AccomCheckinMapper checkinMapper;
    private final AccomTransferMapper transferMapper;
    private final AccomCheckoutMapper checkoutMapper;
    private final DormBedMapper bedMapper;
    private final DormRoomMapper roomMapper;
    private final SysUserMapper userMapper;

    @Transactional
    public AccomCheckin checkin(CheckinDTO dto) {
        SysUser student = userMapper.selectById(dto.getStudentId());
        if (student == null || student.getRole() != 1) throw new BizException("学生不存在");
        Long count = checkinMapper.selectCount(new LambdaQueryWrapper<AccomCheckin>()
                .eq(AccomCheckin::getStudentId, dto.getStudentId()).eq(AccomCheckin::getStatus, 1));
        if (count > 0) throw new BizException("该学生已入住");
        DormBed bed = bedMapper.selectById(dto.getBedId());
        if (bed == null || bed.getStatus() == 1) throw new BizException("床位已被占用");
        bed.setStatus(1); bedMapper.updateById(bed);
        DormRoom room = roomMapper.selectById(dto.getRoomId());
        room.setOccupiedBeds(room.getOccupiedBeds() + 1); roomMapper.updateById(room);
        AccomCheckin checkin = new AccomCheckin();
        checkin.setStudentId(dto.getStudentId()); checkin.setBedId(dto.getBedId());
        checkin.setRoomId(dto.getRoomId()); checkin.setBuildingId(dto.getBuildingId());
        checkin.setCheckinTime(LocalDateTime.now()); checkin.setStatus(1);
        checkinMapper.insert(checkin); return checkin;
    }

    public List<AccomCheckin> listCheckins() {
        return checkinMapper.selectList(new LambdaQueryWrapper<AccomCheckin>().eq(AccomCheckin::getStatus, 1));
    }

    @Transactional
    public AccomTransfer transfer(TransferDTO dto) {
        AccomCheckin current = checkinMapper.selectOne(new LambdaQueryWrapper<AccomCheckin>()
                .eq(AccomCheckin::getStudentId, dto.getStudentId()).eq(AccomCheckin::getStatus, 1));
        if (current == null) throw new BizException("该学生未入住");
        DormBed newBed = bedMapper.selectById(dto.getNewBedId());
        if (newBed == null || newBed.getStatus() == 1) throw new BizException("目标床位已被占用");
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
}
