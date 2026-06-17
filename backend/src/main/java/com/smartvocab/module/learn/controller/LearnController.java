package com.smartvocab.module.learn.controller;

import com.smartvocab.common.R;
import com.smartvocab.common.SecurityContext;
import com.smartvocab.module.learn.dto.LearnRecordDTO;
import com.smartvocab.module.learn.service.LearnService;
import com.smartvocab.module.word.dto.WordCardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/learn")
@RequiredArgsConstructor
public class LearnController {

    private final LearnService learnService;

    @GetMapping("/today")
    public R<List<WordCardDTO>> today(@RequestParam Long bookId,
                                      @RequestParam(defaultValue = "20") int limit) {
        return R.ok(learnService.todayLearn(SecurityContext.getUserId(), bookId, limit));
    }

    @PostMapping("/record")
    public R<Void> record(@RequestBody LearnRecordDTO dto) {
        learnService.record(SecurityContext.getUserId(), dto);
        return R.ok();
    }
}
