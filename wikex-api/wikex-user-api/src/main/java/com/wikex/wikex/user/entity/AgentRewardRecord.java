package com.wikex.wikex.user.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;


@Entity
@Data
@Table
public class AgentRewardRecord {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private Long memberId;
    private Long fromMemberId;
    private Long orderId;

    private Integer type; // type

    @Column(columnDefinition = "coin unit")
    private String coinUnit;
    /**
     * Commission amount
     */
    @Column(columnDefinition = "decimal(26,16) comment 'commission amount'")
    private BigDecimal num;

    @Column(columnDefinition = "creation time")
    private Long createTime;

    @Transient
    private Member member;

    @Transient
    private Member fromMember;

    @Transient
    private Coin coin;
}
