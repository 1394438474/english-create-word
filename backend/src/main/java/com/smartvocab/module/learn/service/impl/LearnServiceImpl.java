package com.smartvocab.module.learn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartvocab.module.algorithm.EbbinghausEngine;
import com.smartvocab.module.algorithm.MemoryState;
import com.smartvocab.module.book.entity.UserBook;
import com.smartvocab.module.book.mapper.UserBookMapper;
import com.smartvocab.module.learn.dto.LearnRecordDTO;
import com.smartvocab.module.learn.entity.LearnRecord;
import com.smartvocab.module.learn.mapper.LearnRecordMapper;
import com.smartvocab.module.learn.service.LearnService;
import com.smartvocab.module.word.dto.WordCardDTO;
import com.smartvocab.module.word.entity.Word;
import com.smartvocab.module.word.mapper.WordMapper;
import com.smartvocab.module.word.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearnServiceImpl implements LearnService {

    private final WordMapper wordMapper;
    private final LearnRecordMapper learnRecordMapper;
    private final UserBookMapper userBookMapper;
    private final WordService wordService;
    private final EbbinghausEngine ebbinghaus;

    @Override
    public List<WordCardDTO> todayLearn(Long userId, Long bookId, int limit) {
        // 已学过的 wordId
        Set<Long> learned = learnRecordMapper.selectList(new LambdaQueryWrapper<LearnRecord>()
                .eq(LearnRecord::getUserId, userId).eq(LearnRecord::getBookId, bookId))
                .stream().map(LearnRecord::getWordId).collect(Collectors.toSet());
        // 该词书中未学过的单词
        List<Word> words = wordMapper.selectList(new LambdaQueryWrapper<Word>()
                .eq(Word::getBookId, bookId).orderByAsc(Word::getId));
        List<Long> ids = words.stream().map(Word::getId).filter(id -> !learned.contains(id))
                .limit(limit).toList();
        return wordService.listByIds(ids, userId);
    }

    @Override
    @Transactional
    public void record(Long userId, LearnRecordDTO dto) {
        saveRecord(userId, dto, 0.0);
    }

    @Override
    public List<WordCardDTO> todayReview(Long userId, int limit) {
        // 智能复习池：next_review_time <= now
        List<LearnRecord> records = learnRecordMapper.selectList(new LambdaQueryWrapper<LearnRecord>()
                .eq(LearnRecord::getUserId, userId)
                .le(LearnRecord::getNextReviewTime, LocalDateTime.now())
                .orderByAsc(LearnRecord::getStability)
                .last("LIMIT " + limit));
        List<Long> ids = records.stream().map(LearnRecord::getWordId).toList();
        return wordService.listByIds(ids, userId);
    }

    @Override
    @Transactional
    public void recordReview(Long userId, LearnRecordDTO dto) {
        saveRecord(userId, dto, 0.0);
    }

    private void saveRecord(Long userId, LearnRecordDTO dto, double errorRate) {
        // 查找最近一条历史记录
        LearnRecord prev = learnRecordMapper.selectOne(new LambdaQueryWrapper<LearnRecord>()
                .eq(LearnRecord::getUserId, userId).eq(LearnRecord::getWordId, dto.getWordId())
                .orderByDesc(LearnRecord::getId).last("LIMIT 1"));
        double prevStability = prev == null ? 1.0 : prev.getStability();
        double prevDifficulty = prev == null ? 0.5 : prev.getDifficulty();
        int reviewCount = prev == null ? 0 : prev.getReviewCount();

        EbbinghausEngine.Status status = EbbinghausEngine.Status.valueOf(dto.getStatus());
        MemoryState state = ebbinghaus.compute(status, errorRate, dto.getDurationMs() == null ? 5000 : dto.getDurationMs(),
                reviewCount, prevStability, prevDifficulty,
                prev == null ? LocalDateTime.now() : prev.getLastReviewTime());

        LearnRecord r = new LearnRecord();
        r.setUserId(userId);
        r.setWordId(dto.getWordId());
        r.setBookId(dto.getBookId());
        r.setStatus(mapStatus(status, state.getConfidence()));
        r.setStability(state.getStability());
        r.setDifficulty(state.getDifficulty());
        r.setReviewCount(state.getReviewCount());
        r.setDurationMs(dto.getDurationMs());
        r.setLastReviewTime(state.getLastReviewTime());
        r.setNextReviewTime(state.getNextReviewTime());
        learnRecordMapper.insert(r);

        // 更新词书进度
        UserBook ub = userBookMapper.selectOne(new LambdaQueryWrapper<UserBook>()
                .eq(UserBook::getUserId, userId).eq(UserBook::getBookId, dto.getBookId()));
        if (ub == null) {
            ub = new UserBook();
            ub.setUserId(userId);
            ub.setBookId(dto.getBookId());
            ub.setCurrentIndex(0);
            ub.setLearnedCount(0);
            ub.setMasteredCount(0);
        }
        if (prev == null) {
            ub.setLearnedCount(ub.getLearnedCount() == null ? 1 : ub.getLearnedCount() + 1);
        }
        if (state.getConfidence() > 0.85) {
            ub.setMasteredCount(ub.getMasteredCount() == null ? 1 : ub.getMasteredCount() + 1);
        }
        ub.setLastStudyTime(LocalDateTime.now());
        if (ub.getId() == null) userBookMapper.insert(ub); else userBookMapper.updateById(ub);
    }

    private String mapStatus(EbbinghausEngine.Status s, double confidence) {
        if (s == EbbinghausEngine.Status.FAMILIAR && confidence > 0.8) return "MASTERED";
        return s.name();
    }
}
