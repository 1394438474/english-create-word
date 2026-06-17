package com.smartvocab.module.quiz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("quiz_record")
public class QuizRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String quizType;
    private Long bookId;
    private Integer total;
    private Integer correct;
    private Integer score;
    private Integer durationMs;
    private LocalDateTime createTime;
}
