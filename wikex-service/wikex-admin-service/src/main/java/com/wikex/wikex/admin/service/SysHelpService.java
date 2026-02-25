package com.wikex.wikex.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.admin.entity.SysHelp;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.constant.SysHelpClassification;

import java.util.List;

/**
 *
 * @author markchao
 * @since 2021-08-20
 */
public interface SysHelpService extends IService<SysHelp> {

    List<SysHelp> findBySysHelpClassification(SysHelpClassification sysHelpClassification);

    IPage<SysHelp> findByCondition(int pageNo, int pageSize, SysHelpClassification help, String lang);

    List<SysHelp> getCateTops(String cate, String lang);

    int getMaxSort();
}
