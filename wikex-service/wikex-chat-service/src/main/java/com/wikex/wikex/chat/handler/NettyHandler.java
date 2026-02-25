package com.wikex.wikex.chat.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.chat.entity.ChatMessageRecord;
import com.wikex.wikex.chat.entity.ConfirmResult;
import com.wikex.wikex.chat.entity.MessageTypeEnum;
import com.wikex.wikex.chat.entity.RealTimeChatMessage;
import com.wikex.wikex.chat.netty.QuoteMessage;
import com.wikex.wikex.chat.utils.DateUtils;
import com.wikex.wikex.constant.NettyCommand;
import com.wikex.wikex.core.annotation.HawkBean;
import com.wikex.wikex.core.annotation.HawkMethod;
import com.wikex.wikex.netty.common.NettyCacheUtils;
import com.wikex.wikex.netty.push.HawkPushServiceApi;
import com.wikex.wikex.p2p.entity.OtcOrder;
import com.wikex.wikex.p2p.feign.OtcOrderFeign;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashSet;
import java.util.Set;

/**
 * Handle Netty subscriptions and unsubscriptions
 */
@Slf4j
@HawkBean
public class NettyHandler {
    @Autowired
    private HawkPushServiceApi hawkPushService;
    @Autowired
    private OtcOrderFeign orderFeign;
    @Autowired
    private MessageHandler chatMessageHandler ;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ApnsHandler apnsHandler;

    public void subscribeTopic(Channel channel,String topic){
        String userKey = channel.id().asLongText();
        NettyCacheUtils.keyChannelCache.put(channel,userKey);
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

    @HawkMethod(cmd = NettyCommand.SUBSCRIBE_CHAT,version = NettyCommand.COMMANDS_VERSION)
    public QuoteMessage.SimpleResponse subscribeChat(byte[] body, ChannelHandlerContext ctx){
        JSONObject json = JSON.parseObject(new String(body));
        System.out.println("Subscribe: " + json.toJSONString());
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        String orderId = json.getString("orderId");
        String uid = json.getString("uid");
        if(StringUtils.isEmpty(uid) || StringUtils.isEmpty(orderId)){
            response.setCode(500).setMessage("Subscription failed, invalid parameters");
        }
        else {
            String accessKey = orderId + "-" + uid;
            subscribeTopic(ctx.channel(),accessKey);
            response.setCode(0).setMessage("Subscription successful");
        }
        return response.build();
    }

    @HawkMethod(cmd = NettyCommand.SUBSCRIBE_GROUP_CHAT)
    public QuoteMessage.SimpleResponse subscribeGroupChat(byte[] body, ChannelHandlerContext ctx){
        JSONObject json = JSON.parseObject(new String(body));
        
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        String uid = json.getString("uid");
        if(StringUtils.isEmpty(uid)){
            response.setCode(500).setMessage("Subscription failed, invalid parameters");
        }
        else {
            String key = uid;
            subscribeTopic(ctx.channel(),key);
            response.setCode(0).setMessage("Subscription successful");
        }
        return response.build();
    }

    @HawkMethod(cmd = NettyCommand.UNSUBSCRIBE_CHAT)
    public QuoteMessage.SimpleResponse unsubscribeChat(byte[] body, ChannelHandlerContext ctx){
        System.out.println(ctx.channel().id());
        JSONObject json = JSON.parseObject(new String(body));
        String orderId = json.getString("orderId");
        String uid = json.getString("uid");
        String accessKey = orderId+"-"+uid;
        unsubscribeTopic(ctx.channel(),accessKey);
        apnsHandler.removeToken(uid);
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        response.setCode(0).setMessage("Unsubscribe successful");
        return response.build();
    }

    @HawkMethod(cmd = NettyCommand.UNSUBSCRIBE_GROUP_CHAT)
    public QuoteMessage.SimpleResponse unsubscribeGroupChat(byte[] body, ChannelHandlerContext ctx){
        JSONObject json = JSON.parseObject(new String(body));
        String uid = json.getString("uid");
        String key = uid;
        unsubscribeTopic(ctx.channel(),key);
        apnsHandler.removeToken(uid);
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        response.setCode(0).setMessage("Unsubscribe successful");
        return response.build();
    }

    @HawkMethod(cmd = NettyCommand.SUBSCRIBE_APNS)
    public QuoteMessage.SimpleResponse subscribeApns(byte[] body, ChannelHandlerContext ctx){
        JSONObject json = JSON.parseObject(new String(body));
        
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        String token = json.getString("token");
        String uid = json.getString("uid");
        if(StringUtils.isEmpty(uid) || StringUtils.isEmpty(token)){
            response.setCode(500).setMessage("Subscription failed, invalid parameters");
        }
        else {
            apnsHandler.setToken(uid,token);
            response.setCode(0).setMessage("Subscription successful");
        }
        return response.build();
    }

    @HawkMethod(cmd = NettyCommand.UNSUBSCRIBE_APNS)
    public QuoteMessage.SimpleResponse unsubscribeApns(byte[] body, ChannelHandlerContext ctx){
        JSONObject json = JSON.parseObject(new String(body));
        
        String uid = json.getString("uid");
        apnsHandler.removeToken(uid);
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        response.setCode(0).setMessage("Unsubscribe successful");
        return response.build();
    }

    @HawkMethod(cmd = NettyCommand.SEND_CHAT)
    public QuoteMessage.SimpleResponse sendMessage(byte[] body, ChannelHandlerContext ctx){
        
        RealTimeChatMessage message = JSON.parseObject(new String(body), RealTimeChatMessage.class);
        handleMessage(message);
        QuoteMessage.SimpleResponse.Builder response = QuoteMessage.SimpleResponse.newBuilder();
        response.setCode(0).setMessage("Send successful");
        return response.build();
    }

    /**
     * Push message
     * @param key
     * @param result
     */
    public void push(String key, Object result,short command) {
        byte[] body = JSON.toJSONString(result).getBytes();
        Set<Channel> channels = NettyCacheUtils.getChannel(key);
        if(channels!=null && channels.size() > 0) {
            
            hawkPushService.pushMsg(channels, command, body);
        }
    }

    public void handleMessage(RealTimeChatMessage message){
        if(message.getMessageType()== MessageTypeEnum.NOTICE){
            OtcOrder order =  orderFeign.findOneByOrderSn(message.getOrderId());
            ConfirmResult result = new ConfirmResult(message.getContent(),order.getStatus().getCode());
            result.setUidFrom(message.getUidFrom());
            result.setOrderId(message.getOrderId());
            result.setNameFrom(message.getNameFrom());
            push(message.getOrderId() + "-" + message.getUidTo(),result,NettyCommand.PUSH_CHAT);
            push(message.getUidTo(),result,NettyCommand.PUSH_GROUP_CHAT);
            messagingTemplate.convertAndSendToUser(message.getUidTo(),"/order-notice/"+message.getOrderId(),result);
        }
        else if(message.getMessageType() == MessageTypeEnum.NORMAL_CHAT) {
            ChatMessageRecord chatMessageRecord = new ChatMessageRecord();
            BeanUtils.copyProperties(message, chatMessageRecord);
            chatMessageRecord.setSendTime(DateUtils.getCurrentDate().getTime());
            chatMessageRecord.setFromAvatar(message.getAvatar());
            // Save chat message to MongoDB
            chatMessageHandler.handleMessage(chatMessageRecord);
            chatMessageRecord.setSendTimeStr(DateUtils.getDateStr(chatMessageRecord.getSendTime()));
            // Send to the specified user (client subscription path: /user/+uid+/+accessKey)
            push(message.getUidTo(),chatMessageRecord,NettyCommand.PUSH_GROUP_CHAT);
            apnsHandler.handleMessage(message.getUidTo(),chatMessageRecord);
            messagingTemplate.convertAndSendToUser(message.getUidTo(), "/" + message.getOrderId(), chatMessageRecord);
        }
    }
}
