package com.wikex.wikex.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.wikex.wikex.constant.AnnouncementClassification;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Announcement Table
 * </p>
 *
 * Author: markchao
 * Since: 2021-08-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Announcement implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Content
     */
    private String content;

    /**
     * Creation time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Image path
     */
    private String imgUrl;

    /**
     * Display status: 0 - hidden, 1 - visible
     */
    private Boolean isShow;

    /**
     * Top status: 0 - pinned, 1 - not pinned (default)
     */
    private String isTop;

    /**
     * Sort order
     */
    private Integer sort;

    /**
     * Title
     */
    private String title;

    /**
     * Language
     */
    private String lang;

    /**
     * Classification
     */
    private AnnouncementClassification announcementClassification;
}
