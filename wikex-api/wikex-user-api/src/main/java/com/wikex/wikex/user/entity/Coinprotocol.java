package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Coin Protocol
 * </p>
 *
 * @author markchao
 * @since 2022-03-20
 */
@ApiModel(value = "Coin Protocol")
@Data
@EqualsAndHashCode(callSuper = false)
public class Coinprotocol implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "protocol")
    private Integer protocol;

    /**
     * Protocol name
     */
    @ApiModelProperty(value = "protocol name")
    private String protocolName;

    /**
     * RPC Server
     */
    @ApiModelProperty(value = "RPC Server")
    private String rpcServer;

    /**
     * RPC User
     */
    @ApiModelProperty(value = "RPC User")
    private String rpcUser;

    /**
     * RPC Password
     */
    @ApiModelProperty(value = "RPC Password")
    private String rpcPassword;

    /**
     * Browser
     */
    @ApiModelProperty(value = "browser")
    private String browser;

    /**
     * Symbol
     */
    @ApiModelProperty(value = "symbol")
    private String symbol;

    /**
     * Chain ID
     */
    @ApiModelProperty(value = "chain ID")
    private Integer chainId;

}
