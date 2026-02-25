package com.wikex.wikex.p2p.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.p2p.entity.Advertise;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.p2p.vo.AdvertiseVo;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;


public interface AdvertiseMapper extends BaseMapper<Advertise> {

    int alterStatusBatch(@Param("status") Integer status,@Param("updateTime") Date updateTime,@Param("ids") Long[] ids);

    List<AdvertiseVo> selectSellAutoOffShelves(@Param("marketPrice") BigDecimal marketPrice,@Param("coinId") Long coinId, @Param("jyRate")BigDecimal jyRate);

    List<AdvertiseVo> selectBuyAutoOffShelves(@Param("marketPrice")BigDecimal marketPrice, @Param("coinId") Long coinId);

    int putOffAdvertise(@Param("id") Long id, @Param("amount")BigDecimal amount);

    int updateAdvertiseDealAmount(@Param("id")Long id, @Param("amount")BigDecimal amount);

    int updateAdvertiseAmount(@Param("status")int status, @Param("id")Long id, @Param("amount")BigDecimal amount);

    Long getAdvertiserNum(@Param("memberId")Long memberId);

    Page<Advertise> paginationAdvertise(Page<Advertise> page, @Param("marketPrice") double marketPrice,@Param("country")String country, @Param("coinId")Long coinId, @Param("advertiseType")int advertiseType);

    List<Map<String, String>> getPriceBySql(@Param("price") BigDecimal price, @Param("type") int type, @Param("coinId") Long coinId);
}
