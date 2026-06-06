package com.dorm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.common.R;
import com.dorm.dto.RepairDTO;
import com.dorm.dto.RepairProcessDTO;
import com.dorm.entity.RepairProcess;
import com.dorm.entity.RepairRequest;
import com.dorm.service.RepairService;
import com.dorm.vo.RepairRequestVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RepairController {
    private final RepairService repairService;

    @PostMapping("/repairs")
    public R<RepairRequest> submit(@RequestBody RepairDTO dto, HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        return R.ok(repairService.submit(dto, studentId));
    }

    @GetMapping("/repairs")
    public R<Page<RepairRequestVO>> listMyRepairs(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        return R.ok(repairService.listMyRepairs(page, size, studentId));
    }

    @GetMapping("/repairs/all")
    public R<Page<RepairRequestVO>> listAllRepairs(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(required = false) Integer status) {
        return R.ok(repairService.listAllRepairs(page, size, status));
    }

    @GetMapping("/repairs/{id}/history")
    public R<List<RepairProcess>> getProcessHistory(@PathVariable Long id) {
        return R.ok(repairService.getProcessHistory(id));
    }

    @PutMapping("/repairs/{id}/process")
    public R<Void> processRepair(@PathVariable Long id, @RequestBody RepairProcessDTO dto,
                                  HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        repairService.processRepair(id, dto, adminId);
        return R.ok();
    }

    @PutMapping("/repairs/{id}/cancel")
    public R<Void> cancelRepair(@PathVariable Long id, HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        repairService.cancelRepair(id, studentId);
        return R.ok();
    }
}
