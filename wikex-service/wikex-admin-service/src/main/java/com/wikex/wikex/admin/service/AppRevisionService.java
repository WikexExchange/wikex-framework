package com.wikex.wikex.admin.service;

import com.wikex.wikex.admin.entity.AppRevision;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.constant.Platform;

/**
 *
 * @author markchao
 * @since 2021-08-20
 */
public interface AppRevisionService extends IService<AppRevision> {

    AppRevision findRecentVersion(Platform platform);
}
