package com.wikex.wikex.user.task;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.user.entity.Currency;
import com.wikex.wikex.user.service.CurrencyService;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.vo.CurrencyResponse;
import com.wikex.wikex.vo.CurrencyVO;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;


@Component
@Slf4j
public class CurrencyTask {

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @XxlJob("currencySyncTask")
    public void currencySyncTask() throws Exception {

        String url = "https://p2p.binance.com/bapi/asset/v1/public/asset-service/product/currency";
        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(120000);
        restTemplate.setRequestFactory(requestFactory);
        ResponseEntity<byte[]> response = restTemplate.getForEntity(url,  byte[].class);
        if (response.getStatusCodeValue() == HttpStatus.OK.value()) {
            ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
            String body = "";
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(response.getBody()))) {
                byte[] decompressedBytes = StreamUtils.copyToByteArray(gzipInputStream);
                
                body = new String(decompressedBytes);
            }
            

            CurrencyResponse currencyResponse = JSON.parseObject(body, CurrencyResponse.class);
            if (currencyResponse != null) {
                List<CurrencyVO> CurrencyVOList = currencyResponse.getData();
                if (CurrencyVOList != null && !CurrencyVOList.isEmpty()) {

                    LambdaQueryWrapper<Currency> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                    lambdaQueryWrapper.ne(Currency::getFullName, "USD");
                    List<Currency> currencyList = currencyService.list(lambdaQueryWrapper);

                    if (currencyList != null && !currencyList.isEmpty()) {
                        
                        currencyList.forEach(currency -> {
                            CurrencyVOList.stream().
                                    filter(currencyVO -> !StringUtils.isEmpty(currencyVO.getPair()) && currencyVO.getPair().startsWith(currency.getFullName())).
                                    findFirst().
                                    ifPresent(currencyByName -> {
                                        currency.setRate(currencyByName.getRate());
                                        currency.setUpdateTime(DateUtil.getCurrentDate());
                                    });
                        });
                        currencyService.saveOrUpdateBatch(currencyList);
                        redisTemplate.delete(SysConstant.CURRENCY);
                        opsForValue.set(SysConstant.CURRENCY, JSON.toJSONString(currencyService.list()), SysConstant.CURRENCY_HALF_HOUR, TimeUnit.SECONDS);
                    }
                } else {
                    //TODO 
                    
                }
            }
        } else {
            //TODO 
            
        }
    }
}
