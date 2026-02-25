package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.PromotionCard;

import java.util.List;


public interface PromotionCardService extends IService<PromotionCard> {

    List<PromotionCard> findAllByMemberIdAndIsFree(Long memberId, Integer isFree);

    List<PromotionCard> findAllByMemberId(Long memberId);

    PromotionCard findPromotionCardByCardNo(String cardNo);
}
