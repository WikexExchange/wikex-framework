package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.MemberPromotion;
import com.wikex.wikex.user.mapper.MemberPromotionMapper;
import com.wikex.wikex.user.service.MemberPromotionService;
import com.wikex.wikex.user.vo.MemberPromotionStasticVO;
import com.wikex.wikex.util.DateUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class MemberPromotionServiceImpl extends ServiceImpl<MemberPromotionMapper, MemberPromotion>
        implements MemberPromotionService {

    @Override
    public List<MemberPromotionStasticVO> getDateRangeRank(int level, Date startDate, Date endDate, int topCount) {
        return this.baseMapper.getInviteGroupByTypeAndDate(level, startDate, endDate, topCount);
    }

    @Override
    public Long countByInviterId(Long inviterId) {
        return this.lambdaQuery()
                .eq(MemberPromotion::getInviterId, inviterId)
                .eq(MemberPromotion::getLevel, 0)
                .count()
                .longValue();
    }

    public void addMemberPromotion(Long inviteesId, Long inviterId) {
        Date localTime = DateUtil.getCurrentDate();
        MemberPromotion promotion = new MemberPromotion();
        promotion.setInviteesId(inviteesId);
        promotion.setInviterId(inviterId);
        promotion.setLevel(0);
        promotion.setCreateTime(localTime);
        this.save(promotion);

        List<MemberPromotion> parents = this.baseMapper.findAllParentByUserId(inviterId);
        if (parents.size() > 0) {
            for (MemberPromotion parent : parents) {
                MemberPromotion invest = new MemberPromotion();
                invest.setInviteesId(inviteesId);
                invest.setInviterId(parent.getInviterId());
                invest.setLevel(parent.getLevel() + 1);
                invest.setCreateTime(localTime);
                this.save(invest);
            }
        }
    }

    @Override
    public IPage<MemberPromotion> findMemberPromotionPage(Integer pageNo, Integer pageSize, long inviterId) {
        QueryWrapper<MemberPromotion> wrapper = new QueryWrapper<>();
        wrapper.eq("inviter_id", inviterId)
                .le("level", 2)
                .orderByAsc("level")
                .orderByDesc("create_time");

        Page<MemberPromotion> page = new Page<>(pageNo, pageSize);
        return this.page(page, wrapper);
    }

    @Override
    public IPage<MemberPromotion> findMemberPromotionPageByFilter(Integer pageNo, Integer pageSize, long inviterId,
            String time, String level) {
        QueryWrapper<MemberPromotion> wrapper = new QueryWrapper<>();
        wrapper.eq("inviter_id", inviterId);

        if (level != null && !level.trim().isEmpty() && !"all".equalsIgnoreCase(level)) {
            int lv = Integer.parseInt(level.trim());
            if (lv >= 1 && lv <= 3) {
                wrapper.eq("level", lv - 1);
            } else {
                wrapper.le("level", 2);
            }
        } else {
            wrapper.le("level", 2);
        }

        if (time != null && !time.trim().isEmpty() && !"all".equalsIgnoreCase(time)) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = null;

            if ("day".equalsIgnoreCase(time)) {
                start = LocalDate.now().atStartOfDay();
            } else if ("month".equalsIgnoreCase(time)) {
                LocalDate today = LocalDate.now();
                start = LocalDate.of(today.getYear(), today.getMonth(), 1).atStartOfDay();
            } else if ("year".equalsIgnoreCase(time)) {
                LocalDate today = LocalDate.now();
                start = LocalDate.of(today.getYear(), 1, 1).atStartOfDay();
            }

            if (start != null) {
                wrapper.ge("create_time", start)
                        .le("create_time", now);
            }
        }

        wrapper.orderByAsc("level")
                .orderByDesc("create_time");

        Page<MemberPromotion> page = new Page<>(pageNo, pageSize);
        return this.page(page, wrapper);
    }
}
