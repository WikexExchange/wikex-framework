package com.wikex.wikex.admin.controller.finance;

import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.TransactionTypeEnum;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.MessageResult;
import com.wikex.wikex.vo.TurnoverStatisticsVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.shiro.util.Assert;
import org.bson.types.Decimal128;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Finance statistics controller
 */
@RestController
@RequestMapping("finance/statistics")
public class FinanceStatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(FinanceStatisticsController.class);

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private LocaleMessageSourceService messageSource;

    /**
     * Fiat/Spot total volume & total turnover within the range: [startDate, endDate]
     *
     * Note: Only grouped by coin unit; spot does not distinguish by trading pair.
     *
     * @param types     transaction type array
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive, default: tomorrow if null)
     * @param unit      coin unit filter (optional)
     * @return aggregated result list
     */
    @PostMapping("turnover-all")
    @AccessLog(module = AdminModule.FINANCE, operation = "Fiat/Spot total volume & total turnover")
    public MessageResult getResult(
            String[] types,
            @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date startDate,
            @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date endDate,
            String unit) {

        Assert.notNull(types, "type must not be null ");
        if (endDate == null) {
            endDate = DateUtil.getDate(new Date(), 1);
        }

        ProjectionOperation projectionOperation = Aggregation.project("date", "type", "unit", "amount").andExclude("_id");

        List<Criteria> criterias = new ArrayList<>();

        if (startDate != null) {
            criterias.add(Criteria.where("date").gte(startDate));
        }

        criterias.add(Criteria.where("date").lte(endDate));

        if (!StringUtils.isEmpty(unit)) {
            criterias.add(Criteria.where("unit").is(unit));
        }

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("type").in(types)
                        .andOperator(criterias.toArray(new Criteria[0]))
        );

        GroupOperation groupOperation = Aggregation.group("type", "unit").sum("amount").as("amount");

        Aggregation aggregation = Aggregation.newAggregation(projectionOperation, matchOperation, groupOperation);

        AggregationResults<Map> aggregationResults = this.mongoTemplate.aggregate(aggregation, "turnover_statistics", Map.class);

        List<Map> list = aggregationResults.getMappedResults();

        Set<String> units = new HashSet<>();

        for (Map map : list) {
            map.put("amount", ((Decimal128) map.get("amount")).bigDecimalValue());

            units.add(map.get("unit").toString());

            logger.info("Turnover info: {}", map);
        }

        return MessageResult.getSuccessInstance(messageSource.getMessage("SUCCESS"), list);
    }

    /**
     * Fee statistics (total)
     *
     * @param type      ["OTC_NUM","WITHDRAW","EXCHANGE"]
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive, default: tomorrow if null)
     * @param unit      coin unit filter (optional)
     * @return aggregated fee totals
     */
    @PostMapping("fee")
    @AccessLog(module = AdminModule.FINANCE, operation = "Fee statistics total [\"OTC_NUM\",\"WITHDRAW\",\"EXCHANGE\"]")
    public MessageResult getFee(TransactionTypeEnum type
            , @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date startDate
            , @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date endDate
            , String unit) {

        Assert.notNull(type, "type must not be null ");

        Assert.isTrue(type == TransactionTypeEnum.OTC_NUM ||
                type == TransactionTypeEnum.EXCHANGE ||
                type == TransactionTypeEnum.WITHDRAW, messageSource.getMessage("API_IS_FOR_FIAT_STATISTICS"));

        if (endDate == null) {
            endDate = DateUtil.getDate(new Date(), 1);
        }

        Assert.notNull(startDate, "startDate must not be null ");

        ProjectionOperation projectionOperation = Aggregation.project("date", "type", "unit", "fee").andExclude("_id");

        List<Criteria> criterias = new ArrayList<>();

        criterias.add(Criteria.where("date").gte(startDate));
        criterias.add(Criteria.where("date").lte(endDate));

        if (!StringUtils.isEmpty(unit)) {
            criterias.add(Criteria.where("unit").is(unit));
        }

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("type").is(type.toString())
                        .andOperator(criterias.toArray(new Criteria[0]))
        );

        GroupOperation groupOperation = Aggregation.group("type", "unit").sum("fee").as("fee");

        Aggregation aggregation = Aggregation.newAggregation(projectionOperation, matchOperation, groupOperation);

        AggregationResults<Map> aggregationResults = this.mongoTemplate.aggregate(aggregation, "turnover_statistics", Map.class);

        List<Map> list = aggregationResults.getMappedResults();

        for (Map map : list) {
            map.put("fee", ((Decimal128) map.get("fee")).bigDecimalValue());
            logger.info("Fee info: {}", map);
        }

        return MessageResult.getSuccessInstance(messageSource.getMessage("SUCCESS"), list);

    }


    /**
     * Deposit/Withdrawal total amount statistics
     *
     * @param type ["WITHDRAW","RECHARGE"]
     * @return aggregated amount and fee totals
     */
    @PostMapping("recharge-or-withdraw-amount")
    @AccessLog(module = AdminModule.FINANCE, operation = "Deposit/Withdrawal total amount statistics")
    public MessageResult recharge(TransactionTypeEnum type
            , @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date startDate
            , @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date endDate
    ) {

        Assert.isTrue(type == TransactionTypeEnum.WITHDRAW || type == TransactionTypeEnum.RECHARGE,
                "type can only be RECHARGE or WITHDRAW");

        if (endDate == null) {
            endDate = DateUtil.getDate(new Date(), 1);
        }

        Assert.notNull(startDate, "startDate must not be null");

        ProjectionOperation projectionOperation = Aggregation.project("date", "year", "month", "day", "type", "unit", "amount", "fee").andExclude("_id");

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("type").is(type.toString())
                        .andOperator(Criteria.where("date").gte(startDate), Criteria.where("date").lte(endDate))
        );

        GroupOperation groupOperation = Aggregation.group("type", "unit").sum("amount").as("amount").sum("fee").as("fee");

        Aggregation aggregation = Aggregation.newAggregation(projectionOperation, matchOperation, groupOperation);

        AggregationResults<TurnoverStatisticsVO> aggregationResults = this.mongoTemplate.aggregate(aggregation, "turnover_statistics", TurnoverStatisticsVO.class);

        List<TurnoverStatisticsVO> list = aggregationResults.getMappedResults();

        logger.info("{} total: {}", type == TransactionTypeEnum.WITHDRAW ? "Withdrawal" : "Deposit", list);

        return MessageResult.getSuccessInstance(messageSource.getMessage("SUCCESS"), list);
    }

}
