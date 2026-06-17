package com.smartvocab.module.errorbook.controller;

import com.smartvocab.common.R;
import com.smartvocab.common.SecurityContext;
import com.smartvocab.module.errorbook.service.ErrorBookService;
import com.smartvocab.module.word.dto.WordCardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/errorbook")
@RequiredArgsConstructor
public class ErrorBookController {

    private final ErrorBookService errorBookService;

    @GetMapping
    public R<List<WordCardDTO>> list(@RequestParam(required = false) String quizType) {
        return R.ok(errorBookService.listErrors(SecurityContext.getUserId(), quizType));
    }

    @PostMapping("/{wordId}/remove")
    public R<Void> remove(@PathVariable Long wordId) {
        errorBookService.remove(SecurityContext.getUserId(), wordId);
        return R.ok();
    }

    @PostMapping("/add")
    public R<Void> add(@RequestBody Map<String, Object> body) {
        Long wordId = Long.valueOf(body.get("wordId").toString());
        String type = body.getOrDefault("quizType", "TRANSLATE").toString();
        errorBookService.addError(SecurityContext.getUserId(), wordId, type);
        return R.ok();
    }
}
