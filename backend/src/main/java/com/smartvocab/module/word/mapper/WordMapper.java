package com.smartvocab.module.word.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartvocab.module.word.entity.Word;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WordMapper extends BaseMapper<Word> {}
