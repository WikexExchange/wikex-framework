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
 * Partner promotion card order
 * </p>
 *
 * @author markchao
 * @since 2023-01-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PromotionCardOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Redemption amount
     */
    private BigDecimal amount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Integer isFree;

    private Integer isLock;

    private Integer lockDays;

    private Long memberId;

    private Integer state;

    private Long cardId;

}
