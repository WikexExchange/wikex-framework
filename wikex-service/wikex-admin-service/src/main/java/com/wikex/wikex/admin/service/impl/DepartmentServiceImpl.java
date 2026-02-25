package com.wikex.wikex.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.admin.entity.Admin;
import com.wikex.wikex.admin.entity.Department;
import com.wikex.wikex.admin.mapper.DepartmentMapper;
import com.wikex.wikex.admin.service.AdminService;
import com.wikex.wikex.admin.service.DepartmentService;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 * @author markchao
 * @since 2021-08-20
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Autowired
    private LocaleMessageSourceService messageSourceService;
    @Autowired
    private AdminService adminService;

    @Override
    public IPage<Department> findAll(Integer pageNo, Integer pageSize) {
        IPage<Department> page = new Page<>(pageNo,pageSize);
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        return this.page(page,queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageResult deletes(Long id) {
        List<Admin> list = adminService.findAllByDepartment(id);
        if (list != null && list.size() > 0) {
            MessageResult result = MessageResult.error(messageSourceService.getMessage("DELETE_ALL_USERS_IN_THIS_DEPARTMENT"));
            return result;
        }
        this.removeById(id);
        return MessageResult.success(messageSourceService.getMessage("SUCCESS"));
    }
}
