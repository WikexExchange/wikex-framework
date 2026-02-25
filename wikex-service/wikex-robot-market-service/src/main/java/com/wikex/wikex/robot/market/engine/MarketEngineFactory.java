package com.wikex.wikex.robot.market.engine;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.robot.market.entity.CoinThumb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MarketEngineFactory {

	private final static Logger logger  =  LoggerFactory.getLogger(MarketEngineFactory.class);
	private ConcurrentHashMap<String, MarketEngine> engineMap;

    public MarketEngineFactory() {
    	engineMap = new ConcurrentHashMap<>();
    }
    
    public void addEngine(String market, MarketEngine engine) {
    	if(!this.containsEngine(market)) {
    		engineMap.put(market, engine);
    	}
    }
    
    public boolean containsEngine(String market) {
    	return engineMap != null && engineMap.containsKey(market);
    }
    
    public MarketEngine getEngine(String market) {
    	return engineMap.get(market);
    }
    
    public ConcurrentHashMap<String, MarketEngine> getEngineList() {
    	return engineMap;
    }
    
    public List<JSONObject> engineStatus() {
    	List<JSONObject> list = new ArrayList<JSONObject>();
    	engineMap.forEach((engineName, engine)->{
    		JSONObject obj = new JSONObject();
    		obj.put("market", engineName);
    		obj.put("url", engine.getUrl());
    		obj.put("updateTime", engine.getLastUpdateTime());
    		list.add(obj);
    	});
    	return list;
    }

    public CoinThumb getThumbByPair(String pair){
    	List<CoinThumb> thumbs = new ArrayList<CoinThumb>();
    	Long currentTime = System.currentTimeMillis();
    	engineMap.forEach((engineName, engine)->{
    		CoinThumb tem = engine.getCoinThumb(pair);
    		
    		if(tem != null && (currentTime - tem.getLastUpdate() < 180000)) {
    			thumbs.add(tem);
    		}
    	});
    	
    	if(thumbs.size() > 0) {
    		CoinThumb thumb = null;
    		int index = 0;
    		for(int i = 0; i < thumbs.size(); i++) {
    			if(thumbs.get(i).getPrice().compareTo(BigDecimal.ZERO) > 0) {
    				if(thumb != null) {
    					
    					if(thumbs.get(i).getLastUpdate() > thumb.getLastUpdate()) {
    						thumb = thumbs.get(i);
    						index = i;
    					}
    				}else {
    					thumb = thumbs.get(i);
    				}
    			}
    		}
    		
    		if(thumbs.size() > 2) {
    			int count = 0;
    			int newIndex = 0;
	    		for(int j = 0; j < thumbs.size(); j++) {
	    			if( j != index) {
	    				BigDecimal percent = thumbs.get(j).getPrice().subtract(thumb.getPrice()).abs().divide(thumb.getPrice(), 3, BigDecimal.ROUND_HALF_DOWN);
	    				if(percent.compareTo(BigDecimal.valueOf(0.1)) > 0) {
	    					count++;
	    					newIndex = j;
	    				}
	    			}
	    		}
	    		
	    		if(count > 1) {
	    			thumb = thumbs.get(newIndex);
	    		}
    		}
			return thumb;
    	}
    	return null;
    }
}
