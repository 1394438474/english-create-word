package com.smartvocab.module.checkin.service;

import java.util.List;
import java.util.Map;

public interface CheckInService {
    Map<String, Object> doCheckIn(Long userId, int learnedCount, int durationMs);
    Map<String, Object> getCalendar(Long userId, int year, int month);
    List<Map<String, Object>> listMedals(Long userId);
}
