//package com.wikex.wikex.api.mq;
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
//
///*****
// * @Author:
// * @Description: Refund result listener for handling refund application status messages
// ****/
//@Component
//@RocketMQMessageListener(topic = "member-register-swap", consumerGroup = "gateway-group")
//public class RefundResultListener implements RocketMQListener<Object>, RocketMQPushConsumerLifecycleListener {
//
//    @Override
//    public void onMessage(Object message) {
//        // No implementation for onMessage, message handling done in prepareStart
//    }
//
//    /***
//     * Message listener registration
//     * @param consumer DefaultMQPushConsumer instance
//     */
//    @Override
//    public void prepareStart(DefaultMQPushConsumer consumer) {
//        consumer.registerMessageListener(new MessageListenerConcurrently() {
//            @Override
//            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
//                try {
//                    for (MessageExt msg : msgs) {
//                        // AES encrypted string
//                        String result = new String(msg.getBody(), "UTF-8");
//
//                        System.out.println("Refund application status --- result: " + result);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//            }
//        });
//    }
//}
