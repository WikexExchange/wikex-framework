package com.wikex.wikex.user.feign;

import com.wikex.wikex.user.entity.Currency;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-user",contextId = "currencyFeign")
public interface CurrencyFeign {


    @GetMapping("/currencyFeign/findAllCurrency")
    List<Currency> findAllCurrency();

    @GetMapping("/currencyFeign/findCurrencyBySymbol")
    Currency findCurrencyBySymbol(@RequestParam("symbol") String symbol);


}
