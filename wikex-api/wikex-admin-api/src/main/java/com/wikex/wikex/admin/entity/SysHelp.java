package com.wikex.wikex.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;

import com.wikex.wikex.constant.CommonStatus;
import com.wikex.wikex.constant.SysHelpClassification;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * System Help
 * </p>
 *
 * Author: markchao
 * Since: 2021-08-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysHelp implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String author;

    private String content;

    /**
     * Creation time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Image URL
     */
    private String imgUrl;

    /**
     * Whether pinned (0 - pinned, 1 - not pinned [default])
     */
    private String isTop;

    private Integer sort;

    /**
     * Status: 0 - normal, 1 - illegal
     */
    private Integer status;

    /**
     * Classification
     */
    private Integer sysHelpClassification;

    /**
     * Help title
     */
    private String title;

    /**
     * Article language
     */
    private String lang;
}
