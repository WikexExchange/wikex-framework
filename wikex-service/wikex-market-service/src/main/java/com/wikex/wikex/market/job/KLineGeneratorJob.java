package com.wikex.wikex.market.job;

import com.wikex.wikex.market.processor.CoinProcessorFactory;
import com.wikex.wikex.market.service.KlineRobotMarketService;
import com.wikex.wikex.market.util.WebSocketConnectionManage;
import com.wikex.wikex.util.DateUtil;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.concurrent.RejectedExecutionException;

/**
 * Generate K-line (candlestick) information for different time periods
 *
 */
@Component
@Slf4j
public class KLineGeneratorJob {
	private Logger logger = LoggerFactory.getLogger(KLineGeneratorJob.class);
    @Autowired
    private CoinProcessorFactory processorFactory;

	@Autowired
	private TaskExecutor taskExecutor;
	@Autowired
	private KlineRobotMarketService klineRobotMarketService;

	public static String PERIOD[] ={ "1min", "5min", "15min", "30min", "60min", "1day", "1mon", "1week" };

    /**
     * Timer for every minute, process minute K-line
     */
//    @Scheduled(cron = "0 * * * * *")
	@XxlJob("handle5minKLine")
    public void handle5minKLine(){
		try {
			Calendar calendar = Calendar.getInstance();
			// Set second and millisecond fields to 0
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			long time = calendar.getTimeInMillis();
			int minute = calendar.get(Calendar.MINUTE);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			processorFactory.getProcessorMap().forEach((symbol, processor) -> {
				if (!processor.isStopKline()) {
					try {
						taskExecutor.execute(new Runnable() {
							@Override
							public void run() {
								processor.generateKLine(time, minute, hour);
							}
						});
					} catch (RejectedExecutionException e) {
						logger.warn("Rejected K-line task for symbol: {}", symbol, e);
					}
				}
			});
		} catch (Exception e) {
			logger.error("Error handle5minKLine", e);
		}
    }

	/**
     * Run every hour
     */
//    @Scheduled(cron = "0 0 * * * *")
	@XxlJob("handleHourKLine")
    public void handleHourKLine(){
		try {
			processorFactory.getProcessorMap().forEach((symbol,processor)-> {
				if(!processor.isStopKline()) {
					Calendar calendar = Calendar.getInstance();

					// Set minute, second and millisecond fields to 0
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 0);
					long time = calendar.getTimeInMillis();

					processor.generateKLine(1, Calendar.HOUR_OF_DAY, time);

					int m = calendar.get(Calendar.HOUR_OF_DAY);
					if(m%4==0){
						processor.generateKLine(4, Calendar.HOUR_OF_DAY, time);
					}
				}
			});
		} catch (Exception e) {
			logger.error("Error handleHourKLine", e);
		}
    }

    /**
     * Daily processor at midnight, process daily K-line
     */
//    @Scheduled(cron = "0 0 0 * * *")
	@XxlJob("handleDayKLine")
    public void handleDayKLine(){
		try {
			processorFactory.getProcessorMap().forEach((symbol,processor)->{
				if(!processor.isStopKline()) {
					Calendar calendar = Calendar.getInstance();

					// Set hour, minute, second, and millisecond fields to 0
					calendar.set(Calendar.HOUR_OF_DAY,0);
					calendar.set(Calendar.MINUTE,0);
					calendar.set(Calendar.SECOND,0);
					calendar.set(Calendar.MILLISECOND,0);
					long time = calendar.getTimeInMillis();
					int week = calendar.get(Calendar.DAY_OF_WEEK);
					int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
					if(week == 1){
						processor.generateKLine(1, Calendar.WEEK_OF_MONTH, time);
					}
					if(dayOfMonth == 1){
						processor.generateKLine(1, Calendar.MONTH, time);
					}
					processor.generateKLine(1, Calendar.DAY_OF_YEAR,time);
				}
			});
		} catch (Exception e) {
			logger.error("Error handleDayKLine", e);
		}
    }

	/**
	 * Timer for every minute, process minute K-line
	 */
//	@Scheduled(cron = "5 */1 * * * ?")
//	public void handle5minKLine(){
//		processorFactory.getProcessorMap().forEach((symbol,processor)->{
//			if(!processor.isStopKline()) {
//				taskExecutor.execute(new Runnable() {
//					@Override
//					public void run() {
//						syncKLine(symbol);
//					}
//				});
//			}
//		});
//	}

	public void syncKLine(String symbol){
//		List<Symbol> symbols = klineRobotMarketService.findAllSymbol();

		// Get current time (seconds)
		Long currentTime = DateUtil.getTimeMillis() / 1000;
		// Initialize K-line, time point
//        int count = 2000;
		
		for(String period : PERIOD) {
//			long fromTime = 0;
			long fromTime = klineRobotMarketService.findMaxTimestamp(symbol,period); // +1 is to avoid fetching the last K-line again
			if(fromTime<=1){
				fromTime = 0;
			}else {
				fromTime = fromTime/1000;
			}
			long timeGap = currentTime - fromTime;
			
			if(period.equals("1min") && timeGap >= 60 ) { // Exceeds 1 minute
				if(fromTime == 0) {
					// Initialization, fetch the latest 600 K-lines
					WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, currentTime - 60 * 2500, currentTime);
				}else{
					// Non-initialization, fetch the latest generated K-lines
					long toTime = fromTime + (timeGap / 60) * 60 - 5;
					WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, fromTime, toTime);
				}
			}

			if(period.equals("5min") && timeGap >= 60 * 5 ) { // Exceeds 5 minutes
				if(fromTime == 0) {
					// Initialization, fetch the latest 600 K-lines
					WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, currentTime - 5 * 60 * 1000, currentTime);
				}else{
					// Non-initialization, fetch the latest generated K-lines
					long toTime = fromTime + (timeGap / (60 * 5)) * (60*5) - 5;
					WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, fromTime, toTime);
				}
			}

			if(period.equals("15min") && timeGap >= (60 * 15 )) { // Exceeds 15 minutes
				if(fromTime == 0) {
					// Initialization, fetch the latest 600 K-lines
					WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, currentTime - 15 * 60 * 1000, currentTime);
				}else {
					// Non-initialization, fetch the latest generated K-lines
					long toTime = fromTime + (timeGap / (60 * 15)) * (60 * 15) - 5;
					WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, fromTime, toTime);
				}
			}

			if(period.equals("30min") && timeGap >= (60 * 30 )) { // Exceeds 30 minutes
				if(fromTime == 0) {
					// Initialization, fetch the latest 600 K-lines
					WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, currentTime - 30 * 60 * 1000, currentTime);
				}else{
					long toTime = fromTime + (timeGap / (60 * 30)) * (60 * 30) - 5;
					WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, fromTime, toTime);
				}
			}

			if(period.equals("60min") && timeGap >= (60 * 60 )) { // Exceeds 60 minutes
				if(fromTime == 0) {
					// Initialization, fetch the latest 600 K-lines
					WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, currentTime - 60 * 60 * 1000, currentTime);
				}else{
					long toTime = fromTime + (timeGap / (60 * 60)) * (60 * 60) - 5;
					WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, fromTime, toTime);
				}
			}

			if(period.equals("4hour") && timeGap >= (60 * 60 * 4 )) { // Exceeds 4 hours
				if(fromTime == 0) {
					// Initialization, fetch the latest 600 K-lines
					WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, currentTime - 4 * 60 * 60 * 600, currentTime);
				}else{
					long toTime = fromTime + (timeGap / (60 * 60 * 4)) * (60 * 60 * 4) - 5;
					WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, fromTime, toTime);
				}
			}

			if(period.equals("1day") && timeGap >= (60 * 60 * 24)) { // Exceeds 24 hours
				if(fromTime == 0) {
					// Initialization, fetch the latest 600 K-lines
					WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, currentTime - 24 * 60 * 60 * 1000, currentTime);
				}else {
					long toTime = fromTime + (timeGap / (60 * 60 * 24)) * (60 * 60 * 24) - 5;
					WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, fromTime, toTime);
				}
			}

			if(period.equals("1week") && timeGap >= (60 * 60 * 24 * 7)) { // Exceeds 1 week
				if(fromTime == 0) {
					// Initialization, fetch the latest 600 K-lines
					WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, currentTime - 7 * 24 * 60 * 60 * 500, currentTime);
				}else{
					long toTime = fromTime + (timeGap / (60 * 60 * 24*7)) * (60 * 60 * 24*7) - 5;
					WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, fromTime, toTime);
				}
			}

			if(period.equals("1mon") && timeGap >= (60 * 60 * 24 * 30)) { // Exceeds 1 month
				if(fromTime == 0) {
					// Initialization, fetch the latest 600 K-lines
					WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, currentTime - 30 * 24 * 60 * 60 * 100, currentTime);
				}else{
					long toTime = fromTime + (timeGap / (60 * 60 * 24 * 30)) * (60 * 60 * 24 * 30) - 5;
					WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, fromTime, toTime);
				}
			}
		}
	}
}
