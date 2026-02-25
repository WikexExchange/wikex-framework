package com.wikex.wikex.admin.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.admin.entity.Department;
import com.wikex.wikex.admin.service.DepartmentService;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.util.BindingResultUtil;
import com.wikex.wikex.util.MessageResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

 // Department Management Controller
@RestController
@RequestMapping(value = "/system/department")
public class DepartmentController extends BaseAdminController {

    @Autowired
    private DepartmentService departmentService;

    /**
     * Create or update a department
     *
     * @param department department entity
     * @return MessageResult
     */
    @RequiresPermissions("system:department:merge")
    @RequestMapping("merge")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Create or update Department")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult save(@Valid Department department, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        if (department.getId() != null) {
            department.setCreateTime(departmentService.getById(department.getId()).getCreateTime());
        }
        departmentService.saveOrUpdate(department);
        return success();
    }

    /**
     * Department details
     *
     * @param departmentId department id
     * @return MessageResult with department info
     */
    @RequiresPermissions("system:department:detail")
    @RequestMapping("detail")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Department details")
    public MessageResult detail(Long departmentId) {
        Department department = departmentService.getById(departmentId);
        return success(department);
    }

    /**
     * Get all departments with pagination
     *
     * @return MessageResult with list of departments
     */
    @RequiresPermissions("system:department:all")
    @RequestMapping("all")
    @AccessLog(module = AdminModule.SYSTEM, operation = "All Departments")
    public MessageResult allDepartment(PageParam pageParam) {
        IPage<Department> all = departmentService.findAll(pageParam.getPageNo(), pageParam.getPageSize());
        return success(IPage2Page(all));
    }

    /**
     * Batch delete departments
     *
     * @param id department id
     * @return MessageResult
     */
    @RequiresPermissions("system:department:deletes")
    @RequestMapping("deletes")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Batch delete Departments")
    public MessageResult deletes(@RequestParam(value = "id") Long id) {
        MessageResult result = departmentService.deletes(id);
        return result;
    }
}
