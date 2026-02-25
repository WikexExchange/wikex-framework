package com.wikex.wikex.coinswap.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.coinswap.entity.CoinswapFavorSymbol;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface CoinswapFavorSymbolMapper extends BaseMapper<CoinswapFavorSymbol> {

    CoinswapFavorSymbol findByMemberIdAndSymbol(@Param("memberId") Long memberId, @Param("symbol")String symbol);

    List<CoinswapFavorSymbol> findAllByMemberId(@Param("memberId")Long memberId);
}
