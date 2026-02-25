package com.wikex.wikex.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.exchange.entity.ExchangeFavorSymbol;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Transaction Priority Symbol Mapper Interface
 * </p>
 *
 * @author markchao
 * @since 2022-02-07
 */
public interface ExchangeFavorSymbolMapper extends BaseMapper<ExchangeFavorSymbol> {

    ExchangeFavorSymbol findByMemberIdAndSymbol(@Param("memberId") Long memberId, @Param("symbol")String symbol);

    List<ExchangeFavorSymbol> findAllByMemberId(@Param("memberId")Long memberId);
}
