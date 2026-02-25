package com.wikex.wikex.user.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Payment Method Entity
 */
@ApiModel(value = "Payment Method")
@Data
@Entity
public class PaymentType {

    /**
     * Primary Key ID
     */
    @ApiModelProperty(value = "id")
    @Id
    private Long id;

    /**
     * Payment method code
     */
    @ApiModelProperty(value = "Payment method code")
    private String code;

    /**
     * Payment method configuration in JSON format
     */
    @ApiModelProperty(value = "Payment method configuration (JSON)")
    private String configJson;

}
