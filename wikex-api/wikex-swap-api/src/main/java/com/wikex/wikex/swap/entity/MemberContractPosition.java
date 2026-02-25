package com.wikex.wikex.swap.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.wikex.wikex.constant.ContractOrderDirection;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Contract Position Order
 * </p>
 *
 * @author markchao
 * @since 2023-10-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberContractPosition implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "Direction")
    private ContractOrderDirection direction;

    /**
     * Leverage multiplier
     */
    private BigDecimal leverage;

    /**
     * Average price
     */
    private BigDecimal price;

    /**
     * Forced liquidation price
     */
    private BigDecimal forcePrice;

    /**
     * Margin (available + frozen)
     */
    private BigDecimal principalAmount;

    /**
     * Frozen margin
     */
    private BigDecimal frozenPrincipalAmount;

    /**
     * Frozen position
     */
    private BigDecimal frozenPosition;

    /**
     * Position
     */
    private BigDecimal position;

    /**
     * Position mode
     */
    private Integer pattern;

    /**
     * Contract face value
     */
    private BigDecimal shareNumber;

    /**
     * Profit and loss
     */
    private BigDecimal profit;

    private Long memberId;

    private Long contractId;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    @TableField(exist = false)
    private String symbol;

    /**
     * Initial margin at the start of the position
     */
    public BigDecimal getInitPrincipalAmount() {
        return this.position.multiply(this.shareNumber).multiply(this.price).divide(this.leverage, 8, BigDecimal.ROUND_DOWN);
    }
}
