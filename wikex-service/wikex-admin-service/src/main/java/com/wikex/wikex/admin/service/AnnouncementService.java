package com.wikex.wikex.admin.service;

import com.wikex.wikex.admin.entity.Announcement;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *
 * @author markchao
 * @since 2021-08-20
 */
public interface AnnouncementService extends IService<Announcement> {

    Announcement getBack(long id, String lang);

    Announcement getNext(long id, String lang);

    int getMaxSort();
}
