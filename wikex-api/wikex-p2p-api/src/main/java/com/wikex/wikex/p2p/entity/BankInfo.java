package com.wikex.wikex.p2p.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Bank Account Information
 * Represents details of a bank account used for payment or receipt in P2P transactions.
 * 
 * Author: Hevin  
 * Date: 2020-01-16
 */
@ApiModel(value = "Bank Account Information")
@Data
public class BankInfo implements Serializable {
    private static final long serialVersionUID = -5602439827000928628L;
    /**
     * Name of the bank.
     */
    @ApiModelProperty(value = "Bank name")
    private String bank;
    /**
     * Branch of the bank.
     */
    @ApiModelProperty(value = "Bank branch")
    private String branch;
    /**
     * Bank account card number.
     */
    @ApiModelProperty(value = "Bank card number")
    private String cardNo;
}
