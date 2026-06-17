package com.smartvocab.module.learn.dto;

import lombok.Data;

@Data
public class LearnRecordDTO {
    private Long bookId;
    private Long wordId;
    /** FAMILIAR / HAZY / STRANGE */
    private String status;
    private Integer durationMs;
}
