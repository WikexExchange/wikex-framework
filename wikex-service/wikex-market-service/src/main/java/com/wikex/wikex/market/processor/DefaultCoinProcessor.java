package com.wikex.wikex.market.processor;

import com.wikex.wikex.market.component.CoinExchangeRate;
import com.wikex.wikex.market.handler.MarketHandler;
import com.wikex.wikex.market.service.MarketService;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.ExchangeTrade;
import com.wikex.wikex.pojo.KLine;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Default trading processor, generates 1-minute K-line information
 */
@ToString
public class DefaultCoinProcessor implements CoinProcessor {
    private Logger logger = LoggerFactory.getLogger(DefaultCoinProcessor.class);
    private String symbol;
    private String baseCoin;
    private KLine currentKLine;
    private List<MarketHandler> handlers;
    private CoinThumb coinThumb;
    private MarketService service;
    private CoinExchangeRate coinExchangeRate;
    // Whether temporarily halted
    private Boolean isHalt = true;
    // Whether to stop generating K-line
    private Boolean stopKLine = false;

    public DefaultCoinProcessor(String symbol, String baseCoin) {
        handlers = new ArrayList<>();
        createNewKLine();
        this.baseCoin = baseCoin;
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public void initializeThumb() {
        Calendar calendar = Calendar.getInstance();
        // Set seconds and milliseconds to 0
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long nowTime = calendar.getTimeInMillis();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        long firstTimeOfToday = calendar.getTimeInMillis();
        String period = "1min";
        // logger.info("initializeThumb from {} to {}", firstTimeOfToday, nowTime);
        List<KLine> lines = service.findAllKLine(this.symbol, firstTimeOfToday, nowTime, period);
        coinThumb = new CoinThumb();
        synchronized (coinThumb) {
            coinThumb.setSymbol(symbol);
            for (KLine kline : lines) {
                if (kline.getOpenPrice().compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                if (coinThumb.getOpen().compareTo(BigDecimal.ZERO) == 0) {
                    coinThumb.setOpen(kline.getOpenPrice());
                }
                if (coinThumb.getHigh().compareTo(kline.getHighestPrice()) < 0) {
                    coinThumb.setHigh(kline.getHighestPrice());
                }
                if (kline.getLowestPrice().compareTo(BigDecimal.ZERO) > 0 && coinThumb.getLow().compareTo(kline.getLowestPrice()) > 0) {
                    coinThumb.setLow(kline.getLowestPrice());
                }
                if (kline.getClosePrice().compareTo(BigDecimal.ZERO) > 0) {
                    coinThumb.setClose(kline.getClosePrice());
                }
                coinThumb.setVolume(coinThumb.getVolume().add(kline.getVolume()));
                coinThumb.setTurnover(coinThumb.getTurnover().add(kline.getTurnover()));
            }
            coinThumb.setChange(coinThumb.getClose().subtract(coinThumb.getOpen()));
            // Here the percentage change is calculated based on the lowest price instead of the opening price
            if (coinThumb.getLow().compareTo(BigDecimal.ZERO) > 0) {
                coinThumb.setChg(coinThumb.getChange().divide(coinThumb.getLow(), 4, RoundingMode.UP));
            }
        }
    }

    public void createNewKLine() {
        currentKLine = new KLine();
        synchronized (currentKLine) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            // The 1-minute K-line time should be the next full minute
            calendar.add(Calendar.MINUTE, 1);
            currentKLine.setTime(calendar.getTimeInMillis());
            currentKLine.setPeriod("1min");
            currentKLine.setCount(0);
        }
    }

    /**
     * Reset CoinThumb at 00:00:00
     */
    @Override
    public void resetThumb() {
        // logger.info("reset coinThumb");
        synchronized (coinThumb) {
            coinThumb.setOpen(BigDecimal.ZERO);
            coinThumb.setHigh(BigDecimal.ZERO);
            // Set yesterday's closing price
            coinThumb.setLastDayClose(coinThumb.getClose());
            coinThumb.setLow(BigDecimal.ZERO);
            coinThumb.setChg(BigDecimal.ZERO);
            coinThumb.setChange(BigDecimal.ZERO);
        }
    }

    @Override
    public void setExchangeRate(CoinExchangeRate coinExchangeRate) {
        this.coinExchangeRate = coinExchangeRate;
    }

    @Override
    public void update24HVolume(long time) {
        if(coinThumb!=null) {
            synchronized (coinThumb) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(time);
                calendar.add(Calendar.HOUR_OF_DAY, -24);
                long timeStart = calendar.getTimeInMillis();
                BigDecimal volume = service.findTradeVolume(this.symbol, timeStart, time);
                coinThumb.setVolume(volume.setScale(4, RoundingMode.DOWN));
            }
        }
    }

    @Override
    public void initializeUsdRate() {
        BigDecimal baseUsdRate = coinExchangeRate.getUsdRate(baseCoin);
        coinThumb.setBaseUsdRate(baseUsdRate);
        BigDecimal multiply = coinThumb.getClose().multiply(coinExchangeRate.getUsdRate(baseCoin));
        coinThumb.setUsdRate(multiply);
    }

    @Override
    public void generateKLine(long time, int minute, int hour) {
        // logger.info("Generate {} minute K-line", symbol);
        long ml = System.currentTimeMillis();
        // Generate 1-minute K-line
        this.autoGenerate();
        this.generateKLine1min(1, Calendar.MINUTE, time);
        // Update 24H trading volume
        this.update24HVolume(time);
        if(minute %5 == 0) {
            this.generateKLine(5, Calendar.MINUTE, time);
        }
        if(minute %10 == 0){
            this.generateKLine(10, Calendar.MINUTE, time);
        }
        if(minute %15 == 0){
            this.generateKLine(15, Calendar.MINUTE, time);
        }
        if(minute %30 == 0){
            this.generateKLine(30, Calendar.MINUTE, time);
        }
        if(hour == 0 && minute == 0){
            this.resetThumb();
        }
        // logger.info("Time consumed " + (System.currentTimeMillis()-ml) + "ms, completed {} K-line generation", symbol);
    }

    @Override
    public void autoGenerate() {
        // DateFormat df = new SimpleDateFormat("HH:mm:ss");
        // logger.info("auto generate 1min kline in {},data={}", df.format(new Date(currentKLine.getTime())), JSON.toJSONString(currentKLine));
        if(coinThumb != null) {
            synchronized (currentKLine) {
                // If there is no trade price, store the last trade price
                if (currentKLine.getOpenPrice().compareTo(BigDecimal.ZERO) == 0) {
                    currentKLine.setOpenPrice(coinThumb.getClose());
                    currentKLine.setLowestPrice(coinThumb.getClose());
                    currentKLine.setHighestPrice(coinThumb.getClose());
                    currentKLine.setClosePrice(coinThumb.getClose());
                }
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                currentKLine.setTime(calendar.getTimeInMillis());
                createNewKLine();
            }
        }
    }

    @Override
    public void generateKLine1min(int range, int field, long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long endTick = calendar.getTimeInMillis();
        String endTime = df.format(calendar.getTime());
        // Move backward by 'range' time units
        calendar.add(field, -range);
        String fromTime = df.format(calendar.getTime());
        long startTick = calendar.getTimeInMillis();

        KLine kLine = new KLine();
        kLine.setTime(endTick);
        String rangeUnit = "";
        if (field == Calendar.MINUTE) {
            rangeUnit = "min";
        } else if (field == Calendar.HOUR_OF_DAY) {
            rangeUnit = "hour";
        } else if (field == Calendar.DAY_OF_WEEK) {
            rangeUnit = "week";
        } else if (field == Calendar.DAY_OF_YEAR) {
            rangeUnit = "day";
        } else if (field == Calendar.DAY_OF_MONTH) {
            rangeUnit = "month";
        }
        kLine.setPeriod(range + rangeUnit);

        List<ExchangeTrade> exchangeTrades = null;
        // For minute and daily lines, directly query the order trade details within the time period
        if(field == Calendar.MINUTE || field == Calendar.HOUR_OF_DAY || field == Calendar.DAY_OF_YEAR){
            exchangeTrades = service.findTradeByTimeRange(this.symbol, startTick, endTick);
            // Process K-line information
            for (ExchangeTrade exchangeTrade : exchangeTrades) {
                processTrade(kLine, exchangeTrade);
            }
        }else{ // Processing method for weekly and monthly lines
            processKline(kLine, startTick, endTick, field);
        }

        // If the opening price is 0, set it to the previous closing price
        if(kLine.getOpenPrice().compareTo(BigDecimal.ZERO) == 0) {
            kLine.setOpenPrice(coinThumb.getClose());
            kLine.setClosePrice(coinThumb.getClose());
            kLine.setLowestPrice(coinThumb.getClose());
            kLine.setHighestPrice(coinThumb.getClose());
        }
        // logger.info("{} Kline generate " + range + rangeUnit + " kline in {},data={}",this.symbol, df.format(new Date(kLine.getTime())), JSON.toJSONString(kLine));
        handleKLineStorage(kLine);
    }

    @Override
    public void setIsHalt(boolean status) {
        this.isHalt = status;
    }

    @Override
    public void process(List<ExchangeTrade> trades) {
        if (!isHalt) {
            if (trades == null || trades.size() == 0) {
                return;
            }
            synchronized (currentKLine) {
                for (ExchangeTrade exchangeTrade : trades) {
                    // Process K-line
                    processTrade(currentKLine, exchangeTrade);
                }
            }

            for (ExchangeTrade exchangeTrade : trades) {
                // Process today's summary information
                handleThumb(exchangeTrade);
                // Store and push trade information
                handleTradeStorage(exchangeTrade);
            }
        }
    }

    public void processTrade(KLine kLine, ExchangeTrade exchangeTrade) {
        if (kLine.getClosePrice().compareTo(BigDecimal.ZERO) == 0) {
            // Set K-line values for the first time
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

    public void handleTradeStorage(ExchangeTrade exchangeTrade) {
        for (MarketHandler storage : handlers) {
            storage.handleTrade(symbol, exchangeTrade, coinThumb);
        }
    }

    public void handleKLineStorage(KLine kLine) {
        for (MarketHandler storage : handlers) {
            storage.handleKLine(symbol, kLine);
        }
    }

    public void handleThumb(ExchangeTrade exchangeTrade) {
        synchronized (coinThumb) {
            if (coinThumb.getOpen().compareTo(BigDecimal.ZERO) == 0) {
                // The first trade is recorded as the opening price
                coinThumb.setOpen(exchangeTrade.getPrice());
            }
            coinThumb.setHigh(exchangeTrade.getPrice().max(coinThumb.getHigh()));
            if (coinThumb.getLow().compareTo(BigDecimal.ZERO) == 0) {
                coinThumb.setLow(exchangeTrade.getPrice());
            } else {
                coinThumb.setLow(exchangeTrade.getPrice().min(coinThumb.getLow()));
            }
            coinThumb.setClose(exchangeTrade.getPrice());
            coinThumb.setVolume(coinThumb.getVolume().add(exchangeTrade.getAmount()).setScale(4, RoundingMode.UP));
            BigDecimal turnover = exchangeTrade.getPrice().multiply(exchangeTrade.getAmount()).setScale(4, RoundingMode.UP);
            coinThumb.setTurnover(coinThumb.getTurnover().add(turnover));
            BigDecimal change = coinThumb.getClose().subtract(coinThumb.getOpen());
            coinThumb.setChange(change);
            if (coinThumb.getLow().compareTo(BigDecimal.ZERO) > 0) {
                coinThumb.setChg(change.divide(coinThumb.getLow(), 4, BigDecimal.ROUND_UP));
            }
            if ("USDT".equalsIgnoreCase(baseCoin)) {
                // logger.info("setUsdRate", exchangeTrade.getPrice());
                coinThumb.setUsdRate(exchangeTrade.getPrice());
            }
            coinThumb.setBaseUsdRate(coinExchangeRate.getUsdRate(baseCoin));
            coinThumb.setUsdRate(exchangeTrade.getPrice().multiply(coinExchangeRate.getUsdRate(baseCoin)));
            // logger.info("setUsdRate", exchangeTrade.getPrice().multiply(coinExchangeRate.getUsdRate(baseCoin)));
            // logger.info("thumb = {}", coinThumb);
        }
    }

    @Override
    public void addHandler(MarketHandler storage) {
        handlers.add(storage);
    }

    @Override
    public CoinThumb getThumb() {
        return coinThumb;
    }

    @Override
    public void setMarketService(MarketService service) {
        this.service = service;
    }

    @Override
    public void generateKLine(int range, int field, long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        // DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long endTick = calendar.getTimeInMillis();
        // String endTime = df.format(calendar.getTime());
        // Move backward by 'range' time units
        calendar.add(field, -range);
        // String fromTime = df.format(calendar.getTime());
        long startTick = calendar.getTimeInMillis();
        // logger.debug("time range from {} to {}", fromTime, endTime);

        KLine kLine = new KLine();
        kLine.setTime(endTick);
        String rangeUnit = "";
        if (field == Calendar.MINUTE) {
            rangeUnit = "min";
        } else if (field == Calendar.HOUR_OF_DAY) {
            rangeUnit = "hour";
        } else if (field == Calendar.WEEK_OF_MONTH) {
            rangeUnit = "week";
        } else if (field == Calendar.DAY_OF_YEAR) {
            rangeUnit = "day";
        } else if (field == Calendar.MONTH) {
            rangeUnit = "month";
        }
        kLine.setPeriod(range + rangeUnit);

        List<ExchangeTrade> exchangeTrades = null;
        // For minute and daily lines, directly query the order trade details within the time period
        if(field == Calendar.MINUTE || field == Calendar.HOUR_OF_DAY || field == Calendar.DAY_OF_YEAR){
            exchangeTrades = service.findTradeByTimeRange(this.symbol, startTick, endTick);
            // Process K-line information
            for (ExchangeTrade exchangeTrade : exchangeTrades) {
                processTrade(kLine, exchangeTrade);
            }
        }else{ // Processing method for weekly and monthly lines
            processKline(kLine, startTick, endTick, field);
        }

        // If the opening price is 0, set it to the previous closing price
        if(kLine.getOpenPrice().compareTo(BigDecimal.ZERO) == 0) {
            kLine.setOpenPrice(coinThumb.getClose());
            kLine.setClosePrice(coinThumb.getClose());
            kLine.setLowestPrice(coinThumb.getClose());
            kLine.setHighestPrice(coinThumb.getClose());
        }
        // logger.info("generate " + range + rangeUnit + " kline in {},data={}", df.format(new Date(kLine.getTime())), JSON.toJSONString(kLine));
        handleKLineStorage(kLine);
    }

    // More efficient method for processing weekly and monthly K-lines
    public void processKline(KLine kline, long fromTime, long endTime, int field){
        // Query daily K-lines of the past period (e.g., 7 days)
        List<KLine> lines = service.findAllKLine(symbol, fromTime, endTime,"1day");
        if(lines.size() > 0) {
            kline.setOpenPrice(lines.get(0).getOpenPrice()); // Set opening price as the first day's opening price
            kline.setLowestPrice(lines.get(0).getLowestPrice());
            for (KLine item : lines) {
                kline.setHighestPrice(kline.getHighestPrice().max(item.getHighestPrice()));
                kline.setLowestPrice(kline.getLowestPrice().min(item.getLowestPrice()));
                kline.setVolume(kline.getVolume().add(item.getVolume()));
                kline.setTurnover(kline.getTurnover().add(item.getTurnover()));
                kline.setCount(kline.getCount() + item.getCount());
            }
            kline.setClosePrice(lines.get(lines.size() - 1).getClosePrice()); // Set closing price as the last day's closing price
        }
    }

    @Override
    public KLine getKLine() {
        return currentKLine;
    }

    @Override
    public void setIsStopKLine(boolean stop) {
        this.stopKLine = stop;
    }

    @Override
    public boolean isStopKline() {
        return this.stopKLine;
    }
}
