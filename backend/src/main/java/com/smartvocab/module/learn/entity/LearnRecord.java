package com.smartvocab.module.learn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learn_record")
public class LearnRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long wordId;
    private Long bookId;
    /** NEW / FAMILIAR / HAZY / STRANGE / MASTERED */
    private String status;
    private Double stability;
    private Double difficulty;
    private Integer reviewCount;
    private Integer durationMs;
    private LocalDateTime nextReviewTime;
    private LocalDateTime lastReviewTime;
    private LocalDateTime createTime;
}
