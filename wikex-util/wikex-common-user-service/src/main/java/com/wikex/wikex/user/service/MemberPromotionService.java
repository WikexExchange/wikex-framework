package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.MemberPromotion;
import com.wikex.wikex.user.vo.MemberPromotionStasticVO;

import java.util.Date;
import java.util.List;

public interface MemberPromotionService extends IService<MemberPromotion> {

    List<MemberPromotionStasticVO> getDateRangeRank(int level, Date startDate, Date endDate, int topCount);

    Long countByInviterId(Long inviterId);

    void addMemberPromotion(Long inviteesId, Long inviterId);

    IPage<MemberPromotion> findMemberPromotionPage(Integer pageNo, Integer pageSize, long id);

    IPage<MemberPromotion> findMemberPromotionPageByFilter(Integer pageNo, Integer pageSize, long inviterId,
            String time, String level);

}
