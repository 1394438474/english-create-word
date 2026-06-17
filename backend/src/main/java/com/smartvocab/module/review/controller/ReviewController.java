package com.smartvocab.module.review.controller;

import com.smartvocab.common.R;
import com.smartvocab.common.SecurityContext;
import com.smartvocab.module.learn.dto.LearnRecordDTO;
import com.smartvocab.module.learn.service.LearnService;
import com.smartvocab.module.word.dto.WordCardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final LearnService learnService;

    @GetMapping("/today")
    public R<List<WordCardDTO>> today(@RequestParam(defaultValue = "30") int limit) {
        return R.ok(learnService.todayReview(SecurityContext.getUserId(), limit));
    }

    @PostMapping("/record")
    public R<Void> record(@RequestBody LearnRecordDTO dto) {
        learnService.recordReview(SecurityContext.getUserId(), dto);
        return R.ok();
    }
}
