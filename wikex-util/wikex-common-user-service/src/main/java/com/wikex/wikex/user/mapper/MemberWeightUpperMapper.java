package com.wikex.wikex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.user.entity.MemberWeightUpper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface MemberWeightUpperMapper extends BaseMapper<MemberWeightUpper> {

    List<MemberWeightUpper> findAllByUpperIds(@Param("ids") List<Long> ids);
}
