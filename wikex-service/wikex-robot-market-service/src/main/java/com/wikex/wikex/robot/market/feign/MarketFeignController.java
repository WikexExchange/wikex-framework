package com.wikex.wikex.robot.market.feign;

import com.wikex.wikex.robot.market.engine.MarketEngineFactory;
import com.wikex.wikex.robot.market.entity.CoinThumb;
import com.wikex.wikex.robot.market.utils.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MarketFeignController {

	@Autowired
	private MarketEngineFactory marketEngineFactory;

	/**
	 * Get trading pair market data
	 * 
	 * @param pair Trading pair (e.g. btcusdt)
	 * @return Market data of the trading pair
	 */
	@RequestMapping("thumb4Feign/{pair}")
	public MessageResult findThumb(@PathVariable(value = "pair") String pair) {
		CoinThumb thumb = marketEngineFactory.getThumbByPair(pair);
		if (thumb != null) {
			MessageResult mr = new MessageResult(0, "success");
			mr.setData(thumb);
			return mr;
		} else {
			MessageResult mr = new MessageResult(500, "Trading pair market data not found");
			return mr;
		}
	}

}
