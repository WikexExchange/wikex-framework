package com.wikex.wikex.admin.controller.index;

import com.wikex.wikex.admin.entity.MemberLog;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.TransactionTypeEnum;
import com.wikex.wikex.exchange.feign.ExchangeCoinFeign;
import com.wikex.wikex.p2p.feign.AppealFeign;
import com.wikex.wikex.p2p.feign.BusinessAuthFeign;
import com.wikex.wikex.user.feign.MemberApplicationFeign;
import com.wikex.wikex.user.feign.WithdrawFeign;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.MessageResult;
import com.wikex.wikex.vo.ExchangeTurnoverStatisticsVO;
import com.wikex.wikex.vo.TurnoverStatisticsVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.shiro.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController("index")
@RequestMapping("index/statistics")
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private ExchangeCoinFeign exchangeCoinService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MemberApplicationFeign memberApplicationService;

    @Autowired
    private BusinessAuthFeign businessAuthApplyService;

    @Autowired
    private AppealFeign appealService;

    @Autowired
    private WithdrawFeign withdrawRecordService;

    @PostMapping("member-statistics-info")
    @AccessLog(module = AdminModule.INDEX, operation = "Index member statistics")
    public MessageResult getYestodayStatisticsInfo(
            @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date startDate,
            @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date endDate) {

        if (endDate == null) {
            endDate = DateUtil.getDate(new Date(), 1);
        }

        ProjectionOperation projectionOperation = Aggregation.project("date", "registrationNum", "applicationNum", "bussinessNum");

        List<Criteria> criterias = new ArrayList<>();

        criterias.add(Criteria.where("date").lte(endDate));

        if (startDate != null) {
            criterias.add(Criteria.where("date").gte(startDate));
        }

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("date").ne(null).andOperator(
                        criterias.toArray(new Criteria[criterias.size()])
                )
        );
        GroupOperation groupOperation = Aggregation.group().sum("registrationNum").as("registrationNum")
                .sum("applicationNum").as("applicationNum")
                .sum("bussinessNum").as("bussinessNum");

        Aggregation aggregation = Aggregation.newAggregation(projectionOperation, matchOperation, groupOperation);

        AggregationResults<Map> aggregationResults = this.mongoTemplate.aggregate(aggregation, "member_log", Map.class);

        List<Map> list = aggregationResults.getMappedResults();

        Query query = new Query(Criteria.where("date").is(DateUtil.getDate(new Date(), 1)));

        List<MemberLog> list1 = mongoTemplate.find(query, MemberLog.class);
        MemberLog log = list1 == null || list1.size() < 1 ? new MemberLog() : list1.get(0);
        Map map = new HashMap();
        if (list != null && list.size() > 0) {
            map = list.get(0);
            map.put("yesterdayRegistrationNum", log.getRegistrationNum());
            map.put("yesterdayApplicationNum", log.getApplicationNum());
            map.put("yesterdayBussinessNum", log.getBussinessNum());
        } else {
            map.put("yesterdayRegistrationNum", 0);
            map.put("yesterdayApplicationNum", 0);
            map.put("yesterdayBussinessNum", 0);
        }

        map.remove("_id");
        return MessageResult.getSuccessInstance("", list);

    }

    /**
     * Index member statistics chart
     *
     * @param startDate start date
     * @param endDate   end date
     * @return chart data
     */
    @PostMapping("member-statistics-chart")
    @AccessLog(module = AdminModule.INDEX, operation = "Index member statistics chart")
    public MessageResult getMemberStatisticsChart(
            @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date startDate,
            @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date endDate
    ) {

        Assert.notNull(startDate, "startDate must not be null");

        Assert.notNull(endDate, "endDate must not be null");

        List<Criteria> criterias = new ArrayList<>();

        criterias.add(Criteria.where("date").gte(startDate));

        criterias.add(Criteria.where("date").lte(endDate));

        Query query = new Query(Criteria.where("date").ne(null).andOperator(criterias.toArray(new Criteria[criterias.size()])));

        List<Map> list = mongoTemplate.find(query, Map.class, "member_log");

        for (Map map : list) {
            map.remove("_id");
            map.remove("year");
            map.remove("month");
            map.remove("day");
        }

        return MessageResult.getSuccessInstance("", list);
    }

    @PostMapping("otc-statistics-turnover")
    @AccessLog(module = AdminModule.INDEX, operation = "OTC turnover statistics")
    public MessageResult otcStatistics(
            @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date startDate,
            @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date endDate,
            String unit
    ) {

        Assert.notNull(unit, "unit must not be null ......");

        if (endDate == null) {
            endDate = DateUtil.getDate(new Date(), 1);
        }

        ProjectionOperation projectionOperation = Aggregation.project("date", "type", "unit", "amount", "fee");

        List<Criteria> criterias = new ArrayList<>();

        criterias.add(Criteria.where("date").lte(endDate));

        if (startDate != null) {
            criterias.add(Criteria.where("date").gte(startDate));
        }

        criterias.add(Criteria.where("unit").is(unit));

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("type").in(TransactionTypeEnum.OTC_NUM.toString()
                        , TransactionTypeEnum.OTC_MONEY.toString()).andOperator(
                        criterias.toArray(new Criteria[criterias.size()])
                )
        );

        GroupOperation groupOperation = Aggregation.group("unit", "type")
                .sum("amount").as("amount")
                .sum("fee").as("fee");

        Aggregation aggregation = Aggregation.newAggregation(projectionOperation, matchOperation, groupOperation);

        AggregationResults<Map> aggregationResults = this.mongoTemplate.aggregate(aggregation, "turnover_statistics", Map.class);

        List<Map> list = aggregationResults.getMappedResults();

        Map<String, Object> result = getResults(unit, list, TransactionTypeEnum.OTC_NUM.toString(), TransactionTypeEnum.OTC_MONEY.toString());

        List<Map> yesterdayList = yerterdayQuery(unit, TransactionTypeEnum.OTC_NUM);

        boolean flag = yesterdayList != null && yesterdayList.size() > 0;

        result.put("yesterdayAmount", flag && yesterdayList.get(0).get("amount") != null ? new BigDecimal(yesterdayList.get(0).get("amount").toString()) : 0);

        result.put("yesterdayFee", flag && yesterdayList.get(0).get("fee") != null ? new BigDecimal(yesterdayList.get(0).get("fee").toString()) : 0);

        return MessageResult.getSuccessInstance("", result);
    }

    /**
     * Spot volume/amount/fee totals for the index page
     *
     * @param startDate start date
     * @param endDate   end date
     * @param unit      unit
     * @return totals
     */
    @PostMapping("exchange-statistics-turnover")
    @AccessLog(module = AdminModule.INDEX, operation = "Index spot volume/amount/fee totals")
    public MessageResult exchangeStatistics(
            @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date startDate,
            @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date endDate,
            String unit
    ) {
        Assert.notNull(unit, "unit must not be null ......");

        if (endDate == null) {
            endDate = DateUtil.getDate(new Date(), 1);
        }

        ProjectionOperation projectionOperation = Aggregation.project("date", "type", "unit", "amount", "fee");

        List<Criteria> criterias = new ArrayList<>();

        criterias.add(Criteria.where("date").lte(endDate));

        if (startDate != null) {
            criterias.add(Criteria.where("date").gte(startDate));
        }

        criterias.add(Criteria.where("unit").is(unit));

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("type").is(TransactionTypeEnum.EXCHANGE_COIN.toString()).andOperator(
                        criterias.toArray(new Criteria[criterias.size()])
                )
        );

        GroupOperation groupOperation = Aggregation.group("unit", "type")
                .sum("amount").as("amount");

        Aggregation aggregation = Aggregation.newAggregation(projectionOperation, matchOperation, groupOperation);

        AggregationResults<Map> aggregationResults = this.mongoTemplate.aggregate(aggregation, "turnover_statistics", Map.class);

        List<Map> list = aggregationResults.getMappedResults();

        matchOperation = Aggregation.match(
                Criteria.where("type").is(TransactionTypeEnum.EXCHANGE.toString()).andOperator(
                        criterias.toArray(new Criteria[criterias.size()])));

        groupOperation = Aggregation.group("unit", "type").sum("fee").as("fee");

        aggregation = Aggregation.newAggregation(projectionOperation, matchOperation, groupOperation);

        aggregationResults = this.mongoTemplate.aggregate(aggregation, "turnover_statistics", Map.class);

        List<Map> list1 = aggregationResults.getMappedResults();

        List<Map> yesterdayAmounts = yerterdayQuery(unit, TransactionTypeEnum.EXCHANGE_COIN);

        List<Map> yesterdayFees = yerterdayQuery(unit, TransactionTypeEnum.EXCHANGE);

        Map map = new HashMap();

        boolean flag = list != null && list.size() > 0 && list.get(0).get("amount") != null;

        boolean flag2 = yesterdayAmounts != null && yesterdayAmounts.size() > 0 && yesterdayAmounts.get(0).get("amount") != null;

        boolean flag3 = yesterdayFees != null && yesterdayFees.size() > 0 && yesterdayFees.get(0).get("fee") != null;

        boolean flag4 = list1 != null && list1.size() > 0 && list1.get(0).get("fee") != null;

        map.put("amount", flag ? new BigDecimal(list.get(0).get("amount").toString()) : 0);

        map.put("type", TransactionTypeEnum.EXCHANGE.toString());

        map.put("unit", unit);

        map.put("yesterdayAmount", flag2 ? new BigDecimal(yesterdayAmounts.get(0).get("amount").toString()) : 0);

        map.put("yesterdayFee", flag3 ? new BigDecimal(yesterdayFees.get(0).get("fee").toString()) : 0);

        if (list1 != null && list1.size() > 0) {

            map.put("fee", flag4 ? new BigDecimal(list1.get(0).get("fee").toString()) : 0);

        }

        return MessageResult.getSuccessInstance("", map);
    }

    /**
     * Process result set (aggregate OTC/spot volume/amount/fee/unit into a single map)
     *
     * @param list      source list
     * @param typeNum   volume type key
     * @param typeMoney amount type key
     * @return aggregated map
     */
    private Map<String, Object> getResults(String unit, List<Map> list, String typeNum, String typeMoney) {

        Map<String, Object> map0 = new HashMap<>();
        map0.put("unit", unit);
        for (Map map : list) {
            logger.info("OTC raw turnover info:{}", map);
            if (map.get("type").toString().equals(typeNum)) {
                map0.put("amount", map.get("amount") != null ? new BigDecimal(map.get("amount").toString()) : 0);
                map0.put("fee", map.get("fee") != null ? new BigDecimal(map.get("fee").toString()) : 0);
            } else if (map.get("type").toString().equals(typeMoney)) {
                map0.put("money", map.get("amount") != null ? new BigDecimal(map.get("amount").toString()) : 0);
            }
        }
        map0.put("type", "OTC");
        logger.info("OTC turnover info:{}", map0);
        return map0;
    }

    /**
     * OTC volume chart
     *
     * @param startDate start date
     * @param endDate   end date
     * @param units     units collection
     * @return chart data
     */
    @PostMapping("/otc-statistics-num-chart")
    @AccessLog(module = AdminModule.INDEX, operation = "OTC volume chart")
    public MessageResult otcNumChart(
            @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date startDate,
            @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date endDate,
            String[] units/*,
            TransactionTypeEnum type*/) {

        // Assert that this API is for OTC statistics; type can be 0 (volume) or 1 (amount)

        Assert.notNull(startDate, "startDate must not be null ......");

        Assert.notEmpty(units, "units must not be null");

        if (endDate == null) {
            endDate = DateUtil.getDate(new Date(), 1);
        }

        // Assert.notNull(type,"type must not be null");

        ProjectionOperation projectionOperation = Aggregation.project("date", "type", "unit", "amount");

        List<Criteria> criterias = new ArrayList<>();

        criterias.add(Criteria.where("date").gte(startDate));

        criterias.add(Criteria.where("date").lte(endDate));

        criterias.add(Criteria.where("unit").in(units));

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("type").is(TransactionTypeEnum.OTC_NUM.toString())
                        .andOperator(criterias.toArray(new Criteria[criterias.size()]))

        );

        Aggregation aggregation = Aggregation.newAggregation(projectionOperation, matchOperation);

        AggregationResults<TurnoverStatisticsVO> aggregationResults = this.mongoTemplate.aggregate(aggregation, "turnover_statistics", TurnoverStatisticsVO.class);

        List<TurnoverStatisticsVO> list = aggregationResults.getMappedResults();

        list = list.stream().sorted((x, y) -> {
            if (x.getDate().after(y.getDate())) {
                return -1;
            } else {
                return 1;
            }
        }).collect(Collectors.toList());

        logger.info("OTC volume chart:{}", list);

        return MessageResult.getSuccessInstance("", list);
    }

    /**
     * Spot turnover chart (by trading pair)
     *
     * Index totals by trading pair (total volume/amount)
     *
     * @param startDate  start date
     * @param endDate    end date
     * @param baseSymbol base coin (legal/payment coin)
     * @param coinSymbols platform coins
     * @return chart data
     */
    @PostMapping("exchange-statistics-turnover-chart")
    @AccessLog(module = AdminModule.INDEX, operation = "Spot turnover chart (by trading pair)")
    public MessageResult exchangeNumStatistics(
            @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date startDate,
            @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date endDate,
            String baseSymbol,
            String[] coinSymbols) {

        Assert.notNull(startDate, "startDate must not be null ......");

        Assert.notNull(baseSymbol, "baseSymbol must not be null");

        if (coinSymbols == null || coinSymbols.length < 1) {
            List<String> list0 = exchangeCoinService.getCoinSymbol(baseSymbol);
            coinSymbols = list0 == null ? null : list0.toArray(new String[list0.size()]);
        }

        if (endDate == null) {
            endDate = DateUtil.getDate(new Date(), 1);
        }

        ProjectionOperation projectionOperation = Aggregation.project("date", "baseSymbol", "coinSymbol", "amount", "money").andExclude("_id");

        List<Criteria> criterias = new ArrayList<>();

        criterias.add(Criteria.where("date").gte(startDate));

        criterias.add(Criteria.where("date").lte(endDate));

        criterias.add(Criteria.where("coinSymbol").in(coinSymbols));

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("baseSymbol").is(baseSymbol)
                        .andOperator(criterias.toArray(new Criteria[criterias.size()]))
        );

        Aggregation aggregation = Aggregation.newAggregation(projectionOperation, matchOperation);

        AggregationResults<ExchangeTurnoverStatisticsVO> aggregationResults = this.mongoTemplate.aggregate(aggregation, "exchange_turnover_statistics", ExchangeTurnoverStatisticsVO.class);

        List<ExchangeTurnoverStatisticsVO> list = aggregationResults.getMappedResults();

        logger.info("Spot turnover chart:{}", list);

        return MessageResult.getSuccessInstance("", list);
    }

    /**
     * To-do items
     *
     * @return counts
     */
    @GetMapping("affairs")
    public MessageResult affairs() {
        // Real-name (KYC) review
        long applicationNum = memberApplicationService.countAuditing();
        // Merchant review
        Integer businessAuthNum = businessAuthApplyService.applyCountAuditing();
        // Order appeals
        Integer appealNum = appealService.countAuditing();
        // Merchant cancellation review
        Integer businessCancelNum = businessAuthApplyService.cancelCountAuditing();
        // Withdraw review
        Integer withdrawRecordNum = withdrawRecordService.countAuditing();
        Map<String, Object> map = new HashMap<>();
        map.put("applicationNum", applicationNum);
        map.put("businessAuthNum", businessAuthNum);
        map.put("appealNum", appealNum);
        map.put("businessCancelNum", businessCancelNum);
        map.put("withdrawRecordNum", withdrawRecordNum);
        return MessageResult.getSuccessInstance("", map);
    }

    private List<Map> yerterdayQuery(String unit, TransactionTypeEnum type) {

        Query query = new Query(Criteria.where("date").is(DateUtil.getDate(new Date(), 1))
                .and("type").is(type.toString())
                .and("unit").is(unit));

        List<Map> list = mongoTemplate.find(query, Map.class, "turnover_statistics");
        return list;
    }

    @PostMapping("all-exchange-coin")
    public MessageResult getAllExchangeCoin() {
        List<String> list = exchangeCoinService.getAllCoin();
        return MessageResult.getSuccessInstance("", list);
    }
}
