package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wikex.wikex.constant.WalletType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * Wallet transaction record
 * </p>
 *
 * @author markchao
 * @since 2021-06-21
 */
@ApiModel(value = "Wallet transaction record")
@Data
@EqualsAndHashCode(callSuper = false)
public class WalletTransRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Transfer amount
     */
    @ApiModelProperty(value = "Transfer amount")
    private BigDecimal amount;

    /**
     * Creation time
     */
    @ApiModelProperty(value = "Creation time")
    private LocalDateTime createTime;

    /**
     * Member id
     */
    @ApiModelProperty(value = "Member id")
    private Long memberId;

    /**
     * Transfer from
     */
    @ApiModelProperty(value = "Transfer from")
    private WalletType source;

    /**
     * Transfer to
     */
    @ApiModelProperty(value = "Transfer to")
    private WalletType target;

    @ApiModelProperty(value = "Coin unit")
    private String unit;

}
