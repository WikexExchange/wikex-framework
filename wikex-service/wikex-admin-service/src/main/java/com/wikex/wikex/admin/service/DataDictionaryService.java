package com.wikex.wikex.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.admin.entity.DataDictionary;

/**
 *
 * @author markchao
 * @since 2021-06-21
 */
public interface DataDictionaryService extends IService<DataDictionary> {

    DataDictionary findByBond(String bond);
}
