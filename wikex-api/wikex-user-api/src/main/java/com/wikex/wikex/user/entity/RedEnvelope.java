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
 * Red Envelope
 * </p>
 *
 * @author markchao
 * @since 2023-06-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RedEnvelope implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String bgImage;

    private Integer count;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private String detail;

    private String envelopeNo;

    private Integer expiredHours;

    private Integer invite;

    private String logoImage;

    /**
     * Maximum random receive amount
     */
    private BigDecimal maxRand;

    private Long memberId;

    private String name;

    private Integer plateform;

    /**
     * Total received amount
     */
    private BigDecimal receiveAmount;

    private Integer receiveCount;

    private Integer state;

    /**
     * Total red envelope amount
     */
    private BigDecimal totalAmount;

    private Integer type;

    private String unit;

    private String inviteUser;

    private String inviteUserAvatar;

}
