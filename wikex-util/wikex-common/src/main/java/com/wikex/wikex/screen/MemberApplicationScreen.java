package com.wikex.wikex.screen;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.domain.Sort;

import java.util.List;

@ApiModel(value = "Member Audit Information")
@Data
public class MemberApplicationScreen extends PageParam {
    @ApiModelProperty(value = "Audit Status")
    private Integer auditStatus; // Audit status

    @ApiModelProperty(value = "ID Card Number")
    private String cardNo; // ID card number

    @ApiModelProperty(value = "Account")
    private String account;

    @ApiModelProperty(value = "Inviter ID")
    private Long inviterId;

    private List<Sort.Direction> direction;
    private List<String> property;
}
