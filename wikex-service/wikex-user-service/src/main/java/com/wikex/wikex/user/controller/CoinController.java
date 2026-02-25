package com.wikex.wikex.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.dto.CoinDTO;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.CoinInfo;
import com.wikex.wikex.user.entity.Coinext;
import com.wikex.wikex.user.service.CoinInfoService;
import com.wikex.wikex.user.service.CoinService;
import com.wikex.wikex.user.service.CoinextService;
import com.wikex.wikex.user.service.MemberWalletService;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wikex.wikex.constant.SysConstant.SESSION_MEMBER;

@Api(tags = "Coin")
@RestController
@RequestMapping("/coin")
public class CoinController extends BaseController {

    @Autowired
    private CoinService coinService;
    @Autowired
    private CoinextService coinextService;
    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private CoinInfoService coinInfoService;

    @ApiOperation(value = "Get legal coins")
    @GetMapping("legal")
    public MessageResult legal() {
        List<Coin> legalAll = coinService.findLegalAll();
        return success(legalAll);
    }

    @ApiOperation(value = "Get paginated legal coins")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "Page number"),
            @ApiImplicitParam(name = "pageSize", value = "Page size"),
    })
    @GetMapping("legal/page")
    public MessageResult findLegalCoinPage(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        IPage all = coinService.findLegalCoinPage(pageNo, pageSize);
        return success(IPage2Page(all));
    }

    @ApiOperation(value = "Get all supported coins")
    @RequestMapping("supported")
    public List<Map<String, String>> findCoins() {
        List<Coin> coins = coinService.list();
        List<Map<String, String>> result = new ArrayList<>();
        coins.forEach(coin -> {
            if (coin.getHasLegal().equals(Boolean.FALSE)) {
                Map<String, String> map = new HashMap<>();
                map.put("unit", coin.getUnit());
                map.put("name", coin.getName());
                map.put("nameCn", coin.getNameCn());
                map.put("iconUrl", coin.getIconUrl());
                map.put("withdrawFee", String.valueOf(coin.getMinTxFee()));
                map.put("enableRecharge", String.valueOf(coin.getCanRecharge()));
                map.put("minWithdrawAmount", String.valueOf(coin.getMinWithdrawAmount()));
                map.put("enableWithdraw", String.valueOf(coin.getCanWithdraw()));
                result.add(map);
            }
        });
        return result;
    }

    // Query all coins
    @ApiOperation(value = "Query all coins")
    @GetMapping("list")
    public MessageResult list() {
        List<Coin> coinList = coinService.list();
        List<CoinDTO> coinDTOS = new ArrayList<>();
        for (Coin c : coinList) {
            CoinDTO dto = new CoinDTO();
            dto.setName(c.getName());
            dto.setUnit(c.getUnit());
            coinDTOS.add(dto);
        }
        List<Coinext> coinextList = coinextService.list();
        Map<String, Object> map = new HashMap<>();
        map.put("coinList", coinDTOS);
        map.put("coinextList", coinextList);
        return success(map);
    }

    // Query coin balance
    @ApiOperation(value = "Query coin balance")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "coinName", value = "Coin name"),
    })
    @PermissionOperation
    @GetMapping("balance")
    public MessageResult balance(@RequestHeader(SESSION_MEMBER) String authMember,
            @RequestParam(value = "coinName") String coinName) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Long memberId = user.getId();
        BigDecimal balance = memberWalletService.getBalance(memberId, coinName);
        return success(balance);
    }

    // Get token supply information
    @ApiOperation(value = "Get supply token by unit")
    @GetMapping("supply/{unit}")
    public MessageResult getSupplyToken(@PathVariable("unit") String unit) {
        Coin coin = coinService.findByUnit(unit);
        if (coin == null) {
            return error(msService.getMessage("COIN_NOT_FOUND"));
        }

        CoinInfo info = coinInfoService.findByCoinId(coin.getId());
        if (info == null) {
            return error(msService.getMessage("COIN_INFO_NOT_FOUND"));
        }

        boolean needFetchFromAPI = info.getTotalSupply().compareTo(BigDecimal.ZERO) == 0
                || info.getMaxSupply().compareTo(BigDecimal.ZERO) == 0
                || info.getCirculatingSupply().compareTo(BigDecimal.ZERO) == 0;

        if (needFetchFromAPI && info.getCoingeckoId() == null) {
            return error(msService.getMessage("COINGECKO_ID_NOT_CONFIGURED"));
        }

        if (needFetchFromAPI) {
            CoinInfo fetched = coinInfoService.fetchFromCoinGecko(info.getCoingeckoId());
            if (fetched == null) {
                return error(msService.getMessage("SUPPLY_DATA_NOT_AVAILABLE"));
            }

            fetched.setId(info.getId());
            fetched.setCoinId(info.getCoinId());
            fetched.setCoingeckoId(info.getCoingeckoId());
            coinInfoService.saveOrUpdate(fetched);

            info = fetched;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("unit", unit);
        data.put("totalSupply", info.getTotalSupply());
        data.put("maxSupply", info.getMaxSupply());
        data.put("circulatingSupply", info.getCirculatingSupply());
        return success(data);
    }

    // Get full token information
    @ApiOperation(value = "Get token information by unit")
    @GetMapping("info/{unit}")
    public MessageResult getTokenInformation(@PathVariable("unit") String unit) {
        Coin coin = coinService.findByUnit(unit);
        if (coin == null) {
            return error(msService.getMessage("COIN_NOT_FOUND"));
        }

        CoinInfo info = coinInfoService.findByCoinId(coin.getId());

        Map<String, Object> sys = coinInfoService.fetchInfoToken(coin.getId());
        boolean hasData = false;
        if (sys != null) {
            Object mc = sys.get("marketCapUsd");
            Object fdv = sys.get("fdvUsd");
            Object cir = sys.get("circulatingSupply");
            Object tot = sys.get("totalSupply");
            Object max = sys.get("maxSupply");
            Object desc = sys.get("description");
            List<?> ex = (List<?>) sys.get("explore");
            List<?> off = (List<?>) sys.get("officialLinks");
            List<?> soc = (List<?>) sys.get("social");

            hasData = (mc instanceof BigDecimal && ((BigDecimal) mc).compareTo(BigDecimal.ZERO) > 0)
                    || (fdv instanceof BigDecimal && ((BigDecimal) fdv).compareTo(BigDecimal.ZERO) > 0)
                    || (cir instanceof BigDecimal && ((BigDecimal) cir).compareTo(BigDecimal.ZERO) > 0)
                    || (tot instanceof BigDecimal && ((BigDecimal) tot).compareTo(BigDecimal.ZERO) > 0)
                    || (max instanceof BigDecimal && ((BigDecimal) max).compareTo(BigDecimal.ZERO) > 0)
                    || (desc instanceof String && !((String) desc).isEmpty())
                    || (ex != null && !ex.isEmpty())
                    || (off != null && !off.isEmpty())
                    || (soc != null && !soc.isEmpty());
        }

        if (hasData) {
            sys.put("unit", unit);
            return success(sys);
        }

        if (info == null || info.getCoingeckoId() == null || info.getCoingeckoId().isEmpty()) {
            return error(msService.getMessage("COINGECKO_ID_NOT_CONFIGURED"));
        }

        Map<String, Object> data = coinInfoService.fetchFullInfo(info.getCoingeckoId());
        if (data == null) {
            return error(msService.getMessage("SUPPLY_DATA_NOT_AVAILABLE"));
        }
        data.put("unit", unit);
        return success(data);
    }
}
