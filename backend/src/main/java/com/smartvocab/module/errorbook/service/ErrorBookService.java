package com.smartvocab.module.errorbook.service;

import com.smartvocab.module.word.dto.WordCardDTO;

import java.util.List;

public interface ErrorBookService {
    void addError(Long userId, Long wordId, String quizType);
    List<WordCardDTO> listErrors(Long userId, String quizType);
    void remove(Long userId, Long wordId);
}
