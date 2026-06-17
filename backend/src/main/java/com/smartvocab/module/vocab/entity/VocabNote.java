package com.smartvocab.module.vocab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vocab_note")
public class VocabNote {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long wordId;
    private String note;
    private Integer star;
    private Integer isMarked;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
