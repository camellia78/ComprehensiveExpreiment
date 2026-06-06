package com.dorm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.R;
import com.dorm.dto.CheckinDTO;
import com.dorm.dto.CheckoutDTO;
import com.dorm.dto.TransferDTO;
import com.dorm.entity.AccomCheckin;
import com.dorm.entity.AccomCheckout;
import com.dorm.entity.AccomTransfer;
import com.dorm.service.AccomService;
import com.dorm.vo.CheckinVO;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccomController {
    private final AccomService accomService;

    @PostMapping("/checkins")
    public R<AccomCheckin> checkin(@RequestBody CheckinDTO dto) {
        return R.ok(accomService.checkin(dto));
    }

    @GetMapping("/checkins")
    public R<Page<CheckinVO>> listCheckins(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) Integer status) {
        return R.ok(accomService.listCheckins(page, size, status));
    }

    @PostMapping("/transfers")
    public R<AccomTransfer> transfer(@RequestBody TransferDTO dto) {
        return R.ok(accomService.transfer(dto));
    }

    @PostMapping("/checkouts")
    public R<AccomCheckout> checkout(@RequestBody CheckoutDTO dto) {
        return R.ok(accomService.checkout(dto));
    }
    @GetMapping("/rooms/{roomId}/students")
    public R<List<CheckinVO>> getRoomStudents(@PathVariable Long roomId) {
        return R.ok(accomService.listStudentsByRoomId(roomId));
    }
}