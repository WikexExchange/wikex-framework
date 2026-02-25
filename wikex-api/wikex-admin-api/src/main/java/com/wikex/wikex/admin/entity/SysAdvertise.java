package com.wikex.wikex.admin.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.wikex.wikex.constant.CommonStatus;
import com.wikex.wikex.constant.SysAdvertiseLocation;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Carousel Image
 * </p>
 *
 * Author: markchao
 * Since: 2021-08-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysAdvertise implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private String serialNumber;

    private String author;

    /**
     * Content
     */
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * End time
     */
    private String endTime;

    /**
     * Image link URL
     */
    private String linkUrl;

    /**
     * System advertisement name
     */
    private String name;

    private String remark;

    private Integer sort;

    /**
     * Start time
     */
    private String startTime;

    /**
     * Status: 0 - normal, 1 - illegal
     */
    private Integer status;

    /**
     * System advertisement location: 
     * 0 - app homepage carousel, 
     * 1 - PC homepage carousel,  
     * 2 - PC category advertisement
     */
    private Integer sysAdvertiseLocation;

    /**
     * Advertisement image
     */
    private String url;

    /**
     * Advertisement language
     */
    private String lang;
}
