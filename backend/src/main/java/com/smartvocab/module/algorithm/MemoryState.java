package com.smartvocab.module.algorithm;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 单词记忆参数（艾宾浩斯模型输入输出）
 */
@Data
public class MemoryState {
    /** 记忆稳定性 S（小时） */
    private double stability;
    /** 难度系数 D ∈ (0,1) */
    private double difficulty;
    /** 复习次数 */
    private int reviewCount;
    /** 最近一次停留时长 ms */
    private int durationMs;
    /** 上次复习时间 */
    private LocalDateTime lastReviewTime;
    /** 计算出的下次复习时间 */
    private LocalDateTime nextReviewTime;
    /** 综合置信度 0~1 */
    private double confidence;
}
