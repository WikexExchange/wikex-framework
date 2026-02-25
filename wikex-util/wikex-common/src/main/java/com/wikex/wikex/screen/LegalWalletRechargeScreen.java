package com.wikex.wikex.screen;

import com.wikex.wikex.constant.LegalWalletState;
import lombok.Data;

@Data
public class LegalWalletRechargeScreen {
    LegalWalletState status;
    String username;
    String coinName;

}
