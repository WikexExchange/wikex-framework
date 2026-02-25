package com.wikex.wikex.p2p.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.p2p.entity.OtcOrder;
import com.wikex.wikex.screen.OrderScreen;
import com.wikex.wikex.util.MessageResult;
import com.wikex.wikex.vo.OtcOrderVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-p2p",contextId = "otcOrderFeign")
public interface OtcOrderFeign {
    @PostMapping(value = "/orderFeign/findOne")
    OtcOrder findOne(@RequestParam("id") Long id);

    @PostMapping(value = "/orderFeign/findOneByOrderSn")
    OtcOrder findOneByOrderSn(@RequestParam("orderSn")String orderSn);

    @PostMapping(value = "/orderFeign/cancelOrder")
    Integer cancelOrder(@RequestParam("orderSn")String orderSn);

    @PostMapping(value = "/orderFeign/releaseOrder")
    Integer releaseOrder(@RequestParam("orderSn")String orderSn);

    @PostMapping(value = "/orderFeign/onOrderCompleted")
    MessageResult onOrderCompleted(@RequestBody OtcOrder order);

    @PostMapping(value = "/orderFeign/findAll")
    List<OtcOrder> findAll();

    @PostMapping(value = "/orderFeign/updateById")
    void updateById(@RequestBody OtcOrder order);

    @PostMapping(value = "/orderFeign/outExcel")
    Page<OtcOrderVO> outExcel(@RequestBody OrderScreen screen);

    @PostMapping(value = "/orderFeign/getOrderNum")
    MessageResult getOrderNum();

    @PostMapping(value = "/orderFeign/getOtcOrderStatistics")
    List<OtcOrderVO> getOtcOrderStatistics(@RequestParam("dateStr")String dateStr);
}
