package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.PromotionCardOrder;

import java.util.List;


public interface PromotionCardOrderService extends IService<PromotionCardOrder> {

    List<PromotionCardOrder> findByCardIdAndMemberId(Long cardId, Long memberId);

    List<PromotionCardOrder> findAllByMemberIdAndIsFree(Long memberId, Integer isFree);
}
