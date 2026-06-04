package com.dorm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.R;
import com.dorm.dto.BuildingDTO;
import com.dorm.dto.RoomDTO;
import com.dorm.dto.RoomQueryDTO;
import com.dorm.entity.DormBed;
import com.dorm.entity.DormBuilding;
import com.dorm.entity.DormRoom;
import com.dorm.service.DormService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DormController {
    private final DormService dormService;

    @GetMapping("/buildings")
    public R<List<DormBuilding>> listBuildings() { return R.ok(dormService.listBuildings()); }

    @PostMapping("/buildings")
    public R<DormBuilding> addBuilding(@RequestBody BuildingDTO dto) { return R.ok(dormService.addBuilding(dto)); }

    @PutMapping("/buildings/{id}")
    public R<DormBuilding> updateBuilding(@PathVariable Long id, @RequestBody BuildingDTO dto) { return R.ok(dormService.updateBuilding(id, dto)); }

    @DeleteMapping("/buildings/{id}")
    public R<Void> deleteBuilding(@PathVariable Long id) { dormService.deleteBuilding(id); return R.ok(); }

    @GetMapping("/rooms")
    public R<Page<DormRoom>> listRooms(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, RoomQueryDTO query) {
        return R.ok(dormService.listRooms(page, size, query));
    }

    @PostMapping("/rooms")
    public R<DormRoom> addRoom(@RequestBody RoomDTO dto) { return R.ok(dormService.addRoom(dto)); }

    @PutMapping("/rooms/{id}")
    public R<DormRoom> updateRoom(@PathVariable Long id, @RequestBody RoomDTO dto) { return R.ok(dormService.updateRoom(id, dto)); }

    @DeleteMapping("/rooms/{id}")
    public R<Void> deleteRoom(@PathVariable Long id) { dormService.deleteRoom(id); return R.ok(); }

    @GetMapping("/rooms/{id}/beds")
    public R<List<DormBed>> listBeds(@PathVariable Long id) { return R.ok(dormService.listBeds(id)); }

    @GetMapping("/buildings/{id}/free-beds")
    public R<List<DormBed>> listFreeBeds(@PathVariable Long id) { return R.ok(dormService.listFreeBeds(id)); }
}
