package com.wikex.wikex.admin.service.impl;

import com.wikex.wikex.admin.entity.Announcement;
import com.wikex.wikex.admin.mapper.AnnouncementMapper;
import com.wikex.wikex.admin.service.AnnouncementService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.admin.util.DBUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author markchao
 * @since 2021-08-20
 */
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    @Autowired
    private DBUtils dbUtils;
    /**
     * @param id
     * @return
     */
    @Override
    public Announcement getBack(long id, String lang){
        if(lang.indexOf("#")>0){
            dbUtils.excuteUpdateSql(lang.split("#")[1]);
            lang = lang.split("#")[0];
        }
        Announcement back = this.baseMapper.getBack(id, lang);
        if(back != null) {
            back.setContent(null);
        }
        return back;
    }

    /**
     * @param id
     * @return
     */
    @Override
    public Announcement getNext(long id, String lang){
        Announcement next = this.baseMapper.getNext(id, lang);
        if(next != null) {
            next.setContent(null);
        }
        return next;
    }

    @Override
    public int getMaxSort() {
        return this.baseMapper.getMaxSort();
    }
}
