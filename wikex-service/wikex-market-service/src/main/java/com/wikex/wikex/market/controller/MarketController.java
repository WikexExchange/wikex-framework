package com.wikex.wikex.market.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.exchange.entity.ExchangeCoin;
import com.wikex.wikex.exchange.feign.ExchangeCoinFeign;
import com.wikex.wikex.exchange.feign.ExchangeTradeFeign;
import com.wikex.wikex.exchange.feign.MonitorFeign;
import com.wikex.wikex.market.component.CoinExchangeRate;
import com.wikex.wikex.market.processor.CoinProcessor;
import com.wikex.wikex.market.processor.CoinProcessorFactory;
import com.wikex.wikex.market.service.MarketService;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.ExchangeTrade;
import com.wikex.wikex.pojo.KLine;
import com.wikex.wikex.pojo.KLineTemp;
import com.wikex.wikex.pojo.TradePlateItem;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.util.Convert;
import com.wikex.wikex.util.MessageResult;
import com.wikex.wikex.vo.SpecialPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Api(tags = "Market Trading")
@Slf4j
@RestController
public class MarketController {

    @Autowired
    private MarketService marketService;

    @Autowired
    private ExchangeCoinFeign exchangeCoinFeign;

    @Autowired
    private CoinFeign coinFeign;

    @Autowired
    private CoinProcessorFactory coinProcessorFactory;

    @Autowired
    private ExchangeTradeFeign exchangeTradeFeign;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MonitorFeign monitorFeign;

    @Autowired
    private CoinExchangeRate coinExchangeRate;

    /**
     * Get supported trading pairs
     *
     * @return
     */
    @ApiOperation(value = "Get supported trading pairs")
    @RequestMapping("symbol")
    public List<ExchangeCoin> findAllSymbol() {
        List<ExchangeCoin> coins = exchangeCoinFeign.findAllVisible();
        return coins;
    }

    @ApiOperation(value = "Overview")
    @RequestMapping("overview")
    public Map<String, List<CoinThumb>> overview() {
        Map<String, List<CoinThumb>> result = new HashMap<>();
        List<ExchangeCoin> recommendCoin = exchangeCoinFeign.findAllByFlag(1);

        List<CoinThumb> recommendThumbs = new ArrayList<>();
        for (ExchangeCoin coin : recommendCoin) {
            CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
            if (processor != null) {
                CoinThumb thumb = processor.getThumb();
                recommendThumbs.add(thumb);
            }
        }
        result.put("recommend", recommendThumbs);

        List<CoinThumb> allThumbs = findSymbolThumb();
        Collections.sort(allThumbs, (o1, o2) -> o2.getChg().compareTo(o1.getChg()));
        int limit = allThumbs.size() > 5 ? 5 : allThumbs.size();
        result.put("changeRank", new ArrayList<>(allThumbs.subList(0, limit)));
        return result;
    }

    @ApiOperation(value = "Engines")
    @RequestMapping("engines")
    public Map<String, Integer> engines() {
        Map<String, CoinProcessor> processorList = coinProcessorFactory.getProcessorMap();
        Map<String, Integer> symbols = new HashMap<String, Integer>();
        processorList.forEach((key, processor) -> {
            if (processor.isStopKline()) {
                symbols.put(key, 2);
            } else {
                symbols.put(key, 1);
            }
        });
        return symbols;
    }

    /**
     * Get coin details
     *
     * @param unit
     * @return
     */
    @ApiOperation(value = "Get coin details")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "unit", value = "Coin")
    })
    @RequestMapping("coin-info")
    public Coin findCoin(String unit) {
        Coin coin = coinFeign.findByUnit(unit);
        // coin.setColdWalletAddress("");// Hide cold wallet address
        return coin;
    }

    /**
     * Get C2C USDT to CNY price
     *
     * @return
     */
    @ApiOperation(value = "Get C2C USDT to CNY price")
    @RequestMapping("ctc-usdt")
    public MessageResult ctcUsdt() {
        MessageResult mr = new MessageResult(0, "success");
        BigDecimal latestPrice = coinExchangeRate.getUsdtCnyRate();

        JSONObject obj = new JSONObject();
        obj.put("buy", latestPrice);
        // 0.015 represents 1.5% buy-sell spread
        obj.put("sell",
                latestPrice.subtract(latestPrice.multiply(new BigDecimal(0.011)).setScale(2, BigDecimal.ROUND_DOWN)));

        mr.setData(obj);
        return mr;
    }

    /**
     * Get details of a trading pair
     *
     * @param symbol
     * @return
     */
    @ApiOperation(value = "Get details of a trading pair")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol")
    })
    @RequestMapping("symbol-info")
    public ExchangeCoin findSymbol(String symbol) {
        ExchangeCoin coin = exchangeCoinFeign.findBySymbol(symbol);
        coin.setCurrentTime(Calendar.getInstance().getTimeInMillis());
        return coin;
    }

    /**
     * Get coin summary (thumbnail) market data
     *
     * @return
     */
    @ApiOperation(value = "Get coin summary market data")
    @RequestMapping("symbol-thumb")
    public List<CoinThumb> findSymbolThumb() {
        List<ExchangeCoin> coins = exchangeCoinFeign.findAllVisible();
        List<CoinThumb> thumbs = new ArrayList<>();

        for (ExchangeCoin coin : coins) {
            CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
            CoinThumb thumb = processor.getThumb();
            if (thumb == null) {
                continue;
            }

            Coin coinData = coinFeign.findByUnit(coin.getCoinSymbol());
            thumb.setZone(coin.getZone());
            thumb.setRobotType(coin.getRobotType());
            thumb.setCoinUrl(coinData.getIconUrl());
            thumb.setCoinName(coinData.getName());
            thumb.setCoinScale(coin.getCoinScale());
            thumb.setBaseCoinScale(coin.getBaseCoinScale());
            thumbs.add(thumb);
        }
        return thumbs;
    }

    /**
     * API: Get coin summary market data (pagination + sort)
     */
    @ApiOperation(value = "Get coin summary market data (pagination + sort)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "Page number"),
            @ApiImplicitParam(name = "pageSize", value = "Page size"),
            @ApiImplicitParam(name = "sortBy", value = "Sort field: volume, rate, created"),
            @ApiImplicitParam(name = "sortDir", value = "Sort direction: asc, desc")
    })
    @RequestMapping("symbol-thumb-page")
    public SpecialPage<CoinThumb> findSymbolThumbPage(Integer pageNo, Integer pageSize, String sortBy, String sortDir) {
        int currentPage = (pageNo == null || pageNo < 1) ? 1 : pageNo;
        int size = (pageSize == null || pageSize < 1) ? 10 : pageSize;

        List<CoinThumb> allThumbs = findSymbolThumb();

        String field = (sortBy == null) ? "" : sortBy.trim().toLowerCase();
        boolean asc = "asc".equalsIgnoreCase(sortDir);

        final Map<String, Integer> symbolToSort = "created".equals(field)
                ? exchangeCoinFeign.findAllVisible().stream()
                        .collect(Collectors.toMap(
                                ExchangeCoin::getSymbol,
                                c -> (c.getSort() == null ? Integer.MAX_VALUE : c.getSort())))
                : Collections.emptyMap();

        Map<String, Comparator<CoinThumb>> comparators = new HashMap<>();
        comparators.put("volume",
                Comparator.comparing(CoinThumb::getVolume, Comparator.nullsLast(BigDecimal::compareTo)));
        comparators.put("rate", Comparator.comparing(CoinThumb::getChg, Comparator.nullsLast(BigDecimal::compareTo)));
        comparators.put("created", Comparator.comparing(
                thumb -> symbolToSort.getOrDefault(thumb.getSymbol(), Integer.MAX_VALUE)));

        Comparator<CoinThumb> comparator = comparators.get(field);
        if (comparator != null) {
            allThumbs.sort(asc ? comparator : comparator.reversed());
        }

        int totalElements = allThumbs.size();
        int totalPages = (int) Math.ceil(totalElements / (double) size);

        int fromIndex = Math.min((currentPage - 1) * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<CoinThumb> pageContent = (fromIndex >= toIndex)
                ? Collections.emptyList()
                : new ArrayList<>(allThumbs.subList(fromIndex, toIndex));

        SpecialPage<CoinThumb> page = new SpecialPage<>();
        page.setContext(pageContent);
        page.setCurrentPage(currentPage);
        page.setPageNumber(size);
        page.setTotalElement(totalElements);
        page.setTotalPage(totalPages);

        return page;
    }

    /**
     * API: Mini chart data for symbols (24 hourly points)
     *
     * @param symbols Comma separated symbols, e.g. BTC/USDT,ETH/USDT
     * @return
     */

    @ApiOperation(value = "Mini chart data for symbols (24 hourly points)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbols", value = "Comma separated symbols, e.g. BTC/USDT,ETH/USDT")
    })
    @RequestMapping("symbol-thumb-minichart")
    public MessageResult miniChart(String symbols) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        long to = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        long from = calendar.getTimeInMillis();

        JSONArray data = new JSONArray();

        List<String> symbolList;
        if (symbols == null || symbols.trim().isEmpty()) {
            symbolList = exchangeCoinFeign.findAllVisible().stream()
                    .map(ExchangeCoin::getSymbol)
                    .collect(Collectors.toList());
        } else {
            symbolList = Arrays.stream(symbols.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        for (String symbol : symbolList) {
            // Get latest price first
            CoinProcessor processor = coinProcessorFactory.getProcessor(symbol);
            BigDecimal latestPrice = null;
            if (processor != null && processor.getThumb() != null && processor.getThumb().getClose() != null) {
                latestPrice = processor.getThumb().getClose();
            }

            if (latestPrice == null || latestPrice.compareTo(BigDecimal.ZERO) == 0) {
                log.warn("No current price available for symbol: {}, skipping...", symbol);
                continue;
            }

            List<KLine> lines = marketService.findAllKLine(symbol, from, to, "1hour");
            JSONArray trend = new JSONArray();

            // Create a map for faster KLine lookup
            Map<Long, BigDecimal> klineMap = new HashMap<>();
            for (KLine line : lines) {
                if (line.getClosePrice() != null && line.getClosePrice().compareTo(BigDecimal.ZERO) > 0) {
                    klineMap.put(line.getTime(), line.getClosePrice());
                }
            }

            // Generate 24 hourly
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(from);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            BigDecimal lastValidPrice = latestPrice;

            for (int i = 0; i < 24; i++) {
                long timestamp = cal.getTimeInMillis();
                BigDecimal price;

                // Check if we have KLine data for this hour
                if (klineMap.containsKey(timestamp)) {
                    price = klineMap.get(timestamp);
                    lastValidPrice = price;
                } else {
                    // Use the last valid price
                    price = lastValidPrice;
                }

                // Format timestamp as ISO string
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                String timestampStr = sdf.format(new java.util.Date(timestamp));

                JSONArray point = new JSONArray();
                point.add(timestampStr);
                point.add(price);
                trend.add(point);

                cal.add(Calendar.HOUR_OF_DAY, 1);
            }

            JSONObject obj = new JSONObject();
            obj.put("symbol", symbol);
            obj.put("trend", trend);
            data.add(obj);
        }

        MessageResult mr = new MessageResult(0, "success");
        mr.setData(data);
        return mr;
    }

    @ApiOperation(value = "Get coin summary market trend")
    @RequestMapping("symbol-thumb-trend")
    public JSONArray findSymbolThumbWithTrend() {
        List<ExchangeCoin> coins = exchangeCoinFeign.findAllVisible();
        Calendar calendar = Calendar.getInstance();
        // Set seconds and milliseconds to 0
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.MINUTE, 0);

        long nowTime = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        long firstTimeOfToday = calendar.getTimeInMillis();

        JSONArray array = new JSONArray();
        for (ExchangeCoin coin : coins) {
            CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
            CoinThumb thumb = processor.getThumb();

            JSONObject json = (JSONObject) JSON.toJSON(thumb);
            json.put("zone", coin.getZone());

            List<KLine> lines = marketService.findAllKLine(thumb.getSymbol(), firstTimeOfToday, nowTime, "1hour");
            JSONArray trend = new JSONArray();
            for (KLine line : lines) {
                trend.add(line.getClosePrice());
            }
            json.put("trend", trend);
            array.add(json);
        }
        return array;
    }

    /**
     * Get coin historical K-line data
     *
     * @param symbol
     * @param from
     * @param to
     * @param resolution
     * @return
     */
    @ApiOperation(value = "Get coin historical K-line data")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
            @ApiImplicitParam(name = "from", value = "Start time"),
            @ApiImplicitParam(name = "to", value = "End time"),
            @ApiImplicitParam(name = "resolution", value = "Time format"),
    })
    @RequestMapping("history")
    public JSONArray findKHistory(String symbol, long from, long to, String resolution) {
        String period = "";
        if (resolution.endsWith("W") || resolution.endsWith("w") || resolution.endsWith("M") || resolution.endsWith("m")) {
            resolution = "1D";
        }
        if (resolution.endsWith("H") || resolution.endsWith("h")) {
            period = resolution.substring(0, resolution.length() - 1) + "hour";
        } else if (resolution.endsWith("D") || resolution.endsWith("d")) {
            period = resolution.substring(0, resolution.length() - 1) + "day";
        } else if (resolution.endsWith("W") || resolution.endsWith("w")) {
            period = resolution.substring(0, resolution.length() - 1) + "week";
        } else if (resolution.endsWith("M") || resolution.endsWith("m")) {
            period = resolution.substring(0, resolution.length() - 1) + "month";
        } else {
            Integer val = Integer.parseInt(resolution);
            if (val < 60) {
                period = resolution + "min";
            } else {
                if (val == 240)
                    val = 60;
                period = (val / 60) + "hour";
            }
        }

        List<KLine> list = marketService.findAllKLine(symbol, from, to, period);
        JSONArray array = new JSONArray();

        boolean startFlag = false;
        KLine temKline = null;
        BigDecimal lastPrice = null;

        // Get latest price
        CoinProcessor processor = coinProcessorFactory.getProcessor(symbol);
        if (processor != null) {
            CoinThumb thumb = processor.getThumb();
            if (thumb != null) {
                lastPrice = thumb.getClose();
            }
        }

        for (int i = 0; i < list.size(); i++) {
            KLine item = list.get(i);
            // This section filters out K-lines starting with open/close = 0
            if (!startFlag && item.getOpenPrice().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            } else {
                startFlag = true;
            }

            // If 0 appears in the middle section, adjust values
            if (item.getOpenPrice().compareTo(BigDecimal.ZERO) == 0) {
                item.setOpenPrice(temKline.getClosePrice());
                item.setClosePrice(temKline.getClosePrice());
                item.setHighestPrice(temKline.getClosePrice());
                item.setLowestPrice(temKline.getClosePrice());
            }

            // Last K-line
            if (i == list.size() - 1) {
                long currentTime = System.currentTimeMillis();
                long resolutionDuration = getResolutionDurationInMillis(period);
                if (to >= currentTime - resolutionDuration) {
                    Date date = new Date();
                    KLineTemp temp = new KLineTemp();

                    String cacheKey = SysConstant.MARKET_HISTORY_LAST_KLINE_RESOLUTION + symbol + period + item.getTime();
                    ValueOperations valueOperations = redisTemplate.opsForValue();
                    String strLastKline = (String)valueOperations.get(cacheKey);
                    if (strLastKline == null) {
                        temp.setLastSyncTime(temKline.getTime());
                        temp.setPeriod(item.getPeriod());
                    } else {
                        KLineTemp lastKline = JSONObject.parseObject(strLastKline, KLineTemp.class);
                        temp.setLastSyncTime(lastKline.getLastSyncTime());

                        item.setOpenPrice(lastKline.getOpenPrice());
                        item.setHighestPrice(lastKline.getHighestPrice());
                        item.setLowestPrice(lastKline.getLowestPrice());
                        item.setClosePrice(lastKline.getClosePrice());
                        item.setCount(lastKline.getCount());
                        item.setVolume(lastKline.getVolume());
                        item.setTurnover(lastKline.getTurnover());
                    }

                    List<ExchangeTrade> exchangeTrades = marketService.findTradeByTimeRange(symbol, temp.getLastSyncTime(), date.getTime());
                    if (exchangeTrades != null && exchangeTrades.size() > 0) {
                        for (List<ExchangeTrade> chunks : Convert.chunkArrayList(exchangeTrades, 100)) {
                            for (ExchangeTrade exchangeTrade : chunks) {
                                processTrade(item, exchangeTrade);
                            }
                        }
                        temp.setTime(item.getTime());
                        temp.setOpenPrice(item.getOpenPrice());
                        temp.setHighestPrice(item.getHighestPrice());
                        temp.setLowestPrice(item.getLowestPrice());
                        temp.setClosePrice(item.getClosePrice());
                        temp.setCount(item.getCount());
                        temp.setVolume(item.getVolume());
                        temp.setTurnover(item.getTurnover());
                        temp.setLastSyncTime(date.getTime());
                        valueOperations.set(cacheKey, JSONObject.toJSONString(temp), (resolutionDuration / 1000) + 10, TimeUnit.SECONDS);
                    }
                }
            }
            // Set latest price
            if (i == list.size() - 1 && lastPrice != null) {
                item.setClosePrice(lastPrice);
            }

            JSONArray group = new JSONArray();
            group.add(0, item.getTime());
            group.add(1, item.getOpenPrice());
            group.add(2, item.getHighestPrice());
            group.add(3, item.getLowestPrice());
            group.add(4, item.getClosePrice());
            group.add(5, item.getVolume());
            array.add(group);

            temKline = item;
        }
        return array;
    }

    /**
     * Convert resolution period to milliseconds
     * Supports: 1min, 5min, 15min, 30min, 1hour, 4hour, 1day, 1week, 1month
     *
     * @param period Resolution period (e.g., "1min", "5min", "1hour", "1day", "1week", "1month")
     * @return Duration in milliseconds
     */
    private long getResolutionDurationInMillis(String period) {
        if (period.endsWith("min")) {
            int minutes = Integer.parseInt(period.replace("min", ""));
            return minutes * 60 * 1000L;
        } else if (period.endsWith("hour")) {
            int hours = Integer.parseInt(period.replace("hour", ""));
            return hours * 60 * 60 * 1000L;
        } else if (period.endsWith("day")) {
            int days = Integer.parseInt(period.replace("day", ""));
            return days * 24 * 60 * 60 * 1000L;
        } else if (period.endsWith("week")) {
            int weeks = Integer.parseInt(period.replace("week", ""));
            return weeks * 7 * 24 * 60 * 60 * 1000L;
        } else if (period.endsWith("month")) {
            int months = Integer.parseInt(period.replace("month", ""));
            // Approximate month as 30 days
            return months * 30L * 24 * 60 * 60 * 1000L;
        }
        // Default to 1 minute if unknown format
        return 60 * 1000L;
    }

    public void processTrade(KLine kLine, ExchangeTrade exchangeTrade) {
        if (kLine.getClosePrice().compareTo(BigDecimal.ZERO) == 0) {
            // First time set K-line value
            kLine.setOpenPrice(exchangeTrade.getPrice());
            kLine.setHighestPrice(exchangeTrade.getPrice());
            kLine.setLowestPrice(exchangeTrade.getPrice());
            kLine.setClosePrice(exchangeTrade.getPrice());
        } else {
            kLine.setHighestPrice(exchangeTrade.getPrice().max(kLine.getHighestPrice()));
            kLine.setLowestPrice(exchangeTrade.getPrice().min(kLine.getLowestPrice()));
            kLine.setClosePrice(exchangeTrade.getPrice());
        }
        kLine.setCount(kLine.getCount() + 1);
        kLine.setVolume(kLine.getVolume().add(exchangeTrade.getAmount()));
        BigDecimal turnover = exchangeTrade.getPrice().multiply(exchangeTrade.getAmount());
        kLine.setTurnover(kLine.getTurnover().add(turnover));
    }

    public static void main(String[] args) {
        // System.out.println(new Date().getTime());
    }

    /**
     * Query latest trades
     *
     * @param symbol Trading pair symbol
     * @param size   Maximum number of records
     * @return
     */
    @ApiOperation(value = "Query latest trades")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
            @ApiImplicitParam(name = "size", value = "Maximum number of records"),
    })
    @RequestMapping("latest-trade")
    public List<ExchangeTrade> latestTrade(String symbol, int size) {
        return exchangeTradeFeign.findLatest(symbol, size);
    }

    @ApiOperation(value = "Get trade plate details")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
    })
    @RequestMapping("exchange-plate")
    public Map<String, List<TradePlateItem>> findTradePlate(String symbol) {
        // Remote RPC service URL, suffix is coin unit
        Map<String, List<TradePlateItem>> stringListMap = monitorFeign.traderPlate(symbol);
        return stringListMap;
    }

    @ApiOperation(value = "Get trade plate details (mini)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
    })
    @RequestMapping("exchange-plate-mini")
    public Map<String, JSONObject> findTradePlateMini(String symbol) {
        // Remote RPC service URL, suffix is coin unit
        Map<String, JSONObject> traderPlateMini = monitorFeign.traderPlateMini(symbol);
        return traderPlateMini;
    }

    @ApiOperation(value = "Get trade plate details (full)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
    })
    @RequestMapping("exchange-plate-full")
    public Map<String, JSONObject> findTradePlateFull(String symbol) {
        // Remote RPC service URL, suffix is coin unit
        Map<String, JSONObject> traderPlateFull = monitorFeign.traderPlateFull(symbol);
        return traderPlateFull;
    }

    @ApiOperation(value = "BTC/USDT trend line")
    @GetMapping("add_dcitionary/{bond}/{value}")
    public MessageResult addDcitionaryForAdmin(@PathVariable("bond") String bond, @PathVariable("value") String value) {
        String key = SysConstant.DATA_DICTIONARY_BOUND_KEY + bond;
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object bondvalue = valueOperations.get(key);

        if (bondvalue == null) {
            valueOperations.set(key, value);
        } else {
            valueOperations.getOperations().delete(key);
            valueOperations.set(key, value);
        }

        MessageResult re = new MessageResult();
        re.setCode(0);
        re.setMessage("success");
        return re;
    }

    /**
     * BTC/USDT trend line
     *
     * @return
     */
    @ApiOperation(value = "BTC/USDT trend line")
    @RequestMapping("/btc/trend")
    public MessageResult btcTrend() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        long nowTime = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR_OF_DAY, -24);

        JSONArray array = new JSONArray();
        long firstTimeOfToday = calendar.getTimeInMillis();

        List<KLine> lines = marketService.findAllKLine("BTC/USDT", firstTimeOfToday, nowTime, "5min");
        JSONArray trend = new JSONArray();
        for (KLine line : lines) {
            trend.add(line.getClosePrice());
        }

        MessageResult re = new MessageResult();
        re.setCode(0);
        re.setData(trend);
        re.setMessage("success");
        return re;
    }
}