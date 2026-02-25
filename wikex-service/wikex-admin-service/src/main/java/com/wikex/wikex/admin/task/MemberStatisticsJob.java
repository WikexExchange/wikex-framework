package com.wikex.wikex.admin.task;

import com.wikex.wikex.admin.dao.MemberLogDao;
import com.wikex.wikex.admin.entity.ExchangeTurnoverStatistics;
import com.wikex.wikex.admin.entity.MemberLog;
import com.wikex.wikex.admin.entity.TurnoverStatistics;
import com.wikex.wikex.constant.TransactionTypeEnum;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.exchange.feign.ExchangeOrderFeign;
import com.wikex.wikex.p2p.feign.OtcOrderFeign;
import com.wikex.wikex.user.entity.MemberDeposit;
import com.wikex.wikex.user.feign.MemberDepositFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.WithdrawFeign;
import com.wikex.wikex.user.vo.WithdrawVO;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.vo.OtcOrderVO;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

@Component
@Slf4j
public class MemberStatisticsJob {

    private static Logger logger = LoggerFactory.getLogger(MemberStatisticsJob.class);

    @Autowired
    private MemberFeign memberFeign ;

    @Autowired
    private OtcOrderFeign otcOrderFeign;

    @Autowired
    private ExchangeOrderFeign exchangeOrderFeign;

    @Autowired
    private MemberDepositFeign memberDepositFeign ;

    @Autowired
    private MongoTemplate mongoTemplate ;

    @Autowired
    private WithdrawFeign withdrawFeign ;

    @Autowired
    private MemberLogDao memberLogDao ;

    /**
     * Member registration / real-name verification / merchant certification statistics
     */
//    @Scheduled(cron = "0 34 3 * * ?")
    @XxlJob("statisticsMember")
    public void statisticsMember() {
        try {
            if(!mongoTemplate.collectionExists("member_log")){
                List<Date> list = getDateList();
                String dateStr = "";
                for(Date date :list){
                    dateStr = DateUtil.YYYY_MM_DD.format(date) ;
                    statisticsMember(dateStr,date);
                }
            }else{
                Date date = DateUtil.dateAddDay(DateUtil.getCurrentDate(),-1);
                String dateStr = DateUtil.getFormatTime(DateUtil.YYYY_MM_DD,date);
                statisticsMember(dateStr,date);
            }
        } catch (ParseException e) {
            logger.error("Date parsing exception",e);
        }

    }

    /**
     * Fiat / deposit / withdrawal fees
     * Spot trading fees statistics
     * Fiat trading volume / turnover statistics
     */
//    @Scheduled(cron = "0 24 3 * * ?")
    @XxlJob("turnoverStatistics")
    public void turnoverStatistics() {
        try {
            if(!mongoTemplate.collectionExists("turnover_statistics")){
                List<Date> list = getDateList();
                String dateStr = "";
                for(Date date :list){
                    dateStr = DateUtil.YYYY_MM_DD.format(date) ;
                    statisticsFee(dateStr,date);
                }
            }else{
                Date date = DateUtil.dateAddDay(DateUtil.getCurrentDate(),-1);
                String dateStr = DateUtil.getFormatTime(DateUtil.YYYY_MM_DD,date);
                statisticsFee(dateStr,date);
            }

        } catch (ParseException e) {
            logger.error("Date parsing exception",e);
        }

    }

    /**
     * Spot trading volume / turnover statistics
     */
    //    @Scheduled(cron = "0 14 3 * * ?")
    @XxlJob("exchangeStatistics")
    public void exchangeStatistics(){
        try {
            if(!mongoTemplate.collectionExists("exchange_turnover_statistics")){
                List<Date> list = getDateList();
                String dateStr = "";
                for(Date date :list){
                    dateStr = DateUtil.YYYY_MM_DD.format(date) ;
                    exchangeStatistics(dateStr,date);
                }
            }else{
                Date date = DateUtil.dateAddDay(DateUtil.getCurrentDate(),-1);
                String dateStr = DateUtil.getFormatTime(DateUtil.YYYY_MM_DD,date);
                exchangeStatistics(dateStr,date);
            }

        } catch (ParseException e) {
            logger.error("Date parsing exception",e);
        }
    }

    private void statisticsMember(String dateStr,Date date) throws ParseException {
        // logger.info("Start statistics for member info {}",dateStr);
        int registrationNum = memberFeign.getRegistrationNum(dateStr);
        int bussinessNum = memberFeign.getBussinessNum(dateStr);
        int applicationNum = memberFeign.getApplicationNum(dateStr);
        MemberLog memberLog = new MemberLog();
        memberLog.setApplicationNum(applicationNum);
        memberLog.setBussinessNum(bussinessNum);
        memberLog.setRegistrationNum(registrationNum);
        memberLog.setDate(DateUtil.YYYY_MM_DD.parse(dateStr));
        memberLog.setYear(DateUtil.getDatePart(date,Calendar.YEAR));
        // Calendar month defaults to starting from 0, for convenience, save month starting from 1
        memberLog.setMonth(DateUtil.getDatePart(date,Calendar.MONTH)+1);
        memberLog.setDay(DateUtil.getDatePart(date,Calendar.DAY_OF_MONTH));
        // logger.info("{} member info {}",dateStr,memberLog);
        memberLogDao.save(memberLog);
        // logger.info("End statistics for member info {}",dateStr);
    }

    private List<Date> getDateList() throws ParseException {
        List<Date> list = new ArrayList<>() ;

        Date date = memberFeign.getStartRegistrationDate();
        String dateStr = DateUtil.YYYY_MM_DD.format(date) ;
        date = DateUtil.YYYY_MM_DD.parse(dateStr);

        Calendar calendar = Calendar.getInstance() ;
        calendar.setTime(date);
        Date endDate = DateUtil.dateAddDay(new Date(),-1) ;
        while(date.before(endDate)){
            list.add(date);
            calendar.add(Calendar.DAY_OF_MONTH,1);
            date = calendar.getTime() ;
        }
        return list;
    }

    private void statisticsFee(String dateStr,Date date) throws ParseException {
        /**
         * Fiat trades
         */
        TurnoverStatistics turnoverStatistics = new TurnoverStatistics();
        // logger.info("Start statistics for fiat trades {}",dateStr);
//        try {
//            List<OtcOrderVO> list1 = otcOrderFeign.getOtcOrderStatistics(dateStr);
//            turnoverStatistics.setDate(DateUtil.YYYY_MM_DD.parse(dateStr));
//            turnoverStatistics.setYear(DateUtil.getDatePart(date, Calendar.YEAR));
//            // Calendar month defaults to starting from 0, for convenience, save month starting from 1
//            turnoverStatistics.setMonth(DateUtil.getDatePart(date, Calendar.MONTH) + 1);
//            turnoverStatistics.setDay(DateUtil.getDatePart(date, Calendar.DAY_OF_MONTH));
//            for (OtcOrderVO ord : list1) {
//                /**
//                 * Fiat trading volume / fee
//                 */
//                turnoverStatistics.setUnit(ord.getUnit());
//                turnoverStatistics.setAmount(ord.getNumber());
//                turnoverStatistics.setFee(ord.getFee());
//                turnoverStatistics.setType(TransactionTypeEnum.OTC_NUM);
//                // logger.info("{} fiat trading info {}",dateStr,turnoverStatistics);
//                mongoTemplate.insert(turnoverStatistics, "turnover_statistics");
//
//                /**
//                 * Fiat turnover
//                 */
//                turnoverStatistics.setAmount(ord.getMoney());
//                turnoverStatistics.setType(TransactionTypeEnum.OTC_MONEY);
//                turnoverStatistics.setFee(null);
//                mongoTemplate.insert(turnoverStatistics, "turnover_statistics");
//            }
//            // logger.info("End statistics for fiat trades {}",dateStr);
//        } catch (Exception ex) {
//            logger.error("Error statisticsFee -> Fiat trades", ex);
//        }

        /**
         * Spot turnover
         */
        try {
            // logger.info("Start statistics for spot turnover {}",dateStr);
            turnoverStatistics.setFee(null);
            List<ExchangeOrder> list2 = exchangeOrderFeign.getExchangeTurnoverBase(dateStr);
            for (ExchangeOrder order : list2) {
                turnoverStatistics.setUnit(order.getBaseSymbol());
                turnoverStatistics.setAmount(order.getTurnover());
                turnoverStatistics.setType(TransactionTypeEnum.EXCHANGE_BASE);
                // logger.info("{} spot turnover info {}",dateStr,turnoverStatistics);
                mongoTemplate.insert(turnoverStatistics, "turnover_statistics");
            }
            // logger.info("End statistics for spot turnover {}",dateStr);
        } catch (Exception ex) {
            logger.error("Error statisticsFee -> Spot turnover", ex);
        }

        /**
         * Spot trading volume
         */
        try {
            // logger.info("Start statistics for spot trading volume {}",dateStr);
            List<ExchangeOrder> list3 = exchangeOrderFeign.getExchangeTurnoverCoin(dateStr);
            for (ExchangeOrder order : list3) {
                turnoverStatistics.setUnit(order.getCoinSymbol());
                turnoverStatistics.setAmount(order.getTradedAmount());
                turnoverStatistics.setType(TransactionTypeEnum.EXCHANGE_COIN);
                // logger.info("{} spot trading volume info {}",dateStr,turnoverStatistics);
                mongoTemplate.insert(turnoverStatistics, "turnover_statistics");
            }
            // logger.info("End statistics for spot trading volume {}",dateStr);
        } catch (Exception ex) {
            logger.error("Error statisticsFee -> Spot trading volume", ex);
        }

        /**
         * Deposits
         */
//        try {
//            // logger.info("Start statistics for deposits {}",dateStr);
//            List<MemberDeposit> list4 = memberDepositFeign.getDepositStatistics(dateStr);
//            for (MemberDeposit deposit : list4) {
//                turnoverStatistics.setAmount(deposit.getAmount());
//                turnoverStatistics.setUnit(deposit.getUnit());
//                turnoverStatistics.setType(TransactionTypeEnum.RECHARGE);
//                // logger.info("{} deposit info {}",dateStr,turnoverStatistics);
//                mongoTemplate.insert(turnoverStatistics, "turnover_statistics");
//            }
//            // logger.info("End statistics for deposits {}",dateStr);
//        } catch (Exception ex) {
//            logger.error("Error statisticsFee -> Deposits", ex);
//        }
        /**
         * Spot trading fees
         */
        try {
            // logger.info("Start statistics for spot trading fees {}",dateStr);
            ProjectionOperation projectionOperation = Aggregation.project("time", "type", "unit", "fee");

            Criteria operator = Criteria.where("coinName").ne("").andOperator(
                    Criteria.where("time").gte(DateUtil.YYYY_MM_DD_MM_HH_SS.parse(dateStr + " 00:00:00").getTime()),
                    Criteria.where("time").lte(DateUtil.YYYY_MM_DD_MM_HH_SS.parse(dateStr + " 23:59:59").getTime()),
                    Criteria.where("type").is("EXCHANGE")
            );

            MatchOperation matchOperation = Aggregation.match(operator);

            GroupOperation groupOperation = Aggregation.group("unit", "type").sum("fee").as("feeSum");

            Aggregation aggregation = Aggregation.newAggregation(projectionOperation, matchOperation, groupOperation);
            // Execute operation
            AggregationResults<Map> aggregationResults = this.mongoTemplate.aggregate(aggregation, "order_detail_aggregation", Map.class);
            List<Map> list = aggregationResults.getMappedResults();
            for (Map map : list) {
                // logger.info("*********{} spot trading fee {}************",dateStr,map);
                turnoverStatistics.setFee(new BigDecimal(map.get("feeSum").toString()));
                turnoverStatistics.setAmount(null);
                turnoverStatistics.setUnit(map.get("unit").toString());
                turnoverStatistics.setType(TransactionTypeEnum.EXCHANGE);
                // logger.info("{} spot trading fee info {}",dateStr,turnoverStatistics);
                mongoTemplate.insert(turnoverStatistics, "turnover_statistics");
            }
            // logger.info("End statistics for spot trading fees {}",dateStr);
        } catch (Exception ex) {
            logger.error("Error statisticsFee -> Spot trading fees", ex);
        }
        /**
         * TODO Withdrawal - pending processing
         */
//        try {
//            // logger.info("Start statistics for withdrawals {}",dateStr);
//            List<WithdrawVO> list5 = withdrawFeign.getWithdrawStatistics(dateStr);
//            for (WithdrawVO vo : list5) {
//                turnoverStatistics.setFee(vo.getTotalFee());
//                turnoverStatistics.setAmount(vo.getAmount());
//                turnoverStatistics.setUnit(vo.getUnit());
//                turnoverStatistics.setType(TransactionTypeEnum.WITHDRAW);
//                // logger.info("{} withdrawal info {}",dateStr,turnoverStatistics);
//                mongoTemplate.insert(turnoverStatistics, "turnover_statistics");
//            }
//            // logger.info("End statistics for withdrawals {}",dateStr);
//        } catch (Exception ex) {
//            logger.error("Error statisticsFee -> Withdrawal", ex);
//        }
    }

    private void exchangeStatistics(String dateStr,Date date) throws ParseException {
        /**
         * Spot trades (statistics by trading pair)
         */
        // logger.info("Start statistics for spot trades (by trading pair) {}",dateStr);
        List<ExchangeOrder> list = exchangeOrderFeign.getExchangeTurnoverSymbol(dateStr);
        ExchangeTurnoverStatistics exchangeTurnoverStatistics = new ExchangeTurnoverStatistics() ;
        for(ExchangeOrder order:list){
            exchangeTurnoverStatistics.setDate(DateUtil.YYYY_MM_DD.parse(dateStr));
            exchangeTurnoverStatistics.setAmount(order.getTradedAmount());
            exchangeTurnoverStatistics.setBaseSymbol(order.getBaseSymbol());
            exchangeTurnoverStatistics.setCoinSymbol(order.getCoinSymbol());
            exchangeTurnoverStatistics.setMoney(order.getTurnover());
            exchangeTurnoverStatistics.setYear(DateUtil.getDatePart(date,Calendar.YEAR));
            // Calendar month defaults to starting from 0, for convenience, save month starting from 1
            exchangeTurnoverStatistics.setMonth(DateUtil.getDatePart(date,Calendar.MONTH)+1);
            exchangeTurnoverStatistics.setDay(DateUtil.getDatePart(date,Calendar.DAY_OF_MONTH));
            // logger.info("{} spot trades (by trading pair) {}",dateStr,exchangeTurnoverStatistics);
            mongoTemplate.insert(exchangeTurnoverStatistics,"exchange_turnover_statistics");
        }
        // logger.info("End statistics for spot trades (by trading pair) {}",dateStr);
    }

}
