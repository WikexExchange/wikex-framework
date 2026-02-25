package com.wikex.wikex.vo;

import com.wikex.wikex.annotation.Excel;
import com.wikex.wikex.annotation.ExcelSheet;
import com.wikex.wikex.constant.AdvertiseType;
import com.wikex.wikex.constant.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ExcelSheet
public class OtcOrderVO {

    private Long id ;

    @Excel(name="Order Number")
    private String orderSn ;

    @Excel(name="Transaction Time")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime ;

    @Excel(name="Advertiser")
    private String memberName ;

    @Excel(name="Trader")
    private String customerName ;

    @Excel(name="Currency Unit")
    private String unit ;

    @Excel(name="Advertisement Type")
    private AdvertiseType advertiseType ;

    @Excel(name="Transaction Amount")
    private BigDecimal money ;

    @Excel(name="Transaction Quantity")
    private BigDecimal number ;

    @Excel(name="Fee")
    private  BigDecimal fee ;

    @Excel(name="Payment Method")
    private  String payMode ;

    @Excel(name="Order Status")
    private OrderStatus status ;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cancelTime ;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date releaseTime ;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date payTime ;
}
