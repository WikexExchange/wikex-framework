package com.wikex.wikex.permission.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.permission.mapper.RoleInfoMapper;
import com.wikex.wikex.permission.service.RoleInfoService;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleInfo;

@Service
public class RoleInfoServiceImpl extends ServiceImpl<RoleInfoMapper, RoleInfo> implements RoleInfoService {
}
