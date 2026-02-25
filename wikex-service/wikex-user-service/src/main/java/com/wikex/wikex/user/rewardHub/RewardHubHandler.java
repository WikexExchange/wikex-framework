package com.wikex.wikex.user.rewardHub;

import com.wikex.wikex.constant.CampaignPointType;
import com.wikex.wikex.exchange.entity.CampaignSetting;
import com.wikex.wikex.exchange.entity.CampaignMemberPoint;
import com.wikex.wikex.exchange.entity.TradingVolumn;
import com.wikex.wikex.exchange.entity.TradingVolumnBonus;
import com.wikex.wikex.exchange.entity.TradingVolumnMonth;
import com.wikex.wikex.exchange.entity.TradingVolumnWeek;
import com.wikex.wikex.exchange.entity.LeaderboadardScoreWeek;
import com.wikex.wikex.exchange.entity.LeaderboardScoreMonth;
import com.wikex.wikex.exchange.entity.LeaderboardScore;
import com.wikex.wikex.exchange.entity.LeaderboadardCalendar;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.String;
import org.bson.Document;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class RewardHubHandler implements IRewardHubHandler {
    @Autowired
    private MongoTemplate mongoTemplate;

    public List<CampaignSetting> findCampaignConfigs(String name){
        Criteria criteria = Criteria.where("isActive").is(true).and("name").is(name);
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.ASC,"fromTime"));
        Query query = new Query(criteria).with(sort);
        return mongoTemplate.find(query, CampaignSetting.class,"campaign_settings");
    }

    public BigDecimal sumVolumnByMemberIdsAndCampaignId(String campaignId, List<Long> memberIds, Integer type) {
        Aggregation agg = Aggregation.newAggregation(
            Aggregation.match(
                Criteria.where("campaignId").is(campaignId)
                        .and("memberId").in(memberIds).and("type").is(type)
            ),
            Aggregation.group().sum("volumn").as("totalVolumn")
        );
        AggregationResults<Document> result = mongoTemplate.aggregate(
                agg, "trading_volumns", Document.class
        );

        BigDecimal totalVolumn = BigDecimal.ZERO;

        Document doc = result.getUniqueMappedResult();
        if (doc != null && doc.get("totalVolumn") != null) {
            totalVolumn = new BigDecimal(doc.get("totalVolumn").toString());
        }
        return totalVolumn;
    }

    public BigDecimal sumVolumnByMemberIds(List<Long> memberIds, Integer type) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("memberId").in(memberIds).and("type").is(type)
                ),
                Aggregation.group().sum("volumn").as("totalVolumn")
        );
        AggregationResults<Document> result = mongoTemplate.aggregate(
                agg, "trading_volumns", Document.class
        );

        BigDecimal totalVolumn = BigDecimal.ZERO;

        Document doc = result.getUniqueMappedResult();
        if (doc != null && doc.get("totalVolumn") != null) {
            totalVolumn = new BigDecimal(doc.get("totalVolumn").toString());
        }
        return totalVolumn;
    }

    public List<TradingVolumn> findTradingVolumn(Integer type){
        Criteria criteria = Criteria.where("type").is(type);
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.DESC,"volumn"));
        Query query = new Query(criteria).with(sort).limit(10);
        return mongoTemplate.find(query, TradingVolumn.class,"trading_volumns");
    }

    public List<TradingVolumnBonus> findTradingVolumnBonus(String campaignId, Long memberId, Integer type){
        Criteria criteria = Criteria.where("memberId").is(memberId);
        if (StringUtils.hasText(campaignId)) {
            criteria = criteria.and("campaignId").is(campaignId);
        }
        if (type >= 0) {
            criteria = criteria.and("type").is(type);
        }
        Query query = new Query(criteria);
        return mongoTemplate.find(query, TradingVolumnBonus.class,"trading_volumn_bonus");
    }

    public TradingVolumnBonus findOneTradingVolumnBonus(String id) {
        Criteria criteria = Criteria.where("_id").is(id);
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, TradingVolumnBonus.class,"trading_volumn_bonus");
    }

    public void updateClaimBonus(String id, BigDecimal bonus) {
        Double bonusAsDouble = bonus.doubleValue();
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().set("claimed", bonusAsDouble);
        mongoTemplate.updateFirst(query, update, "trading_volumn_bonus");
    }

    public List<TradingVolumnWeek> findTradingVolumnWeek(Integer year, Integer week, Integer type){
        List<Integer> years = Arrays.asList(2025, 2026);
        Criteria criteria = Criteria.where("yearNo").in(years).and("weekNo").is(week).and("type").is(type);
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.DESC,"volumn"));
        Query query = new Query(criteria).with(sort).limit(10);
        return mongoTemplate.find(query, TradingVolumnWeek.class,"trading_volumn_weeks");
    }

    public List<TradingVolumnMonth> findTradingVolumnMonth(Integer year, Integer month, Integer type){
        List<Integer> years = Arrays.asList(2025, 2026);
        Criteria criteria = Criteria.where("yearNo").in(years).and("monthNo").is(month).and("type").is(type);
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.DESC,"volumn"));
        Query query = new Query(criteria).with(sort).limit(10);
        return mongoTemplate.find(query, TradingVolumnMonth.class,"trading_volumn_months");
    }

    public List<Integer> getWeekNosByYear(Integer year, Integer type) {
        List<Integer> years = Arrays.asList(2025, 2026);
        MatchOperation match = Aggregation.match(Criteria.where("yearNo").in(years).and("type").is(type));
        GroupOperation group = Aggregation.group().addToSet("weekNo").as("weekNos");
        Aggregation aggregation = Aggregation.newAggregation(match, group);

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "trading_volumn_weeks", Document.class);

        if (!results.getMappedResults().isEmpty()) {
            Document doc = results.getMappedResults().get(0);
            return (List<Integer>) doc.get("weekNos");
        } else {
            return new ArrayList<>();
        }
    }

    public List<Integer> getMonthNosByYear(Integer year, Integer type) {
        List<Integer> years = Arrays.asList(2025, 2026);
        MatchOperation match = Aggregation.match(Criteria.where("yearNo").in(years).and("type").is(type));
        GroupOperation group = Aggregation.group().addToSet("monthNo").as("monthNos");
        Aggregation aggregation = Aggregation.newAggregation(match, group);

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "trading_volumn_months", Document.class);

        if (!results.getMappedResults().isEmpty()) {
            Document doc = results.getMappedResults().get(0);
            return (List<Integer>) doc.get("monthNos");
        } else {
            return new ArrayList<>();
        }
    }

    public BigDecimal sumVolumnWeekCampaignId(Integer yearNo, Integer weekNo, Integer type) {
        List<Integer> years = Arrays.asList(2025, 2026);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("yearNo").in(years)
                                .and("weekNo").is(weekNo).and("type").is(type)
                ),
                Aggregation.group().sum("volumn").as("totalVolumn")
        );
        AggregationResults<Document> result = mongoTemplate.aggregate(
                agg, "trading_volumn_weeks", Document.class
        );

        BigDecimal totalVolumn = BigDecimal.ZERO;

        Document doc = result.getUniqueMappedResult();
        if (doc != null && doc.get("totalVolumn") != null) {
            totalVolumn = new BigDecimal(doc.get("totalVolumn").toString());
        }
        return totalVolumn;
    }

    public BigDecimal sumVolumnMonthCampaignId(Integer yearNo, Integer monthNo, Integer type) {
        List<Integer> years = Arrays.asList(2025, 2026);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("yearNo").in(years)
                        .and("monthNo").is(monthNo).and("type").is(type)
                ),
                Aggregation.group().sum("volumn").as("totalVolumn")
        );
        AggregationResults<Document> result = mongoTemplate.aggregate(
                agg, "trading_volumn_months", Document.class
        );

        BigDecimal totalVolumn = BigDecimal.ZERO;

        Document doc = result.getUniqueMappedResult();
        if (doc != null && doc.get("totalVolumn") != null) {
            totalVolumn = new BigDecimal(doc.get("totalVolumn").toString());
        }
        return totalVolumn;
    }

    public BigDecimal sumRewardCampaignId(Long memberId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("memberId").is(memberId)
                ),
                Aggregation.group().sum("claimed").as("totalVolumn")
        );
        AggregationResults<Document> result = mongoTemplate.aggregate(
                agg, "trading_volumn_bonus", Document.class
        );

        BigDecimal totalVolumn = BigDecimal.ZERO;

        Document doc = result.getUniqueMappedResult();
        if (doc != null && doc.get("totalVolumn") != null) {
            totalVolumn = new BigDecimal(doc.get("totalVolumn").toString());
        }
        return totalVolumn;
    }

    public void saveCampaignMemberPoint(Long memberId, Integer type, Integer point, Integer maxPoint, Integer yearNo, Integer monthNo, Integer weekNo) {
        try {
            if (type == CampaignPointType.REFERRER.getCode()) {
                Aggregation agg = Aggregation.newAggregation(
                        Aggregation.match(Criteria.where("memberId").is(memberId).and("type").is(type)
                        ),
                        Aggregation.group().sum("points").as("totalpoints")
                );
                AggregationResults<Document> result = mongoTemplate.aggregate(
                        agg, "campaign_member_points", Document.class
                );

                Integer totalpoints = 0;
                Document doc = result.getUniqueMappedResult();
                if (doc != null && doc.get("totalpoints") != null) {
                    totalpoints = Integer.valueOf(doc.get("totalpoints").toString());
                }
                if (totalpoints >= maxPoint)
                    return;

                if (totalpoints > 0) {
                    Query query = new Query(Criteria.where("memberId").is(memberId).and("type").is(type));
                    Update update = new Update().set("points", totalpoints + point);
                    mongoTemplate.updateFirst(query, update, "campaign_member_points");
                    return;
                }
            }

            CampaignMemberPoint member_point = new CampaignMemberPoint();
            member_point.setCampaignId(memberId.toString());
            member_point.setMemberId(memberId);
            member_point.setType(type);
            member_point.setYearNo(yearNo);
            member_point.setMonthNo(monthNo);
            member_point.setWeekNo(weekNo);
            member_point.setPoints(point);
            mongoTemplate.save(member_point, "campaign_member_points");
        } catch (Exception e) {
            // TODO
        }
    }

    public Integer sumCampaignMemberPoint(Long memberId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("memberId").is(memberId)),
                Aggregation.group().sum("points").as("totalpoints"));
        AggregationResults<Document> result = mongoTemplate.aggregate(
                agg, "campaign_member_points", Document.class);

        Integer totalVolumn = 0;
        Document doc = result.getUniqueMappedResult();
        if (doc != null && doc.get("totalpoints") != null) {
            totalVolumn = Integer.valueOf(doc.get("totalpoints").toString());
        }
        return totalVolumn;
    }

    public List<LeaderboadardScoreWeek> findLeaderboadardScoreWeek(Integer year, Integer weekNo, Long memberId){
        if (memberId == 0) {
            Criteria criteria = Criteria.where("weekNo").is(weekNo).and("yearNo").is(year);
            Sort sort = Sort.by(new Sort.Order(Sort.Direction.DESC, "point"));
            Query query = new Query(criteria).with(sort).limit(20);
            return mongoTemplate.find(query, LeaderboadardScoreWeek.class, "leaderboard_score_weeks");
        } else {
            List<LeaderboadardScoreWeek> data = new ArrayList<>();
            Criteria criteria = Criteria.where("weekNo").is(weekNo).and("yearNo").is(year).and("memberId").is(memberId);
            Query query = new Query(criteria).limit(1);
            LeaderboadardScoreWeek memberWeek = mongoTemplate.findOne(query, LeaderboadardScoreWeek.class, "leaderboard_score_weeks");
            if (memberWeek != null) {
                Double point = memberWeek.getPoint().doubleValue();
                Criteria criteria_m = Criteria.where("weekNo").is(weekNo).and("yearNo").is(year).and("point").gt(point);
                Query query_m = new Query(criteria_m);
                Long count = mongoTemplate.count(query_m, "leaderboard_score_weeks");
                if (point > 0 && count == 0)
                    count = 1L;
                else if (count > 0)
                    count += 1;
                memberWeek.setRank(count);
                data.add(memberWeek);
            }
            return data;
        }
    }

    public List<LeaderboardScoreMonth> findLeaderboadardScoreMonth(Integer year, Integer monthNo, Long memberId){
        if (memberId == 0) {
            Criteria criteria = Criteria.where("monthNo").is(monthNo).and("yearNo").is(year);
            Sort sort = Sort.by(new Sort.Order(Sort.Direction.DESC, "point"));
            Query query = new Query(criteria).with(sort).limit(20);
            return mongoTemplate.find(query, LeaderboardScoreMonth.class, "leaderboard_score_months");
        } else {
            List<LeaderboardScoreMonth> data = new ArrayList<>();
            Criteria criteria = Criteria.where("monthNo").is(monthNo).and("yearNo").is(year).and("memberId").is(memberId);
            Query query = new Query(criteria).limit(1);
            LeaderboardScoreMonth memberMonth = mongoTemplate.findOne(query, LeaderboardScoreMonth.class, "leaderboard_score_months");
            if (memberMonth != null) {
                Double point = memberMonth.getPoint().doubleValue();
                Criteria criteria_m = Criteria.where("monthNo").is(monthNo).and("yearNo").is(year).and("point").gt(point);
                Query query_m = new Query(criteria_m);
                Long count = mongoTemplate.count(query_m, "leaderboard_score_months");
                if (point > 0 && count == 0)
                    count = 1L;
                else if (count > 0)
                    count += 1;
                memberMonth.setRank(count);
                data.add(memberMonth);
            }
            return data;
        }
    }

    public List<LeaderboardScore> findLeaderboadardScore(Long memberId){
        if (memberId == 0) {
            Sort sort = Sort.by(new Sort.Order(Sort.Direction.DESC, "point"));
            Query query = new Query().with(sort).limit(20);
            return mongoTemplate.find(query, LeaderboardScore.class, "leaderboard_scores");
        } else {
            List<LeaderboardScore> data = new ArrayList<>();
            Criteria criteria = Criteria.where("memberId").is(memberId);
            Query query = new Query(criteria).limit(1);
            LeaderboardScore memberOverall = mongoTemplate.findOne(query, LeaderboardScore.class, "leaderboard_scores");
            if (memberOverall != null) {
                Double point = memberOverall.getPoint().doubleValue();
                Criteria criteria_m = Criteria.where("point").gt(point);
                Query query_m = new Query(criteria_m);
                Long count = mongoTemplate.count(query_m, "leaderboard_scores");
                if (point > 0 && count == 0)
                    count = 1L;
                else if (count > 0)
                    count += 1;
                memberOverall.setRank(count);
                data.add(memberOverall);
            }
            return data;
        }
    }

    public Integer maxWeekNoLeaderboard(Integer year) {
        Integer maxWeek = 0;
        try {
            while (year >= 2025) {
                Aggregation aggregation = Aggregation.newAggregation(
                        Aggregation.match(Criteria.where("yearNo").is(year)),
                        Aggregation.sort(Sort.Direction.DESC, "weekNo"),
                        Aggregation.limit(1),
                        Aggregation.project("weekNo")
                );

                AggregationResults<Document> result = mongoTemplate.aggregate(aggregation, "leaderboard_score_weeks", Document.class);

                maxWeek = result.getUniqueMappedResult() != null ? result.getUniqueMappedResult().getInteger("weekNo") : null;
                if (maxWeek == null) {
                    year -= 1;
                } else
                    return maxWeek;
            }
        } catch (Exception e) {
            // TODO
        }
        return maxWeek;
    }

    public Integer maxMonthNoLeaderboard(Integer year) {
        Integer maxMonth = 0;
        try {
            while (year >= 2025) {
                Aggregation aggregation = Aggregation.newAggregation(
                        Aggregation.match(Criteria.where("yearNo").is(year)),
                        Aggregation.sort(Sort.Direction.DESC, "monthNo"),
                        Aggregation.limit(1),
                        Aggregation.project("monthNo")
                );

                AggregationResults<Document> result = mongoTemplate.aggregate(aggregation, "leaderboard_score_months", Document.class);

                maxMonth = result.getUniqueMappedResult() != null ? result.getUniqueMappedResult().getInteger("monthNo") : null;
                if (maxMonth == null) {
                    year -= 1;
                } else
                    return maxMonth;
            }
        } catch (Exception e) {
            // TODO
        }
        return maxMonth;
    }

    public List<LeaderboadardCalendar> getLeaderboardCalendar(Integer year, boolean isWeek) {
        if (!isWeek) {
            Criteria criteria = Criteria.where("yearNo").is(year);
            Query query = new Query(criteria);
            return mongoTemplate.find(query, LeaderboadardCalendar.class, "leaderboard_calendars");
        } else {
            Criteria criteria = Criteria.where("weekYearNo").is(year);
            Query query = new Query(criteria);
            return mongoTemplate.find(query, LeaderboadardCalendar.class, "leaderboard_calendars");
        }
    }
}
