package com.smartvocab.module.vocab.service;

import com.smartvocab.module.vocab.entity.VocabNote;
import com.smartvocab.module.word.dto.WordCardDTO;

import java.util.List;
import java.util.Map;

public interface VocabService {
    void addOrUpdate(Long userId, Long wordId, String note, Integer star, Integer isMarked);
    void updateNote(Long userId, Long wordId, String note);
    void updateStar(Long userId, Long wordId, Integer star);
    List<Map<String, Object>> listVocab(Long userId);
    void batchRemove(Long userId, List<Long> wordIds);
    Map<String, Object> exportVocab(Long userId);
}
