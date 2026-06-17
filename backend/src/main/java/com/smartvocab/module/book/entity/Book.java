package com.smartvocab.module.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("book")
public class Book {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String category;
    private String cover;
    private String color;
    private String description;
    private Integer wordCount;
    private String level;
    private LocalDateTime createTime;
}
