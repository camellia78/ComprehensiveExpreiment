package com.dorm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.BizException;
import com.dorm.dto.BuildingDTO;
import com.dorm.dto.RoomDTO;
import com.dorm.dto.RoomQueryDTO;
import com.dorm.entity.*;
import com.dorm.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DormService {

    private final DormBuildingMapper buildingMapper;
    private final DormRoomMapper roomMapper;
    private final DormBedMapper bedMapper;

    public List<DormBuilding> listBuildings() { return buildingMapper.selectList(null); }

    public DormBuilding addBuilding(BuildingDTO dto) {
        DormBuilding b = new DormBuilding();
        b.setName(dto.getName()); b.setBuildingNo(dto.getBuildingNo());
        b.setFloors(dto.getFloors()); b.setBuildingType(dto.getBuildingType());
        buildingMapper.insert(b); return b;
    }

    public DormBuilding updateBuilding(Long id, BuildingDTO dto) {
        DormBuilding b = buildingMapper.selectById(id);
        if (b == null) throw new BizException("楼栋不存在");
        b.setName(dto.getName()); b.setBuildingNo(dto.getBuildingNo());
        b.setFloors(dto.getFloors()); b.setBuildingType(dto.getBuildingType());
        buildingMapper.updateById(b); return b;
    }

    @Transactional
    public void deleteBuilding(Long id) {
        if (buildingMapper.selectById(id) == null) throw new BizException("楼栋不存在");
        buildingMapper.deleteById(id);
        roomMapper.delete(new LambdaUpdateWrapper<DormRoom>().eq(DormRoom::getBuildingId, id));
    }

    public Page<DormRoom> listRooms(int page, int size, RoomQueryDTO query) {
        LambdaQueryWrapper<DormRoom> w = new LambdaQueryWrapper<>();
        if (query.getBuildingId() != null) w.eq(DormRoom::getBuildingId, query.getBuildingId());
        if (query.getFloor() != null) w.eq(DormRoom::getFloor, query.getFloor());
        if (query.getStatus() != null) w.eq(DormRoom::getStatus, query.getStatus());
        return roomMapper.selectPage(new Page<>(page, size), w);
    }

    @Transactional
    public DormRoom addRoom(RoomDTO dto) {
        DormRoom room = new DormRoom();
        room.setRoomNo(dto.getRoomNo()); room.setBuildingId(dto.getBuildingId());
        room.setFloor(dto.getFloor()); room.setTotalBeds(dto.getTotalBeds());
        room.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        roomMapper.insert(room);
        for (int i = 0; i < dto.getTotalBeds(); i++) {
            DormBed bed = new DormBed();
            bed.setBedNo(String.valueOf((char)('A' + i))); bed.setRoomId(room.getId()); bed.setStatus(0);
            bedMapper.insert(bed);
        }
        return room;
    }

    public DormRoom updateRoom(Long id, RoomDTO dto) {
        DormRoom room = roomMapper.selectById(id);
        if (room == null) throw new BizException("寝室不存在");
        room.setRoomNo(dto.getRoomNo()); room.setBuildingId(dto.getBuildingId());
        room.setFloor(dto.getFloor()); room.setTotalBeds(dto.getTotalBeds());
        room.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        roomMapper.updateById(room); return room;
    }

    @Transactional
    public void deleteRoom(Long id) {
        if (roomMapper.selectById(id) == null) throw new BizException("寝室不存在");
        roomMapper.deleteById(id);
        bedMapper.delete(new LambdaUpdateWrapper<DormBed>().eq(DormBed::getRoomId, id));
    }

    public List<DormBed> listBeds(Long roomId) {
        return bedMapper.selectList(new LambdaQueryWrapper<DormBed>().eq(DormBed::getRoomId, roomId));
    }

    public List<DormBed> listFreeBeds(Long buildingId) {
        List<DormRoom> rooms = roomMapper.selectList(
                new LambdaQueryWrapper<DormRoom>().eq(DormRoom::getBuildingId, buildingId));
        List<DormBed> freeBeds = new ArrayList<>();
        for (DormRoom room : rooms) {
            freeBeds.addAll(bedMapper.selectList(
                    new LambdaQueryWrapper<DormBed>().eq(DormBed::getRoomId, room.getId()).eq(DormBed::getStatus, 0)));
        }
        return freeBeds;
    }
}
