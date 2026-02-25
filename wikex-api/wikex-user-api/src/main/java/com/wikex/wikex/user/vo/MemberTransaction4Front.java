package com.wikex.wikex.user.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class MemberTransaction4Front implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Transaction record ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Address
     */
    private String address;

    /**
     * Airdrop ID
     */
    private Long airdropId;

    /**
     * Deposit amount
     */
    private BigDecimal amount;

    /**
     * Creation time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Discount fee
     */
    private String discountFee;

    /**
     * Actual fee received
     */
    private String realFee;

    /**
     * Transaction fee
     */
    private BigDecimal fee;

    /**
     * Flag
     */
    private Integer flag;

    /**
     * Member ID
     */
    private Long memberId;

    /**
     * Currency symbol
     */
    private String symbol;

    /**
     * Transaction type
     */
    private Integer type;

}
