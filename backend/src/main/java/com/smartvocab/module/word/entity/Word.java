package com.smartvocab.module.word.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("word")
public class Word {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bookId;
    private String spelling;
    private String phoneticUs;
    private String phoneticUk;
    private String audioUrl;
    private String imageUrl;
    private String difficulty;
    private String tags;
    private LocalDateTime createTime;
}
