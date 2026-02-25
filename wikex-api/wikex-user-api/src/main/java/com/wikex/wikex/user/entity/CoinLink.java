package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@ApiModel(value = "Coin Link")
@Data
@EqualsAndHashCode(callSuper = false)
public class CoinLink implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "coin id")
    private Long coinId;

    @ApiModelProperty(value = "type: explorer/official/social")
    private String type;

    @ApiModelProperty(value = "name: Website/Twitter/Reddit/Whitepaper ...")
    private String name;

    @ApiModelProperty(value = "url")
    private String url;
}
