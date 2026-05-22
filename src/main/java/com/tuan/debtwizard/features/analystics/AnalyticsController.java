/*package com.tuan.debtmanagement.controller;

import com.tuan.debtmanagement.dto.DtiResponse;
import com.tuan.debtmanagement.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

//@RestController
//@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * UC 4: Lấy tỷ lệ DTI của tháng hiện tại

    @GetMapping("/dti/{userId}")
    public ResponseEntity<DtiResponse> getCurrentDti(@PathVariable Long userId) {
        LocalDate now = LocalDate.now();

        DtiResponse response = analyticsService.calculateCurrentDti(
                userId,
                now.getMonthValue(),
                now.getYear()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy tỷ lệ DTI của một tháng cụ thể (Dùng cho chức năng xem lịch sử)

    @GetMapping("/dti/{userId}/history")
    public ResponseEntity<DtiResponse> getHistoryDti(
            @PathVariable Long userId,// khóa chính
            @RequestParam int month,// tham số để lọc
            @RequestParam int year) {

        DtiResponse response = analyticsService.calculateCurrentDti(userId, month, year);
        return ResponseEntity.ok(response);
    }
}*/
