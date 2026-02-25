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
 * Address Entity
 * </p>
 *
 * @author markchao
 * @since 2022-03-20
 */
@ApiModel(value = "Address")
@Data
@EqualsAndHashCode(callSuper = false)
public class Addressext implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * Status: 0 - unused, 1 - used
     */
    @ApiModelProperty(value = "status: 0 - unused, 1 - used")
    private Integer status;

    /**
     * Address
     */
    @ApiModelProperty(value = "address")
    private String address;

    /**
     * Protocol (only one record exists per protocol in Yonghui)
     */
    @ApiModelProperty(value = "protocol")
    private Integer coinProtocol;

    /**
     * User ID
     */
    @ApiModelProperty(value = "user id")
    private Long memberId;

    /**
     * Protocol (only one record exists per protocol in Yonghui)
     */
    @ApiModelProperty(value = "chain")
    private String chain;

}
