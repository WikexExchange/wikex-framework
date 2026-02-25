package com.wikex.wikex.user.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * Member Account entity
 * 
 * @author Hevin 
 * @date 2020-01-16
 */
@ApiModel(value = "Member Account")
@Builder
@Data
public class MemberAccount {
    
    @ApiModelProperty(value = "Real Name")
    private String realName;

}
