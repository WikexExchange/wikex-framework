package com.wikex.wikex.robot.market.config;

import com.wikex.wikex.robot.market.engine.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MarketEngineConfig {
	@Bean
	public MarketEngineFactory marketEngineFactory() {
		
		MarketEngineFactory factory = new MarketEngineFactory();

		MarketEngine okexEngine = new MarketEngineOkex();
		factory.addEngine("Okex", okexEngine);
		
//		MarketEngine zbEngine = new MarketEngineZb();
//		factory.addEngine("Zb", zbEngine);
		
		MarketEngine huobiEngine = new MarketEngineHuobi();
		factory.addEngine("Huobi", huobiEngine);

		MarketEngine binanceEngine = new MarketEngineBinance();
		factory.addEngine("Binance", binanceEngine);
		
//		MarketEngine bikiEngine = new MarketEngineBiki();
//		factory.addEngine("Biki", bikiEngine);
//
//		MarketEngine fxhEngine = new MarketEngineFxh();
//		factory.addEngine("Fxh", fxhEngine);
		
		return factory;
	}
}
