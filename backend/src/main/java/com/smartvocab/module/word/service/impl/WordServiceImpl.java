package com.smartvocab.module.word.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartvocab.common.BizException;
import com.smartvocab.common.PageResult;
import com.smartvocab.module.learn.entity.LearnRecord;
import com.smartvocab.module.learn.mapper.LearnRecordMapper;
import com.smartvocab.module.word.dto.WordCardDTO;
import com.smartvocab.module.word.entity.Word;
import com.smartvocab.module.word.entity.WordMeaning;
import com.smartvocab.module.word.entity.WordSentence;
import com.smartvocab.module.word.mapper.WordMapper;
import com.smartvocab.module.word.mapper.WordMeaningMapper;
import com.smartvocab.module.word.mapper.WordSentenceMapper;
import com.smartvocab.module.word.service.WordService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WordServiceImpl extends ServiceImpl<WordMapper, Word> implements WordService {

    private final WordMeaningMapper meaningMapper;
    private final WordSentenceMapper sentenceMapper;
    private final LearnRecordMapper learnRecordMapper;

    public WordServiceImpl(WordMeaningMapper m, WordSentenceMapper s, LearnRecordMapper lr) {
        this.meaningMapper = m;
        this.sentenceMapper = s;
        this.learnRecordMapper = lr;
    }

    @Override
    public PageResult<WordCardDTO> pageWords(Long bookId, int current, int size, Long userId) {
        Page<Word> page = page(new Page<>(current, size),
                new LambdaQueryWrapper<Word>().eq(Word::getBookId, bookId).orderByAsc(Word::getId));
        List<Long> ids = page.getRecords().stream().map(Word::getId).toList();
        Map<Long, WordCardDTO> map = batchBuild(ids, userId);
        List<WordCardDTO> list = page.getRecords().stream().map(w -> map.get(w.getId())).toList();
        return PageResult.of(current, size, page.getTotal(), list);
    }

    @Override
    public WordCardDTO detail(Long wordId, Long userId) {
        Word w = getById(wordId);
        if (w == null) throw new BizException("单词不存在");
        return batchBuild(List.of(wordId), userId).get(wordId);
    }

    @Override
    public List<WordCardDTO> listByIds(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) return List.of();
        Map<Long, WordCardDTO> map = batchBuild(ids, userId);
        return ids.stream().map(map::get).filter(Objects::nonNull).toList();
    }

    /** 批量构建 WordCardDTO（释义、例句、学习状态） */
    private Map<Long, WordCardDTO> batchBuild(List<Long> ids, Long userId) {
        if (ids.isEmpty()) return Map.of();
        List<Word> words = listByIds(ids);
        List<WordMeaning> meanings = meaningMapper.selectList(
                new LambdaQueryWrapper<WordMeaning>().in(WordMeaning::getWordId, ids).orderByAsc(WordMeaning::getSort));
        List<WordSentence> sentences = sentenceMapper.selectList(
                new LambdaQueryWrapper<WordSentence>().in(WordSentence::getWordId, ids));
        Map<Long, List<WordMeaning>> meanMap = meanings.stream().collect(Collectors.groupingBy(WordMeaning::getWordId));
        Map<Long, List<WordSentence>> sentMap = sentences.stream().collect(Collectors.groupingBy(WordSentence::getWordId));

        Map<Long, LearnRecord> learnMap = Map.of();
        if (userId != null) {
            learnMap = learnRecordMapper.selectList(new LambdaQueryWrapper<LearnRecord>()
                    .eq(LearnRecord::getUserId, userId).in(LearnRecord::getWordId, ids)
                    .orderByDesc(LearnRecord::getId))
                .stream().collect(Collectors.toMap(LearnRecord::getWordId, l -> l, (a, b) -> a));
        }

        Map<Long, WordCardDTO> result = new LinkedHashMap<>();
        for (Word w : words) {
            WordCardDTO dto = new WordCardDTO();
            BeanUtils.copyProperties(w, dto);
            dto.setMeanings(meanMap.getOrDefault(w.getId(), List.of()).stream().map(m -> {
                WordCardDTO.Meaning x = new WordCardDTO.Meaning();
                BeanUtils.copyProperties(m, x);
                return x;
            }).toList());
            dto.setSentences(sentMap.getOrDefault(w.getId(), List.of()).stream().map(s -> {
                WordCardDTO.Sentence x = new WordCardDTO.Sentence();
                BeanUtils.copyProperties(s, x);
                return x;
            }).toList());
            LearnRecord lr = learnMap.get(w.getId());
            if (lr != null) {
                dto.setStatus(lr.getStatus());
                dto.setStability(lr.getStability());
                dto.setConfidence(lr.getStability() != null ? Math.min(1.0, lr.getStability() / 10.0) : 0.0);
            } else {
                dto.setStatus("NEW");
            }
            dto.setInVocab(false);
            dto.setError(false);
            result.put(w.getId(), dto);
        }
        return result;
    }
}
