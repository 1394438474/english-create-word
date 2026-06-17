package com.smartvocab.module.quiz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartvocab.common.BizException;
import com.smartvocab.module.errorbook.service.ErrorBookService;
import com.smartvocab.module.quiz.dto.QuizDTO;
import com.smartvocab.module.quiz.entity.QuizRecord;
import com.smartvocab.module.quiz.mapper.QuizRecordMapper;
import com.smartvocab.module.quiz.service.QuizService;
import com.smartvocab.module.word.entity.Word;
import com.smartvocab.module.word.entity.WordMeaning;
import com.smartvocab.module.word.mapper.WordMapper;
import com.smartvocab.module.word.mapper.WordMeaningMapper;
import com.smartvocab.module.word.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final WordMapper wordMapper;
    private final WordMeaningMapper meaningMapper;
    private final WordService wordService;
    private final ErrorBookService errorBookService;
    private final QuizRecordMapper quizRecordMapper;

    @Override
    public QuizDTO generate(Long userId, String type, Long bookId, int size) {
        List<Word> allWords = wordMapper.selectList(new LambdaQueryWrapper<Word>().eq(Word::getBookId, bookId));
        if (allWords.size() < 4) throw new BizException("词书单词不足 4 个，无法生成测试");
        Collections.shuffle(allWords);
        List<Word> chosen = allWords.subList(0, Math.min(size, allWords.size()));
        List<Word> pool = new ArrayList<>(allWords);
        String t = type == null ? "EN_ZH" : type;

        List<QuizDTO.Question> qs = new ArrayList<>();
        for (Word w : chosen) {
            QuizDTO.Question q = new QuizDTO.Question();
            q.setWordId(w.getId());
            q.setSpelling(w.getSpelling());
            q.setPhonetic(w.getPhoneticUs());
            q.setImageUrl(w.getImageUrl());

            List<WordMeaning> means = meaningMapper.selectList(
                    new LambdaQueryWrapper<WordMeaning>().eq(WordMeaning::getWordId, w.getId()).orderByAsc(WordMeaning::getSort));
            String correctMeaning = means.isEmpty() ? w.getSpelling() : means.get(0).getMeaningZh();

            // 正确选项引用（用于在洗牌后定位）
            QuizDTO.Option correctOpt = new QuizDTO.Option();
            List<QuizDTO.Option> options = new ArrayList<>();
            switch (t) {
                case "EN_ZH" -> {
                    q.setPrompt(w.getSpelling());
                    correctOpt = opt("A", correctMeaning, null);
                    options.add(correctOpt);
                    for (Word o : pickOther(pool, w, 3)) {
                        WordMeaning om = firstMeaning(o.getId());
                        options.add(opt("X", om == null ? o.getSpelling() : om.getMeaningZh(), null));
                    }
                    q.setPrompt(w.getSpelling());
                }
                case "ZH_EN" -> {
                    q.setPrompt(correctMeaning);
                    correctOpt = opt("A", w.getSpelling(), null);
                    options.add(correctOpt);
                    for (Word o : pickOther(pool, w, 3)) {
                        options.add(opt("X", o.getSpelling(), null));
                    }
                }
                case "CLOZE" -> {
                    String en = "The word " + w.getSpelling() + " means " + correctMeaning + ".";
                    q.setPrompt(en);
                    correctOpt = opt("A", w.getSpelling(), null);
                    options.add(correctOpt);
                    for (Word o : pickOther(pool, w, 3)) {
                        options.add(opt("X", o.getSpelling(), null));
                    }
                }
                default -> throw new BizException("不支持的题型");
            }
            Collections.shuffle(options);
            // 通过对象引用找到正确选项在洗牌后的位置
            int answerIdx = options.indexOf(correctOpt);
            // 重新打 label A/B/C/D
            for (int i = 0; i < options.size(); i++) {
                options.get(i).setLabel(String.valueOf((char) ('A' + i)));
            }
            q.setOptions(options);
            q.setAnswerIndex(answerIdx);
            qs.add(q);
        }
        QuizDTO dto = new QuizDTO();
        dto.setType(t);
        dto.setBookId(bookId);
        dto.setSize(qs.size());
        dto.setQuestions(qs);
        return dto;
    }

    private static QuizDTO.Option opt(String label, String text, String img) {
        QuizDTO.Option o = new QuizDTO.Option();
        o.setLabel(label); o.setText(text); o.setImageUrl(img); return o;
    }

    private List<Word> pickOther(List<Word> pool, Word cur, int n) {
        List<Word> copy = new ArrayList<>(pool);
        copy.removeIf(w -> w.getId().equals(cur.getId()));
        Collections.shuffle(copy);
        return copy.subList(0, Math.min(n, copy.size()));
    }

    private WordMeaning firstMeaning(Long wordId) {
        return meaningMapper.selectOne(new LambdaQueryWrapper<WordMeaning>().eq(WordMeaning::getWordId, wordId).orderByAsc(WordMeaning::getSort).last("LIMIT 1"));
    }

    @Override
    @Transactional
    public QuizDTO submit(Long userId, QuizDTO dto) {
        int correct = 0;
        List<QuizDTO.Answer> answers = new ArrayList<>();
        for (QuizDTO.Question q : dto.getQuestions()) {
            QuizDTO.Answer a = new QuizDTO.Answer();
            a.setWordId(q.getWordId());
            // 前端用 label "A" 表示选中的选项。我们重新映射 label -> index
            String chosenLabel = dto.getChosenLabels() != null ? dto.getChosenLabels().get(q.getWordId()) : null;
            int chosenIdx = -1;
            if (chosenLabel != null) {
                for (int i = 0; i < q.getOptions().size(); i++) {
                    if (chosenLabel.equals(q.getOptions().get(i).getLabel())) {
                        chosenIdx = i; break;
                    }
                }
            }
            a.setChosen(chosenIdx);
            boolean ok = chosenIdx == q.getAnswerIndex();
            a.setCorrect(ok);
            if (ok) correct++;
            else errorBookService.addError(userId, q.getWordId(), dto.getType());
            answers.add(a);
        }
        int total = dto.getQuestions().size();
        int score = total == 0 ? 0 : (int) Math.round(correct * 100.0 / total);
        QuizRecord r = new QuizRecord();
        r.setUserId(userId);
        r.setQuizType(dto.getType());
        r.setBookId(dto.getBookId());
        r.setTotal(total);
        r.setCorrect(correct);
        r.setScore(score);
        r.setDurationMs(dto.getDurationMs());
        r.setCreateTime(LocalDateTime.now());
        quizRecordMapper.insert(r);

        QuizDTO out = new QuizDTO();
        out.setType(dto.getType());
        out.setBookId(dto.getBookId());
        out.setTotal(total);
        out.setCorrect(correct);
        out.setScore(score);
        out.setDurationMs(dto.getDurationMs());
        out.setAnswers(answers);
        return out;
    }
}
