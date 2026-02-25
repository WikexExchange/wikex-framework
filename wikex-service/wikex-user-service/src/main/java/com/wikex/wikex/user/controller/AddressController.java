package com.wikex.wikex.user.controller;

import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.MemberLevelEnum;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.rpc.feign.RpcFeign;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.udun.feign.WikexWalletFeign;
import com.wikex.wikex.user.entity.Addressext;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.Coinprotocol;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.WalletCreate;
import com.wikex.wikex.user.service.AddressextService;
import com.wikex.wikex.user.service.CoinService;
import com.wikex.wikex.user.service.CoinprotocolService;
import com.wikex.wikex.user.service.MemberService;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import feign.FeignException;

@Api(tags = "Address")
@Slf4j
@RestController
@RequestMapping("/address")
public class AddressController extends BaseController {

    @Autowired
    private AddressextService addressextService;

    @Autowired
    private CoinService coinService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private CoinprotocolService coinprotocolService;

    @Autowired
    private LocaleMessageSourceService messageSource;

    @Autowired
    private RpcFeign rpcFeign;

    @Autowired
    private WikexWalletFeign wikexWalletFeign;

    @Value("${wallet.type}")
    private String walletType;

    @Value("${wikex.wallet.api-key}")
    private String wikexWalletApiKey;

    @Value("${wikex.deposit.enabled}")
    private Boolean isEnable;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private LocaleMessageSourceService msService;

    @ApiOperation(value = "Create Wallet")
    @PermissionOperation
    @PostMapping("/create")
    public MessageResult createWallet(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestBody WalletCreate request) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Long memberId = user.getId();

        Member member = memberService.getById(memberId);
        if (member == null) {
            return error(messageSource.getMessage("USER_NOT_FOUND"));
        }
        if (!isEnable) {
            return error(messageSource.getMessage("DEPOSIT_DISABLED"));
        }
        // Validate chain
        String chain = request.getChain().toUpperCase();
        if (!StringUtils.hasText(request.getChain()) || !chain.matches("^(EVM|BTC|SOL|SUI)$")) {
            return error(messageSource.getMessage("INVALID_CHAIN_VALUE"));
        }

        // Check wallet limit for BTC, SOL, SUI (max 1 wallet each)
        if (chain.equals("EVM") || chain.equals("BTC") || chain.equals("SOL") || chain.equals("SUI")) {
            Addressext existingWallet = addressextService.findByMemberIdAndChain(memberId, chain);
            if (existingWallet != null) {
                return error(messageSource.getMessage("WALLET_ALREADY_EXISTS", new Object[] { chain }));
            }
        }

        request.setChain(chain);

        // if (chain.equals("EVM")) {
        // long evmWalletCount = addressextService.countByMemberIdAndChain(memberId,
        // chain);
        // if (evmWalletCount >= 10) {
        // return error(messageSource.getMessage("EVM_WALLET_LIMIT"));
        // }
        // } else {
        // Addressext existingWallet =
        // addressextService.findByMemberIdAndChain(memberId, chain);
        // if (existingWallet != null) {
        // return error(messageSource.getMessage("WALLET_ALREADY_EXISTS", new Object[] {
        // chain }));
        // }
        // }

        try {
            String externalUserId = String.valueOf(memberId);
            String idemKey = UUID.randomUUID().toString();

            log.info("Creating wallet: memberId={}, chain={}, idemKey={}", memberId, request.getChain(), idemKey);

            Map<String, Object> walletRequest = new HashMap<>();
            walletRequest.put("externalUserId", externalUserId);
            walletRequest.put("blockchain", request.getChain());
            walletRequest.put("idemKey", idemKey);

            Map<String, Object> walletResponse = wikexWalletFeign.createWallet(wikexWalletApiKey, walletRequest);

            if (walletResponse == null || walletResponse.get("address") == null) {
                return error(messageSource.getMessage("CREATE_WALLET_FAILED"));
            }

            String address = (String) walletResponse.get("address");

            Addressext existingAddress = addressextService.findByAddress(address);
            if (existingAddress != null) {
                // log.info("Wallet already exists: address={}", address);
                return success(existingAddress);
            }

            Addressext addressext = new Addressext();
            addressext.setMemberId(memberId.longValue());
            addressext.setAddress(address);
            addressext.setStatus(1);
            addressext.setCoinProtocol(0);
            addressext.setChain(request.getChain());
            addressextService.saveAndFlush(addressext);
            return success(addressext);

        } catch (FeignException.BadRequest ex) {
            return error(ex.contentUTF8());
        } catch (FeignException ex) {
            return error(messageSource.getMessage("CREATE_WALLET_FAILED"));
        } catch (Exception e) {
            return error(messageSource.getMessage("SYSTEM_OPT_FAIL"));
        }
    }

    @ApiOperation(value = "List address by member")
    @PermissionOperation
    @GetMapping("/list")
    public MessageResult listAddressByMember(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Integer memberId = (int) member.getId();
        if (!isEnable) {
            return error(messageSource.getMessage("DEPOSIT_DISABLED"));
        }
        List<Addressext> addresses = addressextService.listByMemberId(memberId);
        MessageResult messageResult = MessageResult.success();
        messageResult.setData(addresses);
        messageResult.setTotalPage("1");
        messageResult.setTotalElement(String.valueOf(addresses.size()));
        return messageResult;
    }

    @ApiOperation(value = "List coins and networks (from wallet)")
    @PermissionOperation
    @GetMapping("/coin-networks")
    public MessageResult listCoinsAndNetworks(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberService.getById(user.getId());
        Assert.notNull(member, messageSource.getMessage("USER_NOT_FOUND"));
        try {
            // call api wikex wallet
            List<Map<String, Object>> wikexWallet = wikexWalletFeign.getDepositSupport(wikexWalletApiKey);
            if (wikexWallet == null) {
                wikexWallet = Collections.emptyList();
            }
            // Map logo from Coin table
            for (Map<String, Object> asset : wikexWallet) {
                Object assetSymObj = asset.get("assetSymbol");
                if (assetSymObj != null) {
                    String assetSym = assetSymObj.toString();
                    Coin coin = coinService.findByName(assetSym);
                    String iconUrl = null;
                    if (coin != null && StringUtils.hasText(coin.getIconUrl())) {
                        iconUrl = coin.getIconUrl();
                    } else {
                        iconUrl = "https://wikex-exchange.sgp1.digitaloceanspaces.com/18d9a42f-d353-4093-bed1-aa417a8cf321.png";
                    }
                    if (iconUrl != null) {
                        asset.put("iconUrl", iconUrl);
                    }
                }
                Object networksObj = asset.get("networks");
                if (networksObj instanceof List) {
                    List<Map<String, Object>> networks = (List<Map<String, Object>>) networksObj;

                    for (Map<String, Object> network : networks) {
                        Object tokenObj = network.get("token");
                        if (tokenObj instanceof Map) {
                            Map<String, Object> token = (Map<String, Object>) tokenObj;

                            Object depositMinObj = token.get("depositMin");
                            if (depositMinObj != null) {
                                try {
                                    BigDecimal depositMin = new BigDecimal(depositMinObj.toString())
                                            .stripTrailingZeros();
                                    token.put("depositMin", depositMin);

                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                }

            }

            MessageResult mr = MessageResult.success();
            mr.setData(wikexWallet);
            mr.setTotalPage("1");
            mr.setTotalElement(String.valueOf(wikexWallet.size()));
            return mr;
        } catch (FeignException.BadRequest ex) {
            return error(ex.contentUTF8());
        } catch (FeignException ex) {
            return error(messageSource.getMessage("WALLET_SERVICE_UNAVAILABLE"));
            // return error(ex.status() + " - " + ex.contentUTF8());
        } catch (Exception e) {
            return error(messageSource.getMessage("SYSTEM_OPT_FAIL"));
        }
    }

}
