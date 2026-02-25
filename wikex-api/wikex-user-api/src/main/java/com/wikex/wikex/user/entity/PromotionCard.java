package com.wikex.wikex.user.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Partner promotion card
 * </p>
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PromotionCard implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Amount per single card
     */
    private BigDecimal amount;

    private String cardDesc;

    private String cardName;

    private String cardNo;

    private Integer count;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Integer exchangeCount;

    private Integer isEnabled;

    private Integer isFree;

    private Integer isLock;

    private Integer lockDays;

    private Long memberId;

    /**
     * Total amount of all cards
     */
    private BigDecimal totalAmount;

    private String coinId;

}
