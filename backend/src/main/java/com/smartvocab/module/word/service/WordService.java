package com.smartvocab.module.word.service;

import com.smartvocab.common.PageResult;
import com.smartvocab.module.word.dto.WordCardDTO;

import java.util.List;

public interface WordService {
    PageResult<WordCardDTO> pageWords(Long bookId, int current, int size, Long userId);
    WordCardDTO detail(Long wordId, Long userId);
    List<WordCardDTO> listByIds(List<Long> ids, Long userId);
}
