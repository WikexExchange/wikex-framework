package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Map;

/**
 * User payment method binding record table
 */
@ApiModel(value = "User payment method binding record table")
@Data
@Entity
public class PaymentTypeRecord {

    @ApiModelProperty(value = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    /**
     * User id
     */
    @ApiModelProperty(value = "User id")
    private Long memberId;
    /**
     * Payment method id
     */
    @ApiModelProperty(value = "Payment method id")
    private Long type;

    @ApiModelProperty(value = "Field 1")
    @TableField(value = "field_1")
    private String field_1;

    @ApiModelProperty(value = "Field 2")
    @TableField(value = "field_2")
    private String field_2;

    @ApiModelProperty(value = "Field 3")
    @TableField(value = "field_3")
    private String field_3;

    @ApiModelProperty(value = "Field 4")
    @TableField(value = "field_4")
    private String field_4;

    @ApiModelProperty(value = "Field 5")
    @TableField(value = "field_5")
    private String field_5;

    @ApiModelProperty(value = "Field 6")
    @TableField(value = "field_6")
    private String field_6;

    @ApiModelProperty(value = "Field 7")
    @TableField(value = "field_7")
    private String field_7;

    @ApiModelProperty(value = "Payment method name")
    @TableField(exist = false)
    private String typeName;

    @ApiModelProperty(value = "Field types")
    @TableField(exist = false)
    private Map<String,String> fieldType;

    @ApiModelProperty(value = "Field names")
    @TableField(exist = false)
    private Map<String,String> fieldName;

    @ApiModelProperty(value = "Color")
    @TableField(exist = false)
    private String color;
}
