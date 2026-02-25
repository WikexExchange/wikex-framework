package com.wikex.wikex.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.admin.entity.DataDictionary;
import com.wikex.wikex.admin.mapper.DataDictionaryMapper;
import com.wikex.wikex.admin.service.DataDictionaryService;
import org.springframework.stereotype.Service;

/**
 *
 * @author markchao
 * @since 2021-06-21
 */
@Service
public class DataDictionaryServiceImpl extends ServiceImpl<DataDictionaryMapper, DataDictionary> implements DataDictionaryService {

    @Override
    public DataDictionary findByBond(String bond) {
        QueryWrapper<DataDictionary> query = new QueryWrapper<>();
        query.eq("bond",bond);
        return this.getOne(query);
    }
}
