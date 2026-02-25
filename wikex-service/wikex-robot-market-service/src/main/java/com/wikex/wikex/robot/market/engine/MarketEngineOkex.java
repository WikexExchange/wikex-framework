package com.wikex.wikex.robot.market.engine;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.robot.market.entity.CoinThumb;
import com.wikex.wikex.robot.market.utils.HttpClientUtil;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Okex Market Engine
 */
public class MarketEngineOkex implements MarketEngine {
	private final static Logger logger = LoggerFactory.getLogger(MarketEngineOkex.class);

	private String engineName = "Okex";

	private String allTickerUrl = "https://www.okx.com/api/v5/market/tickers?instType=SPOT"; // Market data fetch URL

	private Long updateTime = 0L; // Last update time

	private ConcurrentHashMap<String, CoinThumb> tickers = new ConcurrentHashMap<String, CoinThumb>();

	// Name mapping
	private Map<String, String> mappingPair = new HashMap<String, String>();

	@Override
	public void syncMarket() {
		try {
			String retStr = HttpClientUtil.doHttpsGet(allTickerUrl, null, null);
			if (retStr != null && !retStr.equals("")) {

				JSONObject retObj = JSONObject.parseObject(retStr);
				if (retObj.getString("code").equals("0")) {

					JSONArray tickerArr = retObj.getJSONArray("data");
					if (tickerArr != null && tickerArr.size() > 0) {
						for (int i = 0; i < tickerArr.size(); i++) {
							JSONObject obj = tickerArr.getJSONObject(i);

							String coinPair = obj.getString("instId").replace("-", "").toLowerCase();
							CoinThumb thumb = this.getThumb(coinPair);

							thumb.setPrice(obj.getBigDecimal("last"));
							thumb.setHigh(obj.getBigDecimal("high24h"));
							thumb.setLow(obj.getBigDecimal("low24h"));
							thumb.setLastUpdate(System.currentTimeMillis());
						}

						this.updateTime = System.currentTimeMillis();
					}
				}
			}
		} catch (KeyManagementException e1) {
			logger.error(this.engineName + " - [KeyManagementException]" + e1.getMessage());
		} catch (ClientProtocolException e1) {
			logger.error(this.engineName + " - [ClientProtocolException]" + e1.getMessage());
		} catch (NoSuchAlgorithmException e1) {
			logger.error(this.engineName + " - [NoSuchAlgorithmException]" + e1.getMessage());
		} catch (KeyStoreException e1) {
			logger.error(this.engineName + " - [KeyStoreException]" + e1.getMessage());
		} catch (IOException e1) {
			logger.error(this.engineName + " - [IOException]" + e1.getMessage());
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
