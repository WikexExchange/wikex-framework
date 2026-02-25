package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Member API Key entity
 * </p>
 *
 * @author markchao
 * @since 2023-12-15
 */
@ApiModel(value = "Member API Key")
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberApiKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("Primary key")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("API Key")
    private String apiKey;

    @ApiModelProperty("API Name or label")
    private String apiName;

    @ApiModelProperty("Bound IP address")
    private String bindIp;

    @ApiModelProperty("Creation timestamp")
    private Date createTime;

    @ApiModelProperty("Expiration timestamp")
    private Date expireTime;

    @ApiModelProperty("Member ID")
    private Long memberId;

    @ApiModelProperty("Remarks or notes")
    private String remark;

    @ApiModelProperty("Secret key associated with API key")
    private String secretKey;

    @ApiModelProperty("Verification or status code, not persisted")
    @TableField(exist = false)
    private String code;
}
