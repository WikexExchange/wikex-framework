package com.wikex.wikex.robot.market.engine;


import com.wikex.wikex.robot.market.entity.CoinThumb;

import java.util.List;
import java.util.Map;

public interface MarketEngine {
	
	
	public void syncMarket();
	
	
	public boolean containsPair(String pair);
	
	
	public List<CoinThumb> thumbList();
	
	
	public CoinThumb getCoinThumb(String pair);
	
	
	public Long getLastUpdateTime();
	
	
	public boolean updateEngineUrl(String url);
	
	
	public Map<String, String> getAliasMapping();
	
	
	public int addAliasMapping(String name, String alias);
	
	
	public int removeAliasMapp(String name);

	public String getUrl();
}
