package com.wikex.wikex.admin.entity;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class AdminRolePermission implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long roleId;

    private Long ruleId;


}
