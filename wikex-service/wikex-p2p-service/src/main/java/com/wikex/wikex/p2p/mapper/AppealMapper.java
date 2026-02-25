package com.wikex.wikex.p2p.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.p2p.entity.Appeal;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.p2p.vo.AppealVo;
import com.wikex.wikex.screen.AppealScreen;
import org.apache.ibatis.annotations.Param;


public interface AppealMapper extends BaseMapper<Appeal> {

    Page<AppealVo> appealQuery(Page<AppealVo> page, @Param("screen") AppealScreen screen);
}
