package com.smartvocab.module.word.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("word_sentence")
public class WordSentence {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long wordId;
    private String en;
    private String zh;
    private String source;
}
