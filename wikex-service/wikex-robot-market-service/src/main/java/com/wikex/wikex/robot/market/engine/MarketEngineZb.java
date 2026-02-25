package com.wikex.wikex.robot.market.engine;

import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.robot.market.entity.CoinThumb;
import com.wikex.wikex.robot.market.utils.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ZB Market Engine
 */
public class MarketEngineZb implements MarketEngine {

	private final static Logger logger = LoggerFactory.getLogger(MarketEngineOkex.class);

	private String engineName = "Zb";

	private String allTickerUrl = "http://api.zb.work/data/v1/allTicker"; // Market data fetch URL
	private Long updateTime = 0L; // Last update time

	private ConcurrentHashMap<String, CoinThumb> tickers = new ConcurrentHashMap<String, CoinThumb>();

	// Name mapping (e.g. bchusdt -> bchabcusdt, bsvusdt -> bchsvusdt)
	private Map<String, String> mappingPair = new HashMap<String, String>();

	public MarketEngineZb() {
		// Initialize mapping relations
		mappingPair.put("bchusdt", "bchabcusdt");
		mappingPair.put("bsvusdt", "bchsvusdt");
	}

	@Override
	public void syncMarket() {
		try {
			String retStr = HttpClientUtil.doHttpGet(allTickerUrl, null, null);
			if (retStr != null && !retStr.equals("")) {
				JSONObject retObj = JSONObject.parseObject(retStr);
				int count = 0;
				for (Map.Entry entry : retObj.entrySet()) {
					String coinPair = entry.getKey().toString().toLowerCase();
					CoinThumb thumb = this.getThumb(coinPair);
					JSONObject obj = JSONObject.parseObject(entry.getValue().toString());
					thumb.setPrice(obj.getBigDecimal("last"));
					thumb.setHigh(obj.getBigDecimal("high"));
					thumb.setLow(obj.getBigDecimal("low"));
					thumb.setLastUpdate(System.currentTimeMillis());
					count++;
				}
				this.updateTime = System.currentTimeMillis();
			}
		} catch (Exception e) {
			logger.error(this.engineName + " - " + e.getMessage());
		}
	}

	private CoinThumb getThumb(String pair) {
		if (!tickers.containsKey(pair)) {
			CoinThumb thumb = new CoinThumb();
			tickers.put(pair, thumb);
		}
		return tickers.get(pair);
	}

	@Override
	public boolean containsPair(String pair) {
		return tickers.containsKey(pair);
	}

	@Override
	public List<CoinThumb> thumbList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CoinThumb getCoinThumb(String pair) {
		if (mappingPair.containsKey(pair)) {
			pair = mappingPair.get(pair);
		}

		if (tickers.containsKey(pair)) {
			return tickers.get(pair);
		}

		return null;
	}

	@Override
	public Long getLastUpdateTime() {
		return updateTime;
	}

	@Override
	public boolean updateEngineUrl(String url) {
		this.allTickerUrl = url;
		return true;
	}

	@Override
	public Map<String, String> getAliasMapping() {
		return this.mappingPair;
	}

	@Override
	public int addAliasMapping(String name, String alias) {
		this.mappingPair.put(name, alias);
		return 0;
	}

	@Override
	public int removeAliasMapp(String name) {
		this.mappingPair.remove(name);
		return 0;
	}

	@Override
	public String getUrl() {
		return this.allTickerUrl;
	}
}
