package com.wikex.wikex.p2p.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.OrderStatus;
import com.wikex.wikex.exception.InformationExpiredException;
import com.wikex.wikex.p2p.entity.OtcOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.OrderScreen;
import com.wikex.wikex.util.MessageResult;
import com.wikex.wikex.vo.OtcOrderVO;

import java.util.List;


public interface OtcOrderService extends IService<OtcOrder> {

    List<OtcOrder> checkExpiredOrder();

    void cancelOrderTask(OtcOrder order) throws InformationExpiredException;

    int cancelOrder(String orderSn);

    OtcOrder saveOrder(OtcOrder order);

    Page<OtcOrder> pageQuery(int pageNo, int pageSize, OrderStatus status, long id, String orderSn);

    OtcOrder findOneByOrderSn(String orderSn);

    int payForOrder(String orderSn);

    int releaseOrder(String orderSn);

    int updateOrderAppeal(String orderSn);

    Page<OtcOrderVO> outExcel(OrderScreen screen);

    MessageResult getOrderNum();

    List<OtcOrderVO> getOtcOrderStatistics(String dateStr);
}
