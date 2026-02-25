package com.wikex.wikex.task;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.service.UdunService;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.Coinprotocol;
import com.wikex.wikex.user.entity.Withdraw;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.user.feign.CoinprotocolFeign;
import com.wikex.wikex.user.feign.WithdrawFeign;
import com.uduncloud.sdk.constant.MainCoinType;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Component
@Slf4j
public class CheckWithdrawJob {

	@Autowired
    private WithdrawFeign withdrawFeign;
	@Autowired
	private CoinprotocolFeign coinprotocolFeign;
	@Autowired
	private CoinFeign coinFeign;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private UdunService udunService;


	
	@XxlJob("checkWithdraw")
    public void checkWithdraw(){
		
		List<Withdraw> list = withdrawFeign.findWithdrawByStatus(1);
		if(list!=null && list.size()>0){
			for (Withdraw withdraw : list) {
				
				withdrawFeign.updateWithdrawStatus(withdraw.getId(),2);
				handleWithdraw(withdraw);
			}
		}

	}
	private void handleWithdraw(Withdraw record) {
		
		if (record==null || record.getStatus()!=1 ) {
			return;
		}
		Coinprotocol protocol = coinprotocolFeign.findByProtocol(record.getProtocol());
		Long withdrawId = record.getId();
		try {
			Coin coin = coinFeign.findByUnit(record.getCoinName());
			
			if (coin != null && coin.getCanWithdraw() == BooleanEnum.IS_TRUE.getCode()) {

				boolean result = udunService.withdraw(withdrawId+"",record.getRealMoney(), MainCoinType.symbolOf(protocol.getSymbol()).getCode(),
						udunService.getSubCodeByUnit(coin.getUnit(),protocol.getSymbol()),record.getAddress());

				
				
				if (result) {
					
//					String txid = (String) result.getData();
//					withdrawFeign.withdrawSuccess(record,coin,txid);
				} else {
					
					withdrawFeign.updateWithdrawStatus(withdrawId,3);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("auto withdraw failed,error={}", e.getMessage());
			
			withdrawFeign.updateWithdrawStatus(withdrawId,3);
		}
	}


}
