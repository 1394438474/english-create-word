package com.smartvocab.module.vocab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartvocab.module.vocab.entity.VocabNote;
import com.smartvocab.module.vocab.mapper.VocabNoteMapper;
import com.smartvocab.module.vocab.service.VocabService;
import com.smartvocab.module.word.dto.WordCardDTO;
import com.smartvocab.module.word.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VocabServiceImpl extends ServiceImpl<VocabNoteMapper, VocabNote> implements VocabService {

    private final WordService wordService;

    @Override
    @Transactional
    public void addOrUpdate(Long userId, Long wordId, String note, Integer star, Integer isMarked) {
        VocabNote n = getOne(new LambdaQueryWrapper<VocabNote>().eq(VocabNote::getUserId, userId).eq(VocabNote::getWordId, wordId));
        if (n == null) {
            n = new VocabNote();
            n.setUserId(userId);
            n.setWordId(wordId);
            n.setNote(note);
            n.setStar(star == null ? 0 : star);
            n.setIsMarked(isMarked == null ? 0 : isMarked);
            save(n);
        } else {
            if (note != null) n.setNote(note);
            if (star != null) n.setStar(star);
            if (isMarked != null) n.setIsMarked(isMarked);
            updateById(n);
        }
    }

    @Override
    public void updateNote(Long userId, Long wordId, String note) {
        addOrUpdate(userId, wordId, note, null, null);
    }

    @Override
    public void updateStar(Long userId, Long wordId, Integer star) {
        addOrUpdate(userId, wordId, null, star, null);
    }

    @Override
    public List<Map<String, Object>> listVocab(Long userId) {
        List<VocabNote> notes = list(new LambdaQueryWrapper<VocabNote>().eq(VocabNote::getUserId, userId).orderByDesc(VocabNote::getUpdateTime));
        if (notes.isEmpty()) return List.of();
        List<Long> ids = notes.stream().map(VocabNote::getWordId).toList();
        Map<Long, WordCardDTO> wordMap = wordService.listByIds(ids, userId).stream()
                .collect(Collectors.toMap(WordCardDTO::getId, w -> w, (a, b) -> a));
        List<Map<String, Object>> result = new ArrayList<>();
        for (VocabNote n : notes) {
            WordCardDTO w = wordMap.get(n.getWordId());
            if (w == null) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("word", w);
            m.put("note", n.getNote());
            m.put("star", n.getStar());
            m.put("isMarked", n.getIsMarked());
            m.put("updateTime", n.getUpdateTime());
            result.add(m);
        }
        return result;
    }

    @Override
    public void batchRemove(Long userId, List<Long> wordIds) {
        if (wordIds == null || wordIds.isEmpty()) return;
        remove(new LambdaQueryWrapper<VocabNote>().eq(VocabNote::getUserId, userId).in(VocabNote::getWordId, wordIds));
    }

    @Override
    public Map<String, Object> exportVocab(Long userId) {
        List<Map<String, Object>> list = listVocab(userId);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> item : list) {
            WordCardDTO w = (WordCardDTO) item.get("word");
            String meaning = w.getMeanings() == null ? "" : w.getMeanings().stream()
                    .map(x -> x.getMeaningZh() == null ? "" : x.getMeaningZh())
                    .collect(Collectors.joining("; "));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("spelling", w.getSpelling());
            row.put("phonetic", w.getPhoneticUs());
            row.put("meaning", meaning);
            row.put("note", item.get("note"));
            row.put("star", item.get("star"));
            rows.add(row);
        }
        return Map.of("total", rows.size(), "items", rows);
    }
}
