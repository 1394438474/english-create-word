package com.smartvocab.module.errorbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartvocab.module.errorbook.entity.ErrorWord;
import com.smartvocab.module.errorbook.mapper.ErrorWordMapper;
import com.smartvocab.module.errorbook.service.ErrorBookService;
import com.smartvocab.module.word.dto.WordCardDTO;
import com.smartvocab.module.word.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ErrorBookServiceImpl extends ServiceImpl<ErrorWordMapper, ErrorWord> implements ErrorBookService {

    private final WordService wordService;

    @Override
    public void addError(Long userId, Long wordId, String quizType) {
        ErrorWord e = getOne(new LambdaQueryWrapper<ErrorWord>()
                .eq(ErrorWord::getUserId, userId).eq(ErrorWord::getWordId, wordId));
        if (e == null) {
            e = new ErrorWord();
            e.setUserId(userId);
            e.setWordId(wordId);
            e.setErrorCount(1);
            e.setQuizType(quizType);
            e.setLastErrorTime(LocalDateTime.now());
            save(e);
        } else {
            e.setErrorCount(e.getErrorCount() + 1);
            e.setQuizType(quizType);
            e.setLastErrorTime(LocalDateTime.now());
            updateById(e);
        }
    }

    @Override
    public List<WordCardDTO> listErrors(Long userId, String quizType) {
        LambdaQueryWrapper<ErrorWord> q = new LambdaQueryWrapper<ErrorWord>()
                .eq(ErrorWord::getUserId, userId).orderByDesc(ErrorWord::getErrorCount);
        if (quizType != null && !quizType.isEmpty()) q.eq(ErrorWord::getQuizType, quizType);
        List<Long> ids = list(q).stream().map(ErrorWord::getWordId).collect(Collectors.toList());
        return wordService.listByIds(ids, userId);
    }

    @Override
    public void remove(Long userId, Long wordId) {
        remove(new LambdaQueryWrapper<ErrorWord>().eq(ErrorWord::getUserId, userId).eq(ErrorWord::getWordId, wordId));
    }
}
