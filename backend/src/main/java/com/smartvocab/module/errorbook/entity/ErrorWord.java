package com.smartvocab.module.errorbook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("error_word")
public class ErrorWord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long wordId;
    private Integer errorCount;
    private String quizType;
    private LocalDateTime lastErrorTime;
}
