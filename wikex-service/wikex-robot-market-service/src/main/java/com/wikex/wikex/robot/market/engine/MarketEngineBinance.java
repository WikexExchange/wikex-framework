package com.wikex.wikex.robot.market.engine;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.robot.market.entity.CoinThumb;
import com.wikex.wikex.robot.market.utils.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MarketEngineBinance implements MarketEngine {
	private final static Logger logger = LoggerFactory.getLogger(MarketEngineBinance.class);

	private String engineName = "Binance";

	private String allTickerUrl = "https://api.binance.com/api/v3/ticker";
	private Long updateTime = 0L;

	private ConcurrentHashMap<String, CoinThumb> tickers = new ConcurrentHashMap<String, CoinThumb>();

	private Map<String, String> mappingPair = new HashMap<String, String>();
	private static List<String> tc = new ArrayList<>();
	static {
		tc.add("BSV/USDT");
	}
	@Value("${thumb.wikex.url}")
	private String thumbUrl;

	@Override
	public void syncMarket() {
		try {
			List<String> symbols = new ArrayList<>();

			String allSymbols = thumbUrl;
			String retStr = HttpClientUtil.doHttpsGet(allSymbols, null, null);
			if (retStr != null && !retStr.equals("")) {
				JSONArray symbolsRet = JSONArray.parseArray(retStr);
				for (int i = 0; i < symbolsRet.size(); i++) {
					JSONObject symbol = symbolsRet.getJSONObject(i);
					if (1 == symbol.getInteger("robotType")) {
						continue;
					}
					if (tc.contains(symbol.getString("symbol"))) {
						continue;
					}
					symbols.add(symbol.getString("symbol").replace("/", ""));
				}
			}

			List<String> a = new ArrayList<>();
			for (int i = 0; i < symbols.size(); i++) {
				a.add(symbols.get(i));
				if (i % 90 == 0 && i != 0) {
					getTickerAll(a);
					a.clear();
				}
			}
			if (a.size() > 0) {
				getTickerAll(a);
			}

		} catch (Exception e) {
			logger.error(this.engineName + " - " + e.getMessage());
		}
	}

	private void getTickerAll(List<String> sy)
			throws IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

		Map<String, String> param = new HashMap<>();
		param.put("symbols", JSONArray.toJSONString(sy));
		param.put("windowSize", "1m");
		String retStr = HttpClientUtil.doHttpsGet(allTickerUrl, param, null);
		if (retStr != null && !retStr.equals("")) {
			JSONArray tickerArr = JSONArray.parseArray(retStr);
			if (tickerArr != null && tickerArr.size() > 0) {
				for (int i = 0; i < tickerArr.size(); i++) {
					JSONObject obj = tickerArr.getJSONObject(i);

					String coinPair = obj.getString("symbol").toLowerCase();
					CoinThumb thumb = this.getThumb(coinPair);
					thumb.setPrice(obj.getBigDecimal("lastPrice"));
					thumb.setHigh(obj.getBigDecimal("highPrice"));
					thumb.setLow(obj.getBigDecimal("lowPrice"));
					thumb.setLastUpdate(System.currentTimeMillis());
				}
				this.updateTime = System.currentTimeMillis();
			}

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
		if (tickers.containsKey(pair)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<CoinThumb> thumbList() {
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
