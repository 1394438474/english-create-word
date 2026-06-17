package com.smartvocab.module.quiz.controller;

import com.smartvocab.common.R;
import com.smartvocab.common.SecurityContext;
import com.smartvocab.module.quiz.dto.QuizDTO;
import com.smartvocab.module.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/generate")
    public R<QuizDTO> generate(@RequestParam(required = false, defaultValue = "EN_ZH") String type,
                               @RequestParam Long bookId,
                               @RequestParam(defaultValue = "10") int size) {
        return R.ok(quizService.generate(SecurityContext.getUserId(), type, bookId, size));
    }

    @PostMapping("/submit")
    public R<QuizDTO> submit(@RequestBody QuizDTO dto) {
        return R.ok(quizService.submit(SecurityContext.getUserId(), dto));
    }
}
