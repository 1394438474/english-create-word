package com.smartvocab.module.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * 艾宾浩斯优化算法引擎
 * <p>
 * 核心公式：
 *  记忆保留率   R = e^(-Δt / S)
 *  下次间隔  nextInterval = -S * ln(R_target)
 *  稳定性更新  S_new = S * (1 + α * (statusScore - 0.5))
 *  难度更新    D_new = clamp(D + β * (1 - recallQuality), 0.1, 1.0)
 * <p>
 * 输入：用户对一个单词的"熟/模糊/陌生"标记 + 错题率 + 停留时长 + 历史复习次数
 * 输出：新的稳定性、难度、下次复习时间、综合置信度
 *
 * 答辩要点：
 *  1) 相比简单 "1/3/7/15/30 天" 固定艾宾浩斯表，本算法使用连续稳定性参数 S，
 *     复习间隔会随用户表现连续动态调整，真正做到"因人而异"。
 *  2) 综合停留时长、错题率、复习次数等信号，使用指数平滑与衰减因子，
 *     模拟人脑长时记忆的遗忘规律。
 *  3) 当用户连续多次标"陌生"或错误率高时，难度 D 上升、稳定性 S 下降，
 *     单词被高频调度进复习池。
 *
 * @author 智绘记研发组
 */
@Slf4j
@Component
public class EbbinghausEngine {

    /** 目标保留率 */
    private static final double TARGET_RETENTION = 0.9;
    /** 稳定性增长系数（学习后） */
    private static final double ALPHA = 0.6;
    /** 难度调整系数 */
    private static final double BETA = 0.2;
    /** 最小间隔(小时) */
    private static final double MIN_INTERVAL_HOURS = 4;
    /** 最大间隔(小时) */
    private static final double MAX_INTERVAL_HOURS = 24 * 90; // 90 天

    public enum Status { FAMILIAR, HAZY, STRANGE }

    /**
     * 推算记忆状态
     *
     * @param status        用户三态标记
     * @param errorRate     历史错题率 0~1（仅测试场景传入；学习场景传 0）
     * @param durationMs    本次停留毫秒
     * @param reviewCount   历史复习次数
     * @param prevStability 旧稳定性
     * @param prevDifficulty 旧难度
     * @param lastReviewTime 上次复习时间
     * @return 更新后的记忆状态
     */
    public MemoryState compute(Status status,
                               double errorRate,
                               int durationMs,
                               int reviewCount,
                               double prevStability,
                               double prevDifficulty,
                               LocalDateTime lastReviewTime) {
        // 1) 状态分
        double statusScore = switch (status) {
            case FAMILIAR -> 1.0;
            case HAZY -> 0.5;
            case STRANGE -> 0.1;
        };

        // 2) 复习次数影响：复习越多，稳定性增长越快
        double repeatFactor = 1.0 + Math.log1p(Math.max(reviewCount, 0)) * 0.15;

        // 3) 停留时长影响：停留过短(<1s)表明未真正记忆；停留过长(>30s)可能犹豫
        double timeFactor;
        if (durationMs < 1000) {
            timeFactor = 0.4;
        } else if (durationMs > 30_000) {
            timeFactor = 0.8;
        } else {
            timeFactor = 0.5 + Math.min(1.0, durationMs / 10_000.0) * 0.5;
        }

        // 4) recallQuality：综合回忆质量
        double recallQuality = clamp(0.4 * statusScore + 0.3 * timeFactor + 0.3 * (1 - errorRate), 0.05, 1.0);

        // 5) 稳定性 S 更新
        double newStability = prevStability * (1 + ALPHA * (statusScore - 0.5)) * repeatFactor;
        newStability = clamp(newStability, 0.5, MAX_INTERVAL_HOURS);

        // 6) 难度 D 更新
        double newDifficulty = prevDifficulty + BETA * (1 - recallQuality);
        newDifficulty = clamp(newDifficulty, 0.1, 1.0);

        // 7) 计算下次复习时间
        //   目标保留率 0.9 ⇒ nextInterval = -S * ln(0.9) ≈ 0.105 * S
        double nextInterval = -newStability * Math.log(TARGET_RETENTION);
        // 陌生 / 高错误率 → 立即复习
        if (status == Status.STRANGE || errorRate > 0.5) {
            nextInterval = MIN_INTERVAL_HOURS;
        }
        nextInterval = clamp(nextInterval, MIN_INTERVAL_HOURS, MAX_INTERVAL_HOURS);

        LocalDateTime base = lastReviewTime != null ? lastReviewTime : LocalDateTime.now();
        LocalDateTime next = base.plus((long) (nextInterval * 60), ChronoUnit.MINUTES);

        // 8) 综合置信度
        double confidence = clamp(0.5 * statusScore + 0.3 * (1 - newDifficulty) + 0.2 * (1 - errorRate), 0, 1);

        MemoryState s = new MemoryState();
        s.setStability(round(newStability));
        s.setDifficulty(round(newDifficulty));
        s.setReviewCount(reviewCount + 1);
        s.setDurationMs(durationMs);
        s.setLastReviewTime(base);
        s.setNextReviewTime(next);
        s.setConfidence(round(confidence));
        log.debug("[Ebbinghaus] status={} recallQ={} S={}→{} D={}→{} next={} conf={}",
                status, String.format(Locale.ROOT, "%.2f", recallQuality),
                prevStability, s.getStability(), prevDifficulty, s.getDifficulty(), next, s.getConfidence());
        return s;
    }

    /** 新单词初始化 */
    public MemoryState initNew(LocalDateTime now) {
        MemoryState s = new MemoryState();
        s.setStability(1.0);
        s.setDifficulty(0.5);
        s.setReviewCount(0);
        s.setLastReviewTime(now);
        s.setNextReviewTime(now.plusHours(12));
        s.setConfidence(0.2);
        return s;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double round(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }
}
