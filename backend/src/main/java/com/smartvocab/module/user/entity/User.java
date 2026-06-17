package com.smartvocab.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String email;
    @JsonIgnore
    private String password;
    private String nickname;
    private String avatar;
    private String bio;
    private Integer streakDays;
    private Integer totalDays;
    private LocalDate lastCheckin;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
