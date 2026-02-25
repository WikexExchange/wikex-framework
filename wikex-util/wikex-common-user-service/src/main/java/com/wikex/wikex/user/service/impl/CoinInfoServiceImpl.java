package com.wikex.wikex.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.CoinInfo;
import com.wikex.wikex.user.entity.CoinLink;
import com.wikex.wikex.user.mapper.CoinInfoMapper;
import com.wikex.wikex.user.service.CoinInfoService;
import com.wikex.wikex.user.service.CoinLinkService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CoinInfoServiceImpl extends ServiceImpl<CoinInfoMapper, CoinInfo> implements CoinInfoService {

    @Autowired
    private CoinLinkService coinLinkService;

    @Override
    public CoinInfo findByCoinId(Long coinId) {
        QueryWrapper<CoinInfo> qw = new QueryWrapper<>();
        qw.eq("coin_id", coinId);
        return this.getOne(qw);
    }

    @Override
    public CoinInfo fetchFromCoinGecko(String coingeckoId) {
        String url = "https://api.coingecko.com/api/v3/coins/" + coingeckoId
                + "?localization=false&tickers=false&market_data=true&community_data=false&developer_data=false&sparkline=false";
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                return null;
            }

            JSONObject json = JSON.parseObject(response.getBody());
            JSONObject marketData = json.getJSONObject("market_data");
            if (marketData == null) {
                return null;
            }

            BigDecimal totalSupply = marketData.getBigDecimal("total_supply");
            BigDecimal maxSupply = marketData.getBigDecimal("max_supply");
            BigDecimal circulatingSupply = marketData.getBigDecimal("circulating_supply");

            if (totalSupply == null)
                totalSupply = BigDecimal.ZERO;
            if (maxSupply == null)
                maxSupply = BigDecimal.ZERO;
            if (circulatingSupply == null)
                circulatingSupply = BigDecimal.ZERO;

            QueryWrapper<CoinInfo> qw = new QueryWrapper<>();
            qw.eq("coingecko_id", coingeckoId);
            CoinInfo coinInfo = this.getOne(qw);

            if (coinInfo == null) {
                coinInfo = new CoinInfo();
                coinInfo.setCoingeckoId(coingeckoId);
            }
            coinInfo.setTotalSupply(totalSupply);
            coinInfo.setMaxSupply(maxSupply);
            coinInfo.setCirculatingSupply(circulatingSupply);

            this.saveOrUpdate(coinInfo);
            return coinInfo;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Map<String, Object> fetchInfoToken(Long coinId) {
        CoinInfo ci = null;
        if (coinId != null) {
            QueryWrapper<CoinInfo> qw = new QueryWrapper<>();
            qw.eq("coin_id", coinId);
            ci = this.getOne(qw);
        }

        BigDecimal marketCapUsd = ci != null && ci.getMarketCapUsd() != null ? ci.getMarketCapUsd() : BigDecimal.ZERO;
        BigDecimal fdvUsd = ci != null && ci.getFdvUsd() != null ? ci.getFdvUsd() : BigDecimal.ZERO;
        BigDecimal circulatingSupply = ci != null && ci.getCirculatingSupply() != null ? ci.getCirculatingSupply()
                : BigDecimal.ZERO;
        BigDecimal totalSupply = ci != null && ci.getTotalSupply() != null ? ci.getTotalSupply() : BigDecimal.ZERO;
        BigDecimal maxSupply = ci != null && ci.getMaxSupply() != null ? ci.getMaxSupply() : BigDecimal.ZERO;
        String description = ci != null && ci.getDescription() != null ? ci.getDescription() : "";

        List<Map<String, String>> explore = new ArrayList<>();
        List<Map<String, String>> official = new ArrayList<>();
        List<Map<String, String>> social = new ArrayList<>();

        if (coinId != null) {
            List<CoinLink> links = coinLinkService.findByCoinId(coinId);
            if (links != null && !links.isEmpty()) {
                for (CoinLink cl : links) {
                    Map<String, String> m = new HashMap<>();
                    m.put("name", cl.getName());
                    m.put("url", cl.getUrl());

                    String type = cl.getType() != null ? cl.getType().toLowerCase() : "";

                    switch (type) {
                        case "explorer":
                            explore.add(m);
                            break;
                        case "official":
                            official.add(m);
                            break;
                        case "social":
                            social.add(m);
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        // --- Output ---
        Map<String, Object> data = new HashMap<>();
        data.put("marketCapUsd", marketCapUsd);
        data.put("fdvUsd", fdvUsd);
        data.put("circulatingSupply", circulatingSupply);
        data.put("totalSupply", totalSupply);
        data.put("maxSupply", maxSupply);
        data.put("explore", explore);
        data.put("officialLinks", official);
        data.put("social", social);
        data.put("description", description);

        return data;
    }

    @Override
    public Map<String, Object> fetchFullInfo(String coingeckoId) {
        RestTemplate rt = new RestTemplate();
        String url = "https://api.coingecko.com/api/v3/coins/" + coingeckoId
                + "?localization=false&tickers=false&market_data=true&community_data=false&developer_data=false&sparkline=false";

        String body;
        try {
            body = rt.getForObject(url, String.class);
        } catch (Exception e) {
            return null;
        }

        if (body == null) {
            return null;
        }

        JSONObject json = JSON.parseObject(body);
        JSONObject market = json.getJSONObject("market_data");
        JSONObject links = json.getJSONObject("links");
        JSONObject desc = json.getJSONObject("description");

        BigDecimal totalSupply = market != null ? market.getBigDecimal("total_supply") : BigDecimal.ZERO;
        BigDecimal maxSupply = market != null ? market.getBigDecimal("max_supply") : BigDecimal.ZERO;
        BigDecimal circulatingSupply = market != null ? market.getBigDecimal("circulating_supply") : BigDecimal.ZERO;

        BigDecimal marketCap = BigDecimal.ZERO;
        BigDecimal fdv = BigDecimal.ZERO;

        if (market != null) {
            JSONObject mc = market.getJSONObject("market_cap");
            if (mc != null && mc.getBigDecimal("usd") != null) {
                marketCap = mc.getBigDecimal("usd");
            }

            JSONObject fdvJson = market.getJSONObject("fully_diluted_valuation");
            if (fdvJson != null && fdvJson.getBigDecimal("usd") != null) {
                fdv = fdvJson.getBigDecimal("usd");
            }
        }

        // Explorers
        List<Map<String, String>> explore = new ArrayList<>();
        if (links != null) {
            JSONArray explorers = links.getJSONArray("blockchain_site");
            if (explorers != null) {
                for (int i = 0; i < explorers.size(); i++) {
                    String u = explorers.getString(i);
                    if (u != null && !u.trim().isEmpty()) {
                        Map<String, String> m = new HashMap<>();
                        m.put("url", u);
                        explore.add(m);
                    }
                }
            }
        }

        // Official links
        List<Map<String, String>> official = new ArrayList<>();
        if (links != null) {
            JSONArray homes = links.getJSONArray("homepage");
            if (homes != null) {
                for (int i = 0; i < homes.size(); i++) {
                    String u = homes.getString(i);
                    if (u != null && !u.trim().isEmpty()) {
                        Map<String, String> m = new HashMap<>();
                        m.put("Website", u);
                        official.add(m);
                    }
                }
            }

            String whitepaper = links.getString("whitepaper");
            if (whitepaper != null && !whitepaper.isEmpty()) {
                Map<String, String> m = new HashMap<>();
                m.put("WhitePaper", whitepaper);
                official.add(m);
            }
        }

        // Social
        List<Map<String, String>> social = new ArrayList<>();
        if (links != null) {

            String twitter = links.getString("twitter_screen_name");
            if (twitter != null && !twitter.trim().isEmpty()) {
                Map<String, String> m = new HashMap<>();
                m.put("name", "Twitter");
                m.put("url", "https://twitter.com/" + twitter);
                social.add(m);
            }

            String facebook = links.getString("facebook_username");
            if (facebook != null && !facebook.trim().isEmpty()) {
                Map<String, String> m = new HashMap<>();
                m.put("name", "Facebook");
                m.put("url", "https://facebook.com/" + facebook);
                social.add(m);
            }

            String subreddit = links.getString("subreddit_url");
            if (subreddit != null && !subreddit.trim().isEmpty()) {
                Map<String, String> m = new HashMap<>();
                m.put("name", "Reddit");
                m.put("url", subreddit);
                social.add(m);
            }
        }

        // Description
        String description = desc != null ? desc.getString("en") : "";

        Map<String, Object> data = new HashMap<>();
        data.put("marketCapUsd", marketCap);
        data.put("fdvUsd", fdv);
        data.put("circulatingSupply", circulatingSupply);
        data.put("totalSupply", totalSupply);
        data.put("maxSupply", maxSupply);
        data.put("explore", explore);
        data.put("officialLinks", official);
        data.put("social", social);
        data.put("description", description);

        return data;
    }
}
