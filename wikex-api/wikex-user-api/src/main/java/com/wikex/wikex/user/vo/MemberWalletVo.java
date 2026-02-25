package com.wikex.wikex.user.vo;

import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.MemberWallet;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * Member Wallet
 * </p>
 *
 * @author markchao
 * @since 2021-06-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberWalletVo extends MemberWallet implements Serializable {

    private static final long serialVersionUID = 1L;

    private Coin coin;

}
