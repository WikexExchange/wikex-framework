package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.PromotionCardOrder;
import com.wikex.wikex.user.mapper.PromotionCardOrderMapper;
import com.wikex.wikex.user.service.PromotionCardOrderService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class PromotionCardOrderServiceImpl extends ServiceImpl<PromotionCardOrderMapper, PromotionCardOrder> implements PromotionCardOrderService {

    @Override
    public List<PromotionCardOrder> findByCardIdAndMemberId(Long cardId, Long memberId) {
        LambdaQueryWrapper<PromotionCardOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PromotionCardOrder::getMemberId,memberId);
        queryWrapper.eq(PromotionCardOrder::getCardId,cardId);
        return this.list(queryWrapper);
    }

    @Override
    public List<PromotionCardOrder> findAllByMemberIdAndIsFree(Long memberId, Integer isFree) {
        LambdaQueryWrapper<PromotionCardOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PromotionCardOrder::getMemberId,memberId);
        queryWrapper.eq(PromotionCardOrder::getIsFree,isFree);
        return this.list(queryWrapper);
    }
}
