package com.wikex.wikex.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wikex.wikex.admin.entity.SysAdvertise;
import com.wikex.wikex.admin.mapper.SysAdvertiseMapper;
import com.wikex.wikex.admin.service.SysAdvertiseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.constant.CommonStatus;
import com.wikex.wikex.constant.SysAdvertiseLocation;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author markchao
 * @since 2021-08-20
 */
@Service
public class SysAdvertiseServiceImpl extends ServiceImpl<SysAdvertiseMapper, SysAdvertise> implements SysAdvertiseService {

    @Override
    public List<SysAdvertise> findAllNormal(SysAdvertiseLocation sysAdvertiseLocation, String lang) {
        QueryWrapper<SysAdvertise> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", CommonStatus.NORMAL.getCode()).eq("sys_advertise_location",sysAdvertiseLocation.getCode()).eq("lang",lang);
        queryWrapper.orderByAsc("sort");
        return this.list(queryWrapper);
    }

    @Override
    public int getMaxSort() {
        return this.baseMapper.getMaxSort();
    }

    @Override
    public List<SysAdvertise> querySysAdvertise(int sort, int cate) {
        return this.baseMapper.querySysAdvertise(sort,cate);
    }
}
