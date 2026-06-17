package com.smartvocab.module.quiz.service;

import com.smartvocab.module.quiz.dto.QuizDTO;

public interface QuizService {
    QuizDTO generate(Long userId, String type, Long bookId, int size);
    QuizDTO submit(Long userId, QuizDTO dto);
}
