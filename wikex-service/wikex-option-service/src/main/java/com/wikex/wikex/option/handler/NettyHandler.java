package com.wikex.wikex.option.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.NettyCommand;
import com.wikex.wikex.core.annotation.HawkBean;
import com.wikex.wikex.core.annotation.HawkMethod;
import com.wikex.wikex.netty.common.NettyCacheUtils;
import com.wikex.wikex.netty.push.HawkPushServiceApi;
import com.wikex.wikex.option.netty.QuoteMessage;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.KLine;
import com.wikex.wikex.pojo.OptionTradePlate;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;


@HawkBean
@Slf4j
public class NettyHandler implements MarketHandler {
    @Autowired
    private HawkPushServiceApi hawkPushService;
    private String topicOfSymbol = "CONTRACT_SYMBOL_THUMB";

    public void subscribeTopic(Channel channel, String topic){
        String userKey = channel.id().asLongText();
        if(!NettyCacheUtils.keyChannelCache.containsKey(channel)) {
            NettyCacheUtils.keyChannelCache.put(channel, userKey);
        }
        NettyCacheUtils.storeChannel(topic,channel);
        if(NettyCacheUtils.userKey.containsKey(userKey)){
            NettyCacheUtils.userKey.get(userKey).add(topic);
        }
        else{
            Set<String> userkeys=new HashSet<>();
            userkeys.add(topic);
            NettyCacheUtils.userKey.put(userKey,userkeys);
        }
    }

    public void unsubscribeTopic(Channel channel,String topic){
        String userKey = channel.id().asLongText();
        if(NettyCacheUtils.userKey.containsKey(userKey)) {
            NettyCacheUtils.userKey.get(userKey).remove(topic);
        }
        NettyCacheUtils.keyChannelCache.remove(channel);
    }

    @HawkMethod(cmd = NettyCommand.CONTRACT_SUBSCRIBE_SYMBOL_THUMB,version = NettyCommand.COMMANDS_VERSION)
    public QuoteMessage.SimpleResponse subscribeSymbolThumb(byte[] body, ChannelHandlerContext ctx){
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        subscribeTopic(ctx.channel(),topicOfSymbol);
        response.setCode(0).setMessage("Subscription successful");
        return response.build();
    }

    @HawkMethod(cmd = NettyCommand.CONTRACT_UNSUBSCRIBE_SYMBOL_THUMB)
    public QuoteMessage.SimpleResponse unsubscribeSymbolThumb(byte[] body, ChannelHandlerContext ctx){
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        unsubscribeTopic(ctx.channel(),topicOfSymbol);
        response.setCode(0).setMessage("Unsubscription successful");
        return response.build();
    }

    @HawkMethod(cmd = NettyCommand.CONTRACT_SUBSCRIBE_EXCHANGE)
    public QuoteMessage.SimpleResponse subscribeExchange(byte[] body, ChannelHandlerContext ctx){
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        JSONObject json = JSON.parseObject(new String(body));
        String symbol = json.getString("symbol");
        String uid = json.getString("uid");
        if(StringUtils.isNotEmpty(uid)){
            subscribeTopic(ctx.channel(),symbol+"-"+uid);
        }
        subscribeTopic(ctx.channel(),symbol);
        response.setCode(0).setMessage("Subscription successful");
        return response.build();
    }

    @HawkMethod(cmd = NettyCommand.CONTRACT_UNSUBSCRIBE_EXCHANGE)
    public QuoteMessage.SimpleResponse unsubscribeExchange(byte[] body, ChannelHandlerContext ctx){
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        JSONObject json = JSON.parseObject(new String(body));
        
        String symbol = json.getString("symbol");
        String uid = json.getString("uid");
        if(StringUtils.isNotEmpty(uid)){
            unsubscribeTopic(ctx.channel(),symbol+"-"+uid);
        }
        unsubscribeTopic(ctx.channel(), symbol);
        response.setCode(0).setMessage("Unsubscription successful");
        return response.build();
    }


    @Override
    public void handleTrade(String symbol, CoinThumb thumb) {

    }

    @Override
    public void handleKLine(String symbol, KLine kLine) {
        hawkPushService.pushMsg(NettyCacheUtils.getChannel(symbol),NettyCommand.CONTRACT_PUSH_EXCHANGE_KLINE, JSONObject.toJSONString(kLine).getBytes());
    }

    
    public void handlePlate(String symbol, OptionTradePlate plate){
        
        // Push order book
        hawkPushService.pushMsg(NettyCacheUtils.getChannel(symbol),NettyCommand.PUSH_EXCHANGE_PLATE, plate.toJSON(24).toJSONString().getBytes());
        // Push depth
        hawkPushService.pushMsg(NettyCacheUtils.getChannel(symbol),NettyCommand.PUSH_EXCHANGE_DEPTH, plate.toJSON(50).toJSONString().getBytes());
    }


}
