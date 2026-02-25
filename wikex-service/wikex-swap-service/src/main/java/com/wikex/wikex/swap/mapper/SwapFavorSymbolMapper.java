package com.wikex.wikex.swap.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.swap.entity.SwapFavorSymbol;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface SwapFavorSymbolMapper extends BaseMapper<SwapFavorSymbol> {

    SwapFavorSymbol findByMemberIdAndSymbol(@Param("memberId") Long memberId, @Param("symbol")String symbol);

    List<SwapFavorSymbol> findAllByMemberId(@Param("memberId")Long memberId);
}
