//package com.wikex.wikex.permission.mq;
//
//import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
//import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
//import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
//import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
//import org.apache.rocketmq.common.message.MessageExt;
//import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
//import org.apache.rocketmq.spring.core.RocketMQListener;
//import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//
///*****
// * @Author:
// * @Description:
// ****/
//@Component
//@RocketMQMessageListener(topic = "member-register",consumerGroup = "permission-group")
//public class RefundResultListener implements RocketMQListener,RocketMQPushConsumerLifecycleListener {
//
//
//    @Override
//    public void onMessage(Object message) {
//    }
//
//    @Override
//    public void prepareStart(DefaultMQPushConsumer consumer) {
//        consumer.registerMessageListener(new MessageListenerConcurrently() {
//            @Override
//            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
//                try {
//                    for (MessageExt msg : msgs) {
//                        
//                        String result = new String(msg.getBody(),"UTF-8");
//
//                       
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//            }
//        });
//    }
//}
