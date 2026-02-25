package com.wikex.wikex.match.trader;

import java.util.concurrent.ConcurrentHashMap;

public class CoinTraderFactory {

	private ConcurrentHashMap<String, CoinTrader> traderMap;

	public CoinTraderFactory() {
		traderMap = new ConcurrentHashMap<>();
	}

	public void addTrader(String symbol, CoinTrader trader) {
		if(!traderMap.containsKey(symbol)) {
			traderMap.put(symbol, trader);
		}
	}

	public void resetTrader(String symbol, CoinTrader trader) {
		traderMap.put(symbol, trader);
	}

	public boolean containsTrader(String symbol) {
		return traderMap.containsKey(symbol);
	}

	public CoinTrader getTrader(String symbol) {
		return traderMap.get(symbol);
	}

	public ConcurrentHashMap<String, CoinTrader> getTraderMap() {
		return traderMap;
	}

}
