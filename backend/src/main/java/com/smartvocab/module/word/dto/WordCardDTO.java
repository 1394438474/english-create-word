package com.smartvocab.module.word.dto;

import lombok.Data;

import java.util.List;

@Data
public class WordCardDTO {
    private Long id;
    private Long bookId;
    private String spelling;
    private String phoneticUs;
    private String phoneticUk;
    private String audioUrl;
    private String imageUrl;
    private String difficulty;
    private List<Meaning> meanings;
    private List<Sentence> sentences;
    private String status;       // NEW / FAMILIAR / HAZY / STRANGE
    private Double stability;    // 当前稳定性
    private Double confidence;   // 当前置信度
    private boolean inVocab;     // 是否在生词本
    private boolean isError;     // 是否在错题本

    @Data
    public static class Meaning {
        private String pos;
        private String meaningZh;
        private String meaningEn;
    }

    @Data
    public static class Sentence {
        private String en;
        private String zh;
        private String source;
    }
}
