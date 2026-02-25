package com.wikex.wikex.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Data Dictionary Table
 * </p>
 *
 * Author: markchao
 * Since: 2021-06-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DataDictionary implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Key
     */
    private String bond;

    /**
     * Annotation
     */
    private String comment;

    /**
     * Creation time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date creationTime;

    /**
     * Update time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * Value
     */
    private String value;

    public DataDictionary() {
    }

    public DataDictionary(String bond, String value, String comment) {
        this.bond = bond;
        this.value = value;
        this.comment = comment;
    }
}
