package com.smartvocab.module.checkin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartvocab.common.BizException;
import com.smartvocab.module.checkin.entity.CheckIn;
import com.smartvocab.module.checkin.entity.Medal;
import com.smartvocab.module.checkin.entity.UserMedal;
import com.smartvocab.module.checkin.mapper.CheckInMapper;
import com.smartvocab.module.checkin.mapper.MedalMapper;
import com.smartvocab.module.checkin.mapper.UserMedalMapper;
import com.smartvocab.module.checkin.service.CheckInService;
import com.smartvocab.module.user.entity.User;
import com.smartvocab.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CheckInServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements CheckInService {

    private final MedalMapper medalMapper;
    private final UserMedalMapper userMedalMapper;
    private final UserService userService;

    @Override
    @Transactional
    public Map<String, Object> doCheckIn(Long userId, int learnedCount, int durationMs) {
        LocalDate today = LocalDate.now();
        User u = userService.getById(userId);
        if (u.getLastCheckin() != null && u.getLastCheckin().equals(today)) {
            throw new BizException("今天已经打卡，明天再来吧～");
        }
        CheckIn ci = new CheckIn();
        ci.setUserId(userId);
        ci.setDay(today);
        ci.setLearnedCount(learnedCount);
        ci.setDurationMs(durationMs);
        save(ci);

        // 连续天数
        if (u.getLastCheckin() != null && ChronoUnit.DAYS.between(u.getLastCheckin(), today) == 1) {
            u.setStreakDays(u.getStreakDays() == null ? 1 : u.getStreakDays() + 1);
        } else {
            u.setStreakDays(1);
        }
        u.setTotalDays(u.getTotalDays() == null ? 1 : u.getTotalDays() + 1);
        u.setLastCheckin(today);
        userService.updateById(u);

        // 解锁勋章
        List<String> unlocked = unlockMedals(userId, u);
        ci.setMedalCode(unlocked.isEmpty() ? null : unlocked.get(0));
        updateById(ci);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("streakDays", u.getStreakDays());
        res.put("totalDays", u.getTotalDays());
        res.put("unlockedMedals", unlocked);
        return res;
    }

    private List<String> unlockMedals(Long userId, User u) {
        List<String> unlocked = new ArrayList<>();
        List<Medal> medals = medalMapper.selectList(null);
        for (Medal m : medals) {
            UserMedal exist = userMedalMapper.selectOne(new LambdaQueryWrapper<UserMedal>()
                    .eq(UserMedal::getUserId, userId).eq(UserMedal::getMedalCode, m.getCode()));
            if (exist != null) continue;
            boolean match = switch (m.getConditionType() == null ? "" : m.getConditionType()) {
                case "STREAK" -> u.getStreakDays() != null && u.getStreakDays() >= m.getConditionValue();
                case "COUNT" -> u.getTotalDays() != null && u.getTotalDays() * 20 >= m.getConditionValue();
                default -> false;
            };
            if (match) {
                UserMedal um = new UserMedal();
                um.setUserId(userId);
                um.setMedalCode(m.getCode());
                um.setAchieveTime(LocalDateTime.now());
                userMedalMapper.insert(um);
                unlocked.add(m.getCode());
            }
        }
        return unlocked;
    }

    @Override
    public Map<String, Object> getCalendar(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<CheckIn> records = list(new LambdaQueryWrapper<CheckIn>()
                .eq(CheckIn::getUserId, userId)
                .between(CheckIn::getDay, start, end));
        Map<String, Integer> map = new LinkedHashMap<>();
        for (CheckIn r : records) {
            map.put(r.getDay().toString(), r.getLearnedCount());
        }
        User u = userService.getById(userId);
        return Map.of(
                "year", year,
                "month", month,
                "days", map,
                "streakDays", u.getStreakDays() == null ? 0 : u.getStreakDays(),
                "totalDays", u.getTotalDays() == null ? 0 : u.getTotalDays()
        );
    }

    @Override
    public List<Map<String, Object>> listMedals(Long userId) {
        List<Medal> medals = medalMapper.selectList(null);
        List<UserMedal> owns = userMedalMapper.selectList(new LambdaQueryWrapper<UserMedal>().eq(UserMedal::getUserId, userId));
        Set<String> owned = new HashSet<>();
        for (UserMedal um : owns) owned.add(um.getMedalCode());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Medal m : medals) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", m.getCode());
            item.put("name", m.getName());
            item.put("description", m.getDescription());
            item.put("icon", m.getIcon());
            item.put("conditionType", m.getConditionType());
            item.put("conditionValue", m.getConditionValue());
            item.put("owned", owned.contains(m.getCode()));
            result.add(item);
        }
        return result;
    }
}
