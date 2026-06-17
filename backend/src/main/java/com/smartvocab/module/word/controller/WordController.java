package com.smartvocab.module.word.controller;

import com.smartvocab.common.PageResult;
import com.smartvocab.common.R;
import com.smartvocab.common.SecurityContext;
import com.smartvocab.module.word.dto.WordCardDTO;
import com.smartvocab.module.word.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    @GetMapping("/book/{bookId}")
    public R<PageResult<WordCardDTO>> listByBook(@PathVariable Long bookId,
                                                 @RequestParam(defaultValue = "1") int current,
                                                 @RequestParam(defaultValue = "20") int size) {
        return R.ok(wordService.pageWords(bookId, current, size, SecurityContext.getUserId()));
    }

    @GetMapping("/{id}")
    public R<WordCardDTO> detail(@PathVariable Long id) {
        return R.ok(wordService.detail(id, SecurityContext.getUserId()));
    }

    @PostMapping("/batch")
    public R<List<WordCardDTO>> batch(@RequestBody List<Long> ids) {
        return R.ok(wordService.listByIds(ids, SecurityContext.getUserId()));
    }
}
