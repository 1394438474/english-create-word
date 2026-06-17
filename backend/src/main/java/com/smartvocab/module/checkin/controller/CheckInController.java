package com.smartvocab.module.checkin.controller;

import com.smartvocab.common.R;
import com.smartvocab.common.SecurityContext;
import com.smartvocab.module.checkin.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    public R<Map<String, Object>> doCheckIn(@RequestBody Map<String, Integer> body) {
        return R.ok(checkInService.doCheckIn(SecurityContext.getUserId(),
                body.getOrDefault("learnedCount", 0),
                body.getOrDefault("durationMs", 0)));
    }

    @GetMapping("/calendar")
    public R<Map<String, Object>> calendar(@RequestParam int year, @RequestParam int month) {
        return R.ok(checkInService.getCalendar(SecurityContext.getUserId(), year, month));
    }

    @GetMapping("/medals")
    public R<List<Map<String, Object>>> medals() {
        return R.ok(checkInService.listMedals(SecurityContext.getUserId()));
    }
}
