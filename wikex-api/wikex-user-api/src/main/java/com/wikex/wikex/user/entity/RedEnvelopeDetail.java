package com.wikex.wikex.user.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Red Envelope Detail
 * </p>
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RedEnvelopeDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Received amount
     */
    private BigDecimal amount;

    private Integer cate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Long envelopeId;

    private Long memberId;

    private String userIdentify;

    @TableField(exist = false)
    private String promotionCode;

}
