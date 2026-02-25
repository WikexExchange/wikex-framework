package com.wikex.wikex.robot.market.controller;

import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.robot.market.engine.MarketEngine;
import com.wikex.wikex.robot.market.engine.MarketEngineFactory;
import com.wikex.wikex.robot.market.entity.CoinThumb;
import com.wikex.wikex.robot.market.utils.MessageResult;
import com.wikex.wikex.service.LocaleMessageSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MarketController {

	@Autowired
	private MarketEngineFactory marketEngineFactory;
	@Autowired
	private LocaleMessageSourceService messageSource;

	
	@RequestMapping("/{pair}")
	public MessageResult findThumb(@PathVariable(value = "pair") String pair){
		CoinThumb thumb = marketEngineFactory.getThumbByPair(pair);
		if(thumb != null) {
			MessageResult mr = new MessageResult(0,"success");
			mr.setData(thumb);
			return mr;
		}else {
			MessageResult mr = new MessageResult(500,"No trading pair found");
			return mr;
		}
	}

	
	@RequestMapping("status")
	public MessageResult status(){
		List<JSONObject> engineStatus = marketEngineFactory.engineStatus();
		MessageResult mr = new MessageResult(0,"success");
		mr.setData(engineStatus);
		return mr;
	}

	
	@RequestMapping("updateUrl")
	public MessageResult updateEngineUrl(String market, String url) {
		if(marketEngineFactory.containsEngine(market)) {
			MarketEngine engine = marketEngineFactory.getEngine(market);
			engine.updateEngineUrl(url);
			MessageResult mr = new MessageResult(0, messageSource.getMessage("UPDATE_SUCCESS_LATEST_MARKET_URL")+url);
			return mr;
		}else {
			MessageResult mr = new MessageResult(500, messageSource.getMessage("MARKET_ENGINE_NOT_FOUND"));
			return mr;
		}
	}

	
	@RequestMapping("addMaping")
	public MessageResult addMapping(String market, String name, String alias) {
		if(marketEngineFactory.containsEngine(market)) {
			MarketEngine engine = marketEngineFactory.getEngine(market);
			engine.addAliasMapping(name, alias);
			MessageResult mr = new MessageResult(0, messageSource.getMessage("ADD_UPDATE_ALIAS_MAPPING") + " > " + name + " - " + alias);
			return mr;
		}else {
			MessageResult mr = new MessageResult(500, messageSource.getMessage("MARKET_ENGINE_NOT_FOUND"));
			return mr;
		}
	}

	
	@RequestMapping("removeMaping")
	public MessageResult addMapping(String market, String name) {
		if(marketEngineFactory.containsEngine(market)) {
			MarketEngine engine = marketEngineFactory.getEngine(market);
			engine.removeAliasMapp(name);
			MessageResult mr = new MessageResult(0, messageSource.getMessage("ADD_UPDATE_ALIAS_MAPPING") + " > " + name);
			return mr;
		}else {
			MessageResult mr = new MessageResult(500, messageSource.getMessage("MARKET_ENGINE_NOT_FOUND"));
			return mr;
		}
	}
}
