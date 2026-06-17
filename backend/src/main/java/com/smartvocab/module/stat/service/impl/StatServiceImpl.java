package com.smartvocab.module.stat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartvocab.module.book.entity.Book;
import com.smartvocab.module.book.entity.UserBook;
import com.smartvocab.module.book.mapper.BookMapper;
import com.smartvocab.module.book.mapper.UserBookMapper;
import com.smartvocab.module.checkin.entity.CheckIn;
import com.smartvocab.module.checkin.mapper.CheckInMapper;
import com.smartvocab.module.errorbook.entity.ErrorWord;
import com.smartvocab.module.errorbook.mapper.ErrorWordMapper;
import com.smartvocab.module.learn.entity.LearnRecord;
import com.smartvocab.module.learn.mapper.LearnRecordMapper;
import com.smartvocab.module.quiz.entity.QuizRecord;
import com.smartvocab.module.quiz.mapper.QuizRecordMapper;
import com.smartvocab.module.stat.service.StatService;
import com.smartvocab.module.user.entity.User;
import com.smartvocab.module.user.service.UserService;
import com.smartvocab.module.vocab.entity.VocabNote;
import com.smartvocab.module.vocab.mapper.VocabNoteMapper;
import com.smartvocab.module.word.entity.Word;
import com.smartvocab.module.word.mapper.WordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    private final UserService userService;
    private final UserBookMapper userBookMapper;
    private final BookMapper bookMapper;
    private final LearnRecordMapper learnRecordMapper;
    private final ErrorWordMapper errorWordMapper;
    private final VocabNoteMapper vocabNoteMapper;
    private final CheckInMapper checkInMapper;
    private final QuizRecordMapper quizRecordMapper;
    private final WordMapper wordMapper;

    @Override
    public Map<String, Object> dashboard(Long userId) {
        User u = userService.getById(userId);
        // 学习概览
        long totalLearned = learnRecordMapper.selectCount(new LambdaQueryWrapper<LearnRecord>().eq(LearnRecord::getUserId, userId));
        long mastered = learnRecordMapper.selectCount(new LambdaQueryWrapper<LearnRecord>()
                .eq(LearnRecord::getUserId, userId).in(LearnRecord::getStatus, "FAMILIAR", "MASTERED"));
        long errors = errorWordMapper.selectCount(new LambdaQueryWrapper<ErrorWord>().eq(ErrorWord::getUserId, userId));
        long vocabs = vocabNoteMapper.selectCount(new LambdaQueryWrapper<VocabNote>().eq(VocabNote::getUserId, userId));
        long todayReview = learnRecordMapper.selectCount(new LambdaQueryWrapper<LearnRecord>()
                .eq(LearnRecord::getUserId, userId).le(LearnRecord::getNextReviewTime, LocalDateTime.now()));

        // 词书进度
        List<UserBook> userBooks = userBookMapper.selectList(new LambdaQueryWrapper<UserBook>().eq(UserBook::getUserId, userId));
        List<Map<String, Object>> bookProgress = new ArrayList<>();
        for (UserBook ub : userBooks) {
            Book b = bookMapper.selectById(ub.getBookId());
            if (b == null) continue;
            long total = wordMapper.selectCount(new LambdaQueryWrapper<Word>().eq(Word::getBookId, b.getId()));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("bookId", b.getId());
            item.put("bookName", b.getName());
            item.put("color", b.getColor());
            item.put("learned", ub.getLearnedCount() == null ? 0 : ub.getLearnedCount());
            item.put("mastered", ub.getMasteredCount() == null ? 0 : ub.getMasteredCount());
            item.put("total", total);
            item.put("progress", total == 0 ? 0 : Math.round((ub.getLearnedCount() == null ? 0 : ub.getLearnedCount()) * 100.0 / total));
            bookProgress.add(item);
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("nickname", u.getNickname());
        res.put("avatar", u.getAvatar());
        res.put("streakDays", u.getStreakDays());
        res.put("totalDays", u.getTotalDays());
        res.put("totalLearned", totalLearned);
        res.put("mastered", mastered);
        res.put("errorCount", errors);
        res.put("vocabCount", vocabs);
        res.put("todayReview", todayReview);
        res.put("bookProgress", bookProgress);
        return res;
    }

    @Override
    public List<Map<String, Object>> trend(Long userId, int days) {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            LocalDateTime start = d.atStartOfDay();
            LocalDateTime end = d.plusDays(1).atStartOfDay().minusNanos(1);
            Long learned = learnRecordMapper.selectCount(new LambdaQueryWrapper<LearnRecord>()
                    .eq(LearnRecord::getUserId, userId).between(LearnRecord::getCreateTime, start, end));
            CheckIn ci = checkInMapper.selectOne(new LambdaQueryWrapper<CheckIn>()
                    .eq(CheckIn::getUserId, userId).eq(CheckIn::getDay, d));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", d.toString());
            item.put("learned", learned);
            item.put("checked", ci != null);
            list.add(item);
        }
        return list;
    }

    @Override
    public Map<String, Object> mastery(Long userId) {
        long familiar = learnRecordMapper.selectCount(new LambdaQueryWrapper<LearnRecord>()
                .eq(LearnRecord::getUserId, userId).eq(LearnRecord::getStatus, "FAMILIAR"));
        long hazy = learnRecordMapper.selectCount(new LambdaQueryWrapper<LearnRecord>()
                .eq(LearnRecord::getUserId, userId).eq(LearnRecord::getStatus, "HAZY"));
        long strange = learnRecordMapper.selectCount(new LambdaQueryWrapper<LearnRecord>()
                .eq(LearnRecord::getUserId, userId).eq(LearnRecord::getStatus, "STRANGE"));
        long mastered = learnRecordMapper.selectCount(new LambdaQueryWrapper<LearnRecord>()
                .eq(LearnRecord::getUserId, userId).eq(LearnRecord::getStatus, "MASTERED"));
        long newCount = learnRecordMapper.selectCount(new LambdaQueryWrapper<LearnRecord>()
                .eq(LearnRecord::getUserId, userId).eq(LearnRecord::getStatus, "NEW"));
        List<Map<String, Object>> items = List.of(
                Map.of("name", "已掌握", "value", mastered),
                Map.of("name", "熟悉", "value", familiar),
                Map.of("name", "模糊", "value", hazy),
                Map.of("name", "陌生", "value", strange),
                Map.of("name", "未学", "value", newCount)
        );
        return Map.of("items", items, "total", mastered + familiar + hazy + strange + newCount);
    }

    @Override
    public List<Map<String, Object>> weak(Long userId, int limit) {
        List<ErrorWord> errs = errorWordMapper.selectList(new LambdaQueryWrapper<ErrorWord>()
                .eq(ErrorWord::getUserId, userId)
                .orderByDesc(ErrorWord::getErrorCount)
                .last("LIMIT " + limit));
        return errs.stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("wordId", e.getWordId());
            m.put("errorCount", e.getErrorCount());
            m.put("quizType", e.getQuizType());
            m.put("lastErrorTime", e.getLastErrorTime());
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> radar(Long userId) {
        long totalQuiz = quizRecordMapper.selectCount(new LambdaQueryWrapper<QuizRecord>().eq(QuizRecord::getUserId, userId));
        long err = errorWordMapper.selectCount(new LambdaQueryWrapper<ErrorWord>().eq(ErrorWord::getUserId, userId));
        long mastered = learnRecordMapper.selectCount(new LambdaQueryWrapper<LearnRecord>()
                .eq(LearnRecord::getUserId, userId).in(LearnRecord::getStatus, "FAMILIAR", "MASTERED"));
        long vocabs = vocabNoteMapper.selectCount(new LambdaQueryWrapper<VocabNote>().eq(VocabNote::getUserId, userId));
        long total = learnRecordMapper.selectCount(new LambdaQueryWrapper<LearnRecord>().eq(LearnRecord::getUserId, userId));
        long review = learnRecordMapper.selectCount(new LambdaQueryWrapper<LearnRecord>()
                .eq(LearnRecord::getUserId, userId).le(LearnRecord::getNextReviewTime, LocalDateTime.now()));

        double accuracy = totalQuiz == 0 ? 0.85 : Math.min(1.0, 1.0 - err * 0.05);
        double masterRate = total == 0 ? 0.0 : mastered * 1.0 / total;
        double activity = Math.min(1.0, vocabs / 100.0);
        double stability = total == 0 ? 0.5 : Math.min(1.0, mastered * 1.0 / (total + 1));
        double consistency = Math.min(1.0, review * 1.0 / 30.0);
        double breadth = Math.min(1.0, total * 1.0 / 200.0);

        return Map.of(
                "indicator", List.of("准确率", "掌握度", "活跃度", "稳定性", "连续性", "广度"),
                "value", List.of(round(accuracy), round(masterRate), round(activity), round(stability), round(consistency), round(breadth))
        );
    }

    private static double round(double v) { return Math.round(v * 1000.0) / 1000.0; }
}
