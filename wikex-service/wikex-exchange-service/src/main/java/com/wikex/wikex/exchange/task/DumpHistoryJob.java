package com.wikex.wikex.exchange.task;

import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.exchange.entity.ExchangeOrderDetail;
import com.wikex.wikex.exchange.service.ExchangeOrderService;
import com.wikex.wikex.user.service.MemberTransactionService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
@Slf4j
public class DumpHistoryJob {

	@Autowired
	private ExchangeOrderService exchangeOrderService;
    @Autowired
    private MemberTransactionService memberTransactionService;

    @Autowired
    private MongoTemplate mongoTemplate ;

    
	@XxlJob("deleteHistoryOrders")
	public void deleteHistoryOrders(){
		
		long beforeTime = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000); 
		
		int limit = 100;
		int deleteTime = 1;
		int deleteCount = 0;
		List<ExchangeOrder> list = exchangeOrderService.queryHistoryDelete(beforeTime,limit);
		boolean hashNext =true;
		while (hashNext) {
			hashNext = false;
			if (list != null && list.size() > 0) {
				deleteCount = deleteCount + list.size();
				Set<String> orderIds = new HashSet<>();
				for (int i = 0; i < list.size(); i++) {
					ExchangeOrder exchangeOrder = list.get(i);
					orderIds.add(exchangeOrder.getOrderId());
				}
				
				if(orderIds.size()>0) {
					Query query = new Query(Criteria.where("orderId").in(orderIds));
					
					mongoTemplate.remove(query, ExchangeOrderDetail.class);
					
					exchangeOrderService.removeByIds(orderIds);
				}

				deleteTime++;
				if (list.size() == limit) {
					hashNext = true;
					try {
						Thread.sleep(500); // Sleep 100ms between batches to reduce CPU load
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
					list = exchangeOrderService.queryHistoryDelete(beforeTime,limit);
				}
			}
		}

		Date today = new Date();
		Calendar now =Calendar.getInstance();
		now.setTime(today);
		now.set(Calendar.DATE,now.get(Calendar.DATE) - 5); 
		Date startTime = now.getTime();

		memberTransactionService.deleteHistory(startTime);
	}
}
