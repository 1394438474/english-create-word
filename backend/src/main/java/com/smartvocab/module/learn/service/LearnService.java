package com.smartvocab.module.learn.service;

import com.smartvocab.module.learn.dto.LearnRecordDTO;
import com.smartvocab.module.word.dto.WordCardDTO;

import java.util.List;

public interface LearnService {
    List<WordCardDTO> todayLearn(Long userId, Long bookId, int limit);
    void record(Long userId, LearnRecordDTO dto);
    List<WordCardDTO> todayReview(Long userId, int limit);
    void recordReview(Long userId, LearnRecordDTO dto);
}
