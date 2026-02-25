package com.wikex.wikex.blog.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Blog entity
 */
@Data
public class Blog implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    /**
     * Author ID
     */
    private Long userId;

    /**
     * Blog title
     */
    private String title;

    /**
     * Short title
     */
    private String shortTitle;

    /**
     * Blog content
     */
    private String content;
    
    /**
     * Image URL
     */
    private String imgURL;

    /**
     * Blog tags
     */
    private List<Tag> tags;

    /**
     * View count
     */
    private Integer viewCount;

    /**
     * Like count
     */
    private Integer likeCount;

    /**
     * Comment count
     */
    private Integer commentCount;

    /**
     * Status: 0-draft, 1-published, 2-deleted
     */
    private Integer status;

    /**
     * Create time
     */
    private Date createTime;

    /**
     * Update time
     */
    private Date updateTime;
}
