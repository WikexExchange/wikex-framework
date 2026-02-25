package com.wikex.wikex.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.entity.Admin;
import com.wikex.wikex.admin.entity.AdminPermission;
import com.wikex.wikex.admin.entity.AdminRole;
import com.wikex.wikex.admin.mapper.AdminRoleMapper;
import com.wikex.wikex.admin.service.AdminRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.admin.service.AdminService;
import com.wikex.wikex.core.Menu;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author markchao
 * @since 2021-08-20
 */
@Service
public class AdminRoleServiceImpl extends ServiceImpl<AdminRoleMapper, AdminRole> implements AdminRoleService {

    @Autowired
    private LocaleMessageSourceService messageSourceService;
    @Autowired
    private AdminService adminService;

    /**
     * @param list
     * @param parentId
     * @return
     */
    @Override
    public List<Menu> toMenus(List<AdminPermission> list, Long parentId) {
        return list.stream()
                .filter(x -> x.getParentId().equals(parentId))
                .sorted(Comparator.comparing(AdminPermission::getSort))
                .map(x ->
                        Menu.builder()
                                .id(x.getId())
                                .name(x.getName())
                                .parentId(x.getParentId())
                                .sort(x.getSort())
                                .title(x.getTitle())
                                .titleKey(x.getTitleKey())
                                .description(x.getDescription())
                                .subMenu(toMenus(list, x.getId()))
                                .build()

                )
                .collect(Collectors.toList());
    }

    @Override
    public IPage<AdminRole> findAll(int pageNo, int pageSize) {
        IPage<AdminRole> page = new Page<>(pageNo,pageSize);
        QueryWrapper<AdminRole> queryWrapper = new QueryWrapper<>();
        return this.page(page,queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageResult deletes(Long id) {
        List<Admin> list = adminService.findAllByRoleId(id);
        if (list != null && list.size() > 0) {
            return MessageResult.error(messageSourceService.getMessage("FAIL"));
        }
        this.removeById(id);
        return MessageResult.success(messageSourceService.getMessage("SUCCESS"));
    }

}
