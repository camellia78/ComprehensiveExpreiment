package com.dorm.controller;

import com.dorm.common.R;
import com.dorm.dto.CheckinDTO;
import com.dorm.dto.CheckoutDTO;
import com.dorm.dto.TransferDTO;
import com.dorm.entity.AccomCheckin;
import com.dorm.entity.AccomCheckout;
import com.dorm.entity.AccomTransfer;
import com.dorm.service.AccomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccomController {
    private final AccomService accomService;

    @PostMapping("/checkins")
    public R<AccomCheckin> checkin(@RequestBody CheckinDTO dto) { return R.ok(accomService.checkin(dto)); }

    @GetMapping("/checkins")
    public R<List<AccomCheckin>> listCheckins() { return R.ok(accomService.listCheckins()); }

    @PostMapping("/transfers")
    public R<AccomTransfer> transfer(@RequestBody TransferDTO dto) { return R.ok(accomService.transfer(dto)); }

    @PostMapping("/checkouts")
    public R<AccomCheckout> checkout(@RequestBody CheckoutDTO dto) { return R.ok(accomService.checkout(dto)); }
}
