package com.wikex.wikex.chat.handler;

import com.wikex.wikex.chat.entity.ChatMessageRecord;
import com.wikex.wikex.chat.entity.HistoryChatMessage;
import com.wikex.wikex.chat.entity.HistoryMessagePage;
import com.wikex.wikex.chat.utils.DateUtils;
import com.wikex.wikex.p2p.entity.Advertise;
import com.wikex.wikex.p2p.entity.OtcOrder;
import com.wikex.wikex.p2p.feign.AdvertiseFeign;
import com.wikex.wikex.p2p.feign.OtcOrderFeign;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChatMessageHandler implements MessageHandler {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private OtcOrderFeign orderFeign;
    @Autowired
    private AdvertiseFeign advertiseFeign;

    @Override
    public void handleMessage(ChatMessageRecord message) {
        mongoTemplate.insert(message, "chat_message"/*+message.getOrderId()*/);
    }

    /**
     * Get historical chat messages
     *
     * @param message
     * @return
     */
    @Override
    public HistoryMessagePage getHistoryMessage(HistoryChatMessage message) {
        Criteria criteria = new Criteria();
        if(!StringUtils.isEmpty(message.getOrderId())) {
            criteria = Criteria.where("orderId").is(message.getOrderId());
        }
        Sort sort =  Sort.by(Sort.Direction.DESC,message.getSortFiled());
        Query query = new Query(criteria).with(sort);
        long total = mongoTemplate.count(query, ChatMessageRecord.class, "chat_message");
        query.limit(message.getLimit()).skip((message.getPage() - 1) * message.getLimit());
        List<ChatMessageRecord> list = mongoTemplate.find(query, ChatMessageRecord.class, "chat_message");

        // Check whether auto-reply is enabled
        OtcOrder order = orderFeign.findOneByOrderSn(message.getOrderId());
        Long advertiseId = order.getAdvertiseId();
        Advertise ad = advertiseFeign.findOne(advertiseId);
        if(ad!=null && ad.getAuto().intValue()==1 && message.getUidTo().equals(ad.getMemberId().toString())){
            if(list==null || list.size()==0){
                // Reply message
                ChatMessageRecord chatMessageRecord = new ChatMessageRecord();
                BeanUtils.copyProperties(message, chatMessageRecord);
                chatMessageRecord.setUidFrom(message.getUidTo());
                chatMessageRecord.setUidTo(message.getUidFrom());
                chatMessageRecord.setNameFrom(message.getNameTo());
                chatMessageRecord.setNameTo(message.getNameFrom());
                chatMessageRecord.setSendTime(DateUtils.getCurrentDate().getTime());
                chatMessageRecord.setContent(ad.getAutoword());
                // Save chat message to MongoDB
                this.handleMessage(chatMessageRecord);
                list = new ArrayList<>();
                list.add(chatMessageRecord);
            }
        }

        for (ChatMessageRecord record : list) {
            record.setSendTimeStr(DateUtils.getDateStr(record.getSendTime()));
        }

        long consult = total / message.getLimit();
        long residue = total % message.getLimit();
        long totalPage = residue == 0 ? consult : (consult + 1);
        return HistoryMessagePage.getInstance(message.getPage(), totalPage, list.size(), total, list);
    }

}
