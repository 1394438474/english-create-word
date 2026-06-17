package com.smartvocab.module.stat.controller;

import com.smartvocab.common.R;
import com.smartvocab.common.SecurityContext;
import com.smartvocab.module.stat.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stat")
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;

    @GetMapping("/dashboard")
    public R<Map<String, Object>> dashboard() {
        return R.ok(statService.dashboard(SecurityContext.getUserId()));
    }

    @GetMapping("/trend")
    public R<List<Map<String, Object>>> trend(@RequestParam(defaultValue = "14") int days) {
        return R.ok(statService.trend(SecurityContext.getUserId(), days));
    }

    @GetMapping("/mastery")
    public R<Map<String, Object>> mastery() {
        return R.ok(statService.mastery(SecurityContext.getUserId()));
    }

    @GetMapping("/weak")
    public R<List<Map<String, Object>>> weak(@RequestParam(defaultValue = "10") int limit) {
        return R.ok(statService.weak(SecurityContext.getUserId(), limit));
    }

    @GetMapping("/radar")
    public R<Map<String, Object>> radar() {
        return R.ok(statService.radar(SecurityContext.getUserId()));
    }
}
