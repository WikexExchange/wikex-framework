package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.Settings;
import com.wikex.wikex.user.mapper.SettingMapper;
import com.wikex.wikex.user.service.SettingService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SettingServiceImpl extends ServiceImpl<SettingMapper, Settings> implements SettingService {

    @Override
    public Settings findByName(String name) {
        QueryWrapper<Settings> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name)
                .eq("is_active", 1);
        return this.getOne(queryWrapper);
    }

    @Override
    public Map<String, Settings> mapByArrNames(List<String> names) {
        Map<String, Settings> map = new HashMap<>();
        LambdaQueryWrapper<Settings> query = new LambdaQueryWrapper<>();
        query.in(Settings::getName, names).eq(Settings::getIs_active, 1);
        List<Settings> allByIdIn = this.list(query);
        allByIdIn.forEach(v -> {
            map.put(v.getName(), v);
        });

        return map;
    }
}
