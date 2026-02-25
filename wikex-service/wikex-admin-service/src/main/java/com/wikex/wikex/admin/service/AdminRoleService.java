package com.wikex.wikex.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.admin.entity.AdminPermission;
import com.wikex.wikex.admin.entity.AdminRole;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.core.Menu;
import com.wikex.wikex.util.MessageResult;

import java.util.List;

/**
 *
 * @author markchao
 * @since 2021-08-20
 */
public interface AdminRoleService extends IService<AdminRole> {

    List<Menu> toMenus(List<AdminPermission> list, Long parentId);

    IPage<AdminRole> findAll(int pageNo, int pageSize);

    MessageResult deletes(Long id);

}
