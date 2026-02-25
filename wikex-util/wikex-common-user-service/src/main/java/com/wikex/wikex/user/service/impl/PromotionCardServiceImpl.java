package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.PromotionCard;
import com.wikex.wikex.user.mapper.PromotionCardMapper;
import com.wikex.wikex.user.service.PromotionCardService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class PromotionCardServiceImpl extends ServiceImpl<PromotionCardMapper, PromotionCard> implements PromotionCardService {

    @Override
    public List<PromotionCard> findAllByMemberIdAndIsFree(Long memberId, Integer isFree) {
        LambdaQueryWrapper<PromotionCard> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PromotionCard::getMemberId,memberId);
        queryWrapper.eq(PromotionCard::getIsFree,isFree);
        return this.list(queryWrapper);
    }

    @Override
    public List<PromotionCard> findAllByMemberId(Long memberId) {
        LambdaQueryWrapper<PromotionCard> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PromotionCard::getMemberId,memberId);
        return this.list(queryWrapper);
    }

    @Override
    public PromotionCard findPromotionCardByCardNo(String cardNo) {
        LambdaQueryWrapper<PromotionCard> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PromotionCard::getCardNo,cardNo);
        List<PromotionCard> list = this.list(queryWrapper);
        if(list!=null && list.size()>0){
            return list.get(0);
        }else {
            return null;
        }
    }
}
