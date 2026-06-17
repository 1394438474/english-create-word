package com.smartvocab.module.stat.service;

import java.util.List;
import java.util.Map;

public interface StatService {
    Map<String, Object> dashboard(Long userId);
    List<Map<String, Object>> trend(Long userId, int days);
    Map<String, Object> mastery(Long userId);
    List<Map<String, Object>> weak(Long userId, int limit);
    Map<String, Object> radar(Long userId);
}
