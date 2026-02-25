package com.wikex.wikex.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * Country Information Table
 * </p>
 *
 * @author markchao
 * @since 2021-06-21
 */
@ApiModel(value = "Country Information Table")
@Data
@EqualsAndHashCode(callSuper = false)
public class Country implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Chinese Name
     */
    @ApiModelProperty(value = "Chinese Name")
    private String zhName;

    /**
     * Area Code (e.g., country calling code)
     */
    @ApiModelProperty(value = "Area Code")
    private String areaCode;

    /**
     * English Name
     */
    @ApiModelProperty(value = "English Name")
    private String enName;

    /**
     * Vietnamese Name
     */
    @ApiModelProperty(value = "Vietnamese Name")
    private String viName;

    /**
     * Country flag logo URL
     */
    @ApiModelProperty(value = "Country flag logo")
    private String countryImageUrl;

    /**
     * Language
     */
    @ApiModelProperty(value = "Language")
    private String language;

    /**
     * Local currency abbreviation
     */
    @ApiModelProperty(value = "Local currency abbreviation")
    private String localCurrency;

    /**
     * Sort order
     */
    @ApiModelProperty(value = "Sort order")
    private Integer sort;

    /**
     * Name after multilingual processing
     */
    @ApiModelProperty(value = "Name after multilingual processing")
    @TableField(exist = false)
    private String name;

}
