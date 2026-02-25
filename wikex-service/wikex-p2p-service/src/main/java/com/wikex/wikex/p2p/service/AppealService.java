package com.wikex.wikex.p2p.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.p2p.entity.Appeal;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.p2p.vo.AppealVo;
import com.wikex.wikex.screen.AppealScreen;


public interface AppealService extends IService<Appeal> {

    Page appealQuery(AppealScreen screen);

    AppealVo findOneAppealVO(Long id);

    Appeal findOne(Long id);

    Integer countAuditing();
}
