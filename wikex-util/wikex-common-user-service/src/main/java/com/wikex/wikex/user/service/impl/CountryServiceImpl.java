package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.Country;
import com.wikex.wikex.user.mapper.CountryMapper;
import com.wikex.wikex.user.service.CountryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CountryServiceImpl extends ServiceImpl<CountryMapper, Country> implements CountryService {

    @Override
    public List<Country> getAllCountry() {
        LambdaQueryWrapper<Country> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.orderByAsc(Country::getSort);
        return this.list(queryWrapper);
    }

    @Override
    public Country findOne(String zhName) {
        QueryWrapper<Country> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vi_name", zhName);
        return this.getOne(queryWrapper);
    }
}
