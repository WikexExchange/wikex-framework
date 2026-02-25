package com.wikex.wikex.p2p.vo;

import com.wikex.wikex.constant.AdvertiseControlStatus;
import com.wikex.wikex.constant.AdvertiseType;
import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.constant.PriceType;
import com.wikex.wikex.p2p.entity.Advertise;
import com.wikex.wikex.p2p.entity.OtcCoin;
import com.wikex.wikex.user.entity.Country;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;



@Builder
@Data
public class MemberAdvertiseDetail implements Serializable{

    private Long id;

    private Long coinId;

    private String coinName;

    private String coinNameCn;

    private String coinUnit;

    private Country country;

    private PriceType priceType;

    /**
     * Transaction price (real-time change)
     */
    private BigDecimal price;

    /**
     * Advertisement type 0: buy 1: sell
     */
    private AdvertiseType advertiseType;

    /**
     * Minimum single transaction amount
     */
    private BigDecimal minLimit;

    /**
     * Maximum single transaction amount
     */
    private BigDecimal maxLimit;

    /**
     * Remark
     */
    private String remark;

    /**
     * Payment deadline (minutes)
     */
    private Integer timeLimit;

    /**
     * Premium percentage
     */
    private BigDecimal premiseRate;

    /**
     * Payment methods (separated by English commas)
     */
    private String payMode;

    /**
     * Advertisement status
     */
    private AdvertiseControlStatus status ;

    private BigDecimal number;

    /**
     * Market price
     */
    private BigDecimal marketPrice;

    private BooleanEnum auto;

    private String autoword;

    public static MemberAdvertiseDetail toMemberAdvertiseDetail(Advertise advertise, OtcCoin coin,Country country){
        return MemberAdvertiseDetail.builder()
                .id(advertise.getId())
                .advertiseType(AdvertiseType.creator(advertise.getAdvertiseType()))
                .coinId(coin.getId())
                .coinName(coin.getName())
                .coinNameCn(coin.getNameCn())
                .coinUnit(coin.getUnit())
                .country(country)
                .auto(BooleanEnum.creator(advertise.getAuto()))
                .maxLimit(advertise.getMaxLimit())
                .minLimit(advertise.getMinLimit())
                .number(advertise.getNumber())
                .payMode(advertise.getPayMode())
                .premiseRate(advertise.getPremiseRate())
                .price(advertise.getPrice())
                .priceType(PriceType.creator(advertise.getPriceType()))
                .remark(advertise.getRemark())
                .status(AdvertiseControlStatus.creator(advertise.getStatus()))
                .timeLimit(advertise.getTimeLimit())
                .autoword(advertise.getAutoword())
                .build();
    }

}
