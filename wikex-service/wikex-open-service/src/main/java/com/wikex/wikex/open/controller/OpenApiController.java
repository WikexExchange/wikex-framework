package com.wikex.wikex.open.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.exchange.entity.ExchangeOrderDetail;
import com.wikex.wikex.exchange.feign.ExchangeOrderFeign;
import com.wikex.wikex.open.util.RedisUtil;
import com.wikex.wikex.user.entity.MemberApiKey;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.MemberApiKeyFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
public class OpenApiController extends BaseController {
    @Autowired
    private MemberApiKeyFeign apiKeyService ;
    @Autowired
    private MemberWalletFeign memberWalletFeign;
    @Autowired
    private ExchangeOrderFeign exchangeOrderFeign;
    @Autowired
    private RedisUtil redisUtil;

    @RequestMapping(value = "get/account",method = RequestMethod.GET)
    public MessageResult getUserId(HttpServletRequest request){
        String ac = request.getParameter("accessKeyId");
        MemberApiKey memberApiKey = apiKeyService.findMemberApiKeyByApiKey(ac);
        JSONObject re = new JSONObject();
        re.put("memberId",memberApiKey.getMemberId());
        return success("SUCCESS",re);
    }

    @RequestMapping(value = "/account",method = RequestMethod.GET)
    public MessageResult getUserAccountInfo(HttpServletRequest request){
        String ac = request.getParameter("accessKeyId");
        MemberApiKey memberApiKey = apiKeyService.findMemberApiKeyByApiKey(ac);
        List<MemberWallet> list = memberWalletFeign.findAllByMemberId(memberApiKey.getMemberId());
        return success(list);
    }

    @ApiOperation(value = "Add Entrusted Order")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "direction", value = "Direction 0: Buy  1: Sell"),
            @ApiImplicitParam(name = "symbol", value = "Trading Pair Symbol"),
            @ApiImplicitParam(name = "amount", value = "Amount"),
            @ApiImplicitParam(name = "price", value = "Price"),
            @ApiImplicitParam(name = "type", value = "0 Market Price 1 Limit Price"),
    })
    @RequestMapping("/order/add")
    public MessageResult addOrder(@RequestParam("memberId") Long memberId,
                                  @RequestParam("direction")Integer direction,
                                  @RequestParam("symbol")String symbol,
                                  @RequestParam("price") BigDecimal price,
                                  @RequestParam("amount")BigDecimal amount,
                                  @RequestParam("type")Integer type,HttpServletRequest request){
        String ac = request.getParameter("accessKeyId");
        MemberApiKey memberApiKey = apiKeyService.findMemberApiKeyByApiKey(ac);
        return exchangeOrderFeign.addOrder(memberApiKey.getMemberId(),direction,symbol,price,amount,type);
    }

    @GetMapping("query/order_detail")
    public MessageResult queryOrderDetailByOrderId(HttpServletRequest request,@RequestParam("orderId")String orderId){
        try {
            if(StringUtils.isEmpty(orderId)|| !orderId.startsWith("E")){
                return MessageResult.error(500,"Invalid order number, please verify the order number");
            }
            // Query order by order number
            ExchangeOrder order = exchangeOrderFeign.findOne(orderId);
            String ac = request.getParameter("accessKeyId");
            MemberApiKey memberApiKey = apiKeyService.findMemberApiKeyByApiKey(ac);
            if(!memberApiKey.getMemberId().equals(order.getMemberId())){
                return error("Account id is incorrect");
            }
            order.setDetail(exchangeOrderFeign.findAllDetailByOrderId(order.getOrderId()));
            if(order==null){
                return  MessageResult.error(500,"Order does not exist, please verify the order number");
            }
           return MessageResult.success("success",order);

        } catch (Exception e) {
            return  MessageResult.error(500,"Error querying order details");
        }
    }

    @ApiOperation(value = "Current Entrusted Orders")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading Pair Symbol"),
            @ApiImplicitParam(name = "type", value = "0 Market Price 1 Limit Price"),
            @ApiImplicitParam(name = "startTime", value = "Start Time"),
            @ApiImplicitParam(name = "endTime", value = "End Time"),
            @ApiImplicitParam(name = "direction", value = "Direction 0: Buy  1: Sell"),
    })
    @PostMapping("query/orderCurrent")
    public MessageResult queryOrderByMemberIdAndSymbol(@RequestParam(value = "symbol",required = false) String symbol,
                                                       @RequestParam(value = "type",required = false) Integer type,
                                                       @RequestParam(value = "startTime",required = false) String startTime,
                                                       @RequestParam(value = "endTime",required = false) String endTime,
                                                       @RequestParam(value = "direction",required = false) Integer direction,
                                                       @RequestParam(value = "pageNo",defaultValue = "1") int pageNo,
                                                       @RequestParam(value = "pageSize",defaultValue = "10") int pageSize,
                                                       HttpServletRequest request){
        try {
            String ac = request.getParameter("accessKeyId");
            MemberApiKey memberApiKey = apiKeyService.findMemberApiKeyByApiKey(ac);

            Page<ExchangeOrder> page = exchangeOrderFeign.personalCurrentOrder(memberApiKey.getMemberId(),symbol,type,startTime,endTime,direction,pageNo,pageSize);
            page.getRecords().forEach(exchangeOrder -> {
                // Get transaction details
                BigDecimal tradedAmount = BigDecimal.ZERO;
                List<ExchangeOrderDetail> details = exchangeOrderFeign.findAllDetailByOrderId(exchangeOrder.getOrderId());
                exchangeOrder.setDetail(details);
                for (ExchangeOrderDetail trade : details) {
                    tradedAmount = tradedAmount.add(trade.getAmount());
                }
                exchangeOrder.setTradedAmount(tradedAmount);
            });
            return MessageResult.success("success",page);
        }catch (Exception e){
            return  MessageResult.error(500,"Error querying current entrusted orders");
        }
    }

    @RequestMapping(value = "cancel_order",method = RequestMethod.GET)
    public MessageResult cancelOrderByOrderId(@RequestParam("orderId")String orderId,
                                              @RequestParam("memberId")Long memberId,HttpServletRequest request){
        try {
            String ac = request.getParameter("accessKeyId");
            MemberApiKey memberApiKey = apiKeyService.findMemberApiKeyByApiKey(ac);
            if(!memberApiKey.getMemberId().equals(memberId)){
                return error("Account id is incorrect");
            }
            return exchangeOrderFeign.cancelOrder4API(memberId, orderId);
        }catch (Exception e){
            return  MessageResult.error(500,"Error cancelling order");
        }
    }

    @RequestMapping(value = "history",method = RequestMethod.POST)
    public MessageResult queryHistoryOrder(
            @RequestParam("memberId") Long memberId,
            @RequestParam(value = "symbol" ,required = false) String symbol,
            @RequestParam(value = "type",required = false) Integer type,
            @RequestParam(value = "status" ,required = false) Integer status,
            @RequestParam(value = "startTime",required = false) String startTime,
            @RequestParam(value = "endTime",required = false) String endTime,
            @RequestParam(value = "direction",required = false) Integer direction,
            @RequestParam(value = "pageNo",defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize",defaultValue = "10") int pageSize,
            HttpServletRequest request){
        MessageResult result = new MessageResult();
        try {
            String ac = request.getParameter("accessKeyId");
            MemberApiKey memberApiKey = apiKeyService.findMemberApiKeyByApiKey(ac);
            if(!memberApiKey.getMemberId().equals(memberId)){
                return error("Account id is incorrect");
            }
            Page<ExchangeOrder> page = exchangeOrderFeign.personalHistoryOrder(memberId, symbol, type,status,startTime,endTime,direction,pageNo, pageSize);
            page.getRecords().forEach(exchangeOrder -> {
                // Get transaction details
                exchangeOrder.setDetail(exchangeOrderFeign.findAllDetailByOrderId(exchangeOrder.getOrderId()));
            });
            return MessageResult.success("success",page);
        }catch (Exception e){
            return  MessageResult.error(500,"Error querying historical entrusted orders");
        }
    }
}
