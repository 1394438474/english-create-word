package com.smartvocab.module.vocab.controller;

import com.smartvocab.common.R;
import com.smartvocab.common.SecurityContext;
import com.smartvocab.module.vocab.service.VocabService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vocab")
@RequiredArgsConstructor
public class VocabController {

    private final VocabService vocabService;

    @GetMapping
    public R<List<Map<String, Object>>> list() {
        return R.ok(vocabService.listVocab(SecurityContext.getUserId()));
    }

    @PostMapping("/add")
    public R<Void> add(@RequestBody Map<String, Object> body) {
        Long wordId = Long.valueOf(body.get("wordId").toString());
        vocabService.addOrUpdate(SecurityContext.getUserId(), wordId,
                (String) body.get("note"),
                body.get("star") == null ? null : Integer.valueOf(body.get("star").toString()),
                body.get("isMarked") == null ? null : Integer.valueOf(body.get("isMarked").toString()));
        return R.ok();
    }

    @PostMapping("/{wordId}/note")
    public R<Void> updateNote(@PathVariable Long wordId, @RequestBody Map<String, String> body) {
        vocabService.updateNote(SecurityContext.getUserId(), wordId, body.get("note"));
        return R.ok();
    }

    @PostMapping("/{wordId}/star")
    public R<Void> updateStar(@PathVariable Long wordId, @RequestBody Map<String, Integer> body) {
        vocabService.updateStar(SecurityContext.getUserId(), wordId, body.get("star"));
        return R.ok();
    }

    @DeleteMapping("/batch")
    public R<Void> batchRemove(@RequestBody List<Long> wordIds) {
        vocabService.batchRemove(SecurityContext.getUserId(), wordIds);
        return R.ok();
    }

    @GetMapping("/export")
    public R<Map<String, Object>> export() {
        return R.ok(vocabService.exportVocab(SecurityContext.getUserId()));
    }
}
