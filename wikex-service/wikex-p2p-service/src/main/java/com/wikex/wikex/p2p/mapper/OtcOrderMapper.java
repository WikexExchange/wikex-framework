package com.wikex.wikex.p2p.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.p2p.entity.OtcOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.screen.OrderScreen;
import com.wikex.wikex.vo.OtcOrderVO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;


public interface OtcOrderMapper extends BaseMapper<OtcOrder> {

    List<OtcOrder> findAllExpiredOrder(@Param("date")Date date);

    int cancelOrder(@Param("date")Date date, @Param("status")int status, @Param("orderSn")String orderSn);

    int payForOrder(@Param("date")Date date, @Param("status")int status, @Param("orderSn")String orderSn);

    int releaseOrder(@Param("date")Date date,@Param("status")int status,  @Param("orderSn")String orderSn);

    int updateAppealOrder(@Param("status")int status,  @Param("orderSn")String orderSn);

    Page<OtcOrderVO> outExcel(Page<OtcOrderVO> page, @Param("screen") OrderScreen screen);

    List<OtcOrderVO> getOtcOrderStatistics(@Param("dateStr")String dateStr);
}
