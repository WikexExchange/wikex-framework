package com.wikex.wikex.user.rewardHub;

import com.wikex.wikex.exchange.entity.CampaignSetting;
import com.wikex.wikex.exchange.entity.TradingVolumn;
import com.wikex.wikex.exchange.entity.TradingVolumnBonus;
import com.wikex.wikex.exchange.entity.TradingVolumnMonth;
import com.wikex.wikex.exchange.entity.TradingVolumnWeek;
import com.wikex.wikex.exchange.entity.LeaderboadardScoreWeek;
import com.wikex.wikex.exchange.entity.LeaderboardScoreMonth;
import com.wikex.wikex.exchange.entity.LeaderboardScore;
import com.wikex.wikex.exchange.entity.LeaderboadardCalendar;

import java.math.BigDecimal;
import java.util.List;

public interface IRewardHubHandler {
    List<CampaignSetting> findCampaignConfigs(String name);
    BigDecimal sumVolumnByMemberIdsAndCampaignId(String campaignId, List<Long> memberIds, Integer type);
    BigDecimal sumVolumnByMemberIds(List<Long> memberIds, Integer type);
    List<TradingVolumn> findTradingVolumn(Integer type);
    List<TradingVolumnBonus> findTradingVolumnBonus(String campaignId, Long memberId, Integer type);
    TradingVolumnBonus findOneTradingVolumnBonus(String id);
    void updateClaimBonus(String id, BigDecimal bonus);
    List<TradingVolumnWeek> findTradingVolumnWeek(Integer year, Integer week, Integer type);
    List<TradingVolumnMonth> findTradingVolumnMonth(Integer year, Integer month, Integer type);
    List<Integer> getWeekNosByYear(Integer year, Integer type);
    List<Integer> getMonthNosByYear(Integer year, Integer type);
    BigDecimal sumVolumnWeekCampaignId(Integer yearNo, Integer weekNo, Integer type);
    BigDecimal sumVolumnMonthCampaignId(Integer yearNo, Integer monthNo, Integer type);
    BigDecimal sumRewardCampaignId(Long memberId);
    void saveCampaignMemberPoint(Long memberId, Integer type, Integer point, Integer maxPoint, Integer yearNo, Integer monthNo, Integer weekNo);
    Integer sumCampaignMemberPoint(Long memberId);
    List<LeaderboadardScoreWeek> findLeaderboadardScoreWeek(Integer year, Integer weekNo, Long memberId);
    List<LeaderboardScoreMonth> findLeaderboadardScoreMonth(Integer year, Integer monthNo, Long memberId);
    List<LeaderboardScore> findLeaderboadardScore(Long memberId);
    Integer maxWeekNoLeaderboard(Integer year);
    Integer maxMonthNoLeaderboard(Integer year);
    List<LeaderboadardCalendar> getLeaderboardCalendar(Integer year, boolean isWeek);
}
