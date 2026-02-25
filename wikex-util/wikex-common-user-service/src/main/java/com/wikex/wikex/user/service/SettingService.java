package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.Settings;

import java.util.List;
import java.util.Map;

public interface SettingService extends IService<Settings> {

    Settings findByName(String name);
    Map<String, Settings> mapByArrNames(List<String> names);
}
