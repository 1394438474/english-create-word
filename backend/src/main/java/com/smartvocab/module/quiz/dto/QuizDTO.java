package com.smartvocab.module.quiz.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QuizDTO {
    /** EN_ZH 看英选义  / ZH_EN 看义选英 / CLOZE 选词填空 */
    private String type;
    private Long bookId;
    private int size;
    private List<Question> questions;
    private Integer total;
    private Integer correct;
    private Integer score;
    private Integer durationMs;
    private List<Answer> answers;
    /** 前端提交时使用，key=wordId, value=选中的 label "A"/"B"/... */
    private Map<Long, String> chosenLabels;

    @Data
    public static class Question {
        private Long wordId;
        private String spelling;
        private String phonetic;
        private String imageUrl;
        private String prompt;
        private List<Option> options;
        private Integer answerIndex;
    }

    @Data
    public static class Option {
        private String label;
        private String text;
        private String imageUrl;
    }

    @Data
    public static class Answer {
        private Long wordId;
        private Integer chosen;
        private boolean correct;
    }
}
