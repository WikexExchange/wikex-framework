package com.wikex.wikex.swap.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Contract Trading Limits
 * </p>
 * 
 * @author markchao
 * @since 2024-01-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberTradeLimit implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long memberId;

    private Long contractId;

    /**
     * Close position fee
     */
    private BigDecimal closeFee;

    /**
     * Open position fee
     */
    private BigDecimal openFee;

    /**
     * Whether tradable
     */
    private Integer exchangeable;

    /**
     * Maximum quantity
     */
    private BigDecimal totalTxQty;

    /**
     * Minimum quantity
     */
    private BigDecimal singleTxQty;

    /**
     * Spread
     */
    private BigDecimal spread;

    /**
     * Spread type
     */
    private Integer spreadType;

    private Long createTime;

    private Long updateTime;

}
