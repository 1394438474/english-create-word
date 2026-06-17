package com.smartvocab.module.checkin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_medal")
public class UserMedal {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String medalCode;
    private LocalDateTime achieveTime;
}
