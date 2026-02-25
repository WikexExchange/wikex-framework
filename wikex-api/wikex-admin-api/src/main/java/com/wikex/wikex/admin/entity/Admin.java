package com.wikex.wikex.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import com.wikex.wikex.constant.CommonStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class Admin implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Avatar
     */
    private String avatar;

    private String email;

    /**
     * Enable status: 0 Normal, 1 Illegal
     */
    private CommonStatus enable;

    /**
     * Last login IP
     */
    private String lastLoginIp;

    /**
     * User's last login time
     */
    private Date lastLoginTime;

    /**
     * Contact number
     */
    private String mobilePhone;

    private String password;

    private String qq;

    /**
     * Real name
     */
    private String realName;

    /**
     * Role
     */
    private Long roleId;

    /**
     * Status: 0 Normal, 1 Illegal
     */
    private Integer status = 0;

    /**
     * User login name
     */
    private String username;

    private Long departmentId;

    @TableField(exist = false)// Non-database field
    private String roleName;

    @TableField(exist = false)// Non-database field
    private String departmentName;

}
