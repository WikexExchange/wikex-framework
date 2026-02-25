package com.wikex.wikex.user.task;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.rpc.feign.RpcFeign;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.Coinprotocol;
import com.wikex.wikex.user.entity.Withdraw;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.user.feign.CoinprotocolFeign;
import com.wikex.wikex.user.service.WithdrawService;
import com.wikex.wikex.util.MessageResult;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@Slf4j
public class CheckWithdrawJob {
	@Autowired
	private CoinprotocolFeign coinprotocolFeign;
	@Autowired
	private CoinFeign coinFeign;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private WithdrawService withdrawService;
	@Autowired
	private RpcFeign rpcFeign;
	@Value("${wallet.type}")
	private String walletType;

	@XxlJob("checkWithdraw")
	public void checkWithdraw() {

		List<Withdraw> list = withdrawService.findWithdrawByStatus(1);
		if (list != null && list.size() > 0) {
			for (Withdraw withdraw : list) {

				withdrawService.updateWithdrawStatus(withdraw.getId(), 2);
				handleWithdraw(withdraw);
			}
		}

	}

	private void handleWithdraw(Withdraw record) {

		if (record == null || record.getStatus() != 1) {
			return;
		}
		Coinprotocol protocol = coinprotocolFeign.findByProtocol(record.getProtocol());
		Long withdrawId = record.getId();
		try {
			Coin coin = coinFeign.findByUnit(record.getCoinName());

			if (coin != null && coin.getCanWithdraw() == BooleanEnum.IS_TRUE.getCode()) {
				MessageResult mr = null;
				boolean result = false;
				if ("udun".equalsIgnoreCase(walletType)) {

				} else {

					String serviceName = protocol.getSymbol().toLowerCase();
					mr = rpcFeign.withdraw(serviceName, record.getAddress(), record.getRealMoney(), true,
							record.getCoinName(), record.getId().toString());

					if (mr != null && mr.getCode() == 0) {
						result = true;
					}
				}

				if (result) {

					if ("udun".equalsIgnoreCase(walletType)) {

					} else {
						String txid = (String) mr.getData();
						withdrawService.withdrawSuccess(withdrawId, txid);
					}

				} else {

					withdrawService.updateWithdrawStatus(withdrawId, 3);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("auto withdraw failed,error={}", e.getMessage());

			withdrawService.updateWithdrawStatus(withdrawId, 3);
		}
	}

}
