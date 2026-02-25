package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.Country;

import java.util.List;


public interface CountryService extends IService<Country> {

    List<Country> getAllCountry();

    Country findOne(String zhName);
}
