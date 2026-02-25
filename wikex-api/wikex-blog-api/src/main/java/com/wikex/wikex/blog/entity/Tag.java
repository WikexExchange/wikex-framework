package com.wikex.wikex.blog.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Tag entity
 */
@Data
public class Tag implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    
    /**
     * Tag name
     */
    private String name;

    /**
     * Tag slug
     */
    private String slug;
}