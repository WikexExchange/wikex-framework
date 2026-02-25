package com.wikex.wikex.admin.service;

import com.wikex.wikex.admin.entity.SysAdvertise;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.constant.SysAdvertiseLocation;

import java.util.List;

/**
 *
 * @author markchao
 * @since 2021-08-20
 */
public interface SysAdvertiseService extends IService<SysAdvertise> {

    List<SysAdvertise> findAllNormal(SysAdvertiseLocation sysAdvertiseLocation, String headerLanguage);

    int getMaxSort();

    List<SysAdvertise> querySysAdvertise(int sort, int cate);
}
