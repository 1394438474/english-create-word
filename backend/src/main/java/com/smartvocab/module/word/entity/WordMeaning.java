package com.smartvocab.module.word.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("word_meaning")
public class WordMeaning {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long wordId;
    private String pos;
    private String meaningZh;
    private String meaningEn;
    private Integer sort;
}
