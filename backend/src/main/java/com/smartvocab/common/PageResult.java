package com.smartvocab.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用分页响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private long total;
    private long pages;
    private long current;
    private long size;
    private List<T> records;

    public static <T> PageResult<T> of(long current, long size, long total, List<T> records) {
        long pages = size == 0 ? 0 : (total + size - 1) / size;
        return new PageResult<>(total, pages, current, size, records);
    }
}
