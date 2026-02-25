package com.wikex.wikex.admin.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wikex.wikex.constant.AdminModule;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Admin Access Log
 * </p>
 *
 * Author: markchao
 * Since: 2021-08-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AdminAccessLogVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String accessIp;

    private String accessMethod;

    private Date accessTime;

    private Long adminId;

    private String adminName;

    private String moduleName;

    private Integer module;

    private String operation;

    private String uri;


}
