package com.wikex.wikex.rpc.component;

import com.wikex.wikex.rpc.util.MessageResult;
import java.math.BigDecimal;

public interface RpcController {
    /**
     * Get current block height
     * @return
     */
    MessageResult blockHeight();

    /**
     * Get a new address for the user
     * @param uuid unique user ID
     * @return
     */
    MessageResult getNewAddress(String uuid);

    /**
     * Withdraw
     * @param toAddress recipient address
     * @param amount withdrawal amount
     * @param fee transaction fee
     * @param isSync whether to execute synchronously
     * @param withdrawId withdrawal request ID
     * @return
     */
    MessageResult withdraw(String toAddress, BigDecimal amount, BigDecimal fee, Boolean isSync, String withdrawId);

    /**
     * Transfer
     * @return
     */
    MessageResult transfer();

    /**
     * Balance
     * @return
     */
    MessageResult balance();
}
