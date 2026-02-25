package com.wikex.wikex.screen;

import com.wikex.wikex.constant.WithdrawStatus;
import lombok.Data;

@Data
public class LegalWalletWithdrawScreen {
    WithdrawStatus status;
    String username;
    String coinName;

}
