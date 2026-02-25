package com.wikex.wikex.rpc.feign;

import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient("rpc-service")
public interface RpcFeign {
    @GetMapping("//wikex-rpc-$chain/rpc/{chain}/address/{account}")
    MessageResult getNewAddress(@PathVariable("chain") String chain, @PathVariable("account") String account);

    @GetMapping("//wikex-rpc-$chain/rpc/{chain}/balance")
    MessageResult walletBalance(@PathVariable("chain") String chain,
            @RequestParam(name = "coinName", required = false, defaultValue = "TRX") String coinName);

    @GetMapping("//wikex-rpc-$chain/rpc/{chain}/height")
    MessageResult getHeight(@PathVariable("chain") String chain);

    @GetMapping("//wikex-rpc-$chain/rpc/{chain}/balance/{address}")
    MessageResult addressBalance(@PathVariable("chain") String chain, @PathVariable("address") String address);

    @GetMapping("//wikex-rpc-$chain/rpc/{chain}/setPassword")
    MessageResult setPassword(@PathVariable("chain") String chain, @RequestParam(name = "password") String password)
            throws Exception;

    @GetMapping("//wikex-rpc-$chain/rpc/{chain}/updateContract")
    MessageResult updateContract(@PathVariable("chain") String chain, @RequestParam(name = "password") String password)
            throws Exception;

    @GetMapping("//wikex-rpc-$chain/rpc/{chain}/transfer-from-address")
    MessageResult transferFromAddress(@PathVariable("chain") String chain,
            @RequestParam(name = "fromAddress") String fromAddress,
            @RequestParam(name = "address") String address,
            @RequestParam(name = "amount") BigDecimal amount,
            @RequestParam(name = "fee") BigDecimal fee);

    @GetMapping("//wikex-rpc-$chain/rpc/{chain}/transfer")
    MessageResult transfer(@PathVariable("chain") String chain,
            @RequestParam(name = "password") String password,
            @RequestParam(name = "address") String address,
            @RequestParam(name = "coinName") String coinName,
            @RequestParam(name = "amount") BigDecimal amount,
            @RequestParam(name = "fee") BigDecimal fee);

    @GetMapping("//wikex-rpc-$chain/rpc/{chain}/transferAll")
    MessageResult transferAll(@PathVariable("chain") String chain,
            @RequestParam(name = "address") String address,
            @RequestParam(name = "coinName") String coinName,
            @RequestParam(name = "password") String password);

    @GetMapping("//wikex-rpc-$chain/rpc/{chain}/withdraw")
    MessageResult withdraw(@PathVariable("chain") String chain,
            @RequestParam(name = "address") String address,
            @RequestParam(name = "amount") BigDecimal amount,
            @RequestParam(name = "sync", required = false, defaultValue = "true") Boolean sync,
            @RequestParam(name = "coinName", required = false, defaultValue = "true") String coinName,
            @RequestParam(name = "withdrawId", required = false, defaultValue = "") String withdrawId);

    @GetMapping("//wikex-rpc-$chain/rpc/{chain}/sync-block")
    MessageResult manualSync(@PathVariable("chain") String chain, @RequestParam(name = "startBlock") Long startBlock,
            @RequestParam(name = "endBlock") Long endBlock);

    @GetMapping("//wikex-rpc-$chain/rpc/{chain}/sync-height")
    MessageResult getCurrentSyncHeight(@PathVariable("chain") String chain);

    @GetMapping("//wikex-rpc-$chain/rpc/{chain}/readBlock")
    MessageResult readBlock(@PathVariable("chain") String chain, @RequestParam(name = "blockNum") Long blockNum)
            throws Exception;

    @GetMapping("//wikex-rpc-$chain/rpc/{chain}/checkTransactionSuccessful")
    MessageResult checkTransactionSuccessful(@PathVariable("chain") String chain,
            @RequestParam(name = "txHash") String txHash) throws Exception;

}
