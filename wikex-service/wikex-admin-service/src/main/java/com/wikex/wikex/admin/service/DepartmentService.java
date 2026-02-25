package com.wikex.wikex.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.admin.entity.Department;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.util.MessageResult;

/**
 *
 * @author markchao
 * @since 2021-08-20
 */
public interface DepartmentService extends IService<Department> {

    IPage<Department> findAll(Integer pageNo, Integer pageSize);

    MessageResult deletes(Long id);
}
