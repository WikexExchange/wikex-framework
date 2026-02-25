// package com.wikex.wikex.user.mq;

// import com.alibaba.fastjson.JSON;
// import com.wikex.wikex.user.entity.Member;
// import com.wikex.wikex.user.event.MemberEvent;
// import com.wikex.wikex.user.service.MemberService;
// import com.wikex.wikex.user.service.MemberWeightUpperService;
// import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
// import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
// import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.messaging.Message;
// import org.springframework.stereotype.Component;
// import org.springframework.util.StringUtils;

// import java.util.Map;

// @Component
// @RocketMQTransactionListener(txProducerGroup = "registerSuccess")
// public class RegisterSuccessTransactionListenerImpl implements RocketMQLocalTransactionListener {

//     @Autowired
//     private MemberEvent memberEvent;
//     @Autowired
//     private MemberWeightUpperService memberWeightUpperService;
    
//     @Value("${commission.need.real-name:1}")
//     private int needRealName;
//     @Autowired
//     private MemberService memberService;

    
//     @Override
//     public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
//         try {
//             Map<String, Object> params = (Map<String, Object>) arg;
//             Member member = (Member) params.get("member");
//             String promotionCode = (String) params.get("promotionCode");
//             System.out.println("member:::" + JSON.toJSONString(member));
//             // Promotion activity
//             if (StringUtils.hasText(promotionCode)) {
//                 Member member1 = memberService.findMemberByPromotionCode(promotionCode);
//                 if (member1 != null) {
//                     member.setInviterId(member1.getId());
//                     // If real-name authentication is not required, directly issue rewards
//                     if (needRealName == 0) {
//                         memberEvent.promotion(member1, member);
//                     }
//                 }
//             }
//             // Update
//             memberService.updateById(member);
//             // Add upper relationship
//             memberWeightUpperService.saveMemberWeightUpper(member);

//         } catch (Exception e) {
//             e.printStackTrace();
//             return RocketMQLocalTransactionState.ROLLBACK;
//         }
//         return RocketMQLocalTransactionState.COMMIT;
//     }


    
//     @Override
//     public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
//         return RocketMQLocalTransactionState.COMMIT;
//     }
// }
