package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.MySteryBox;
import com.wikex.wikex.user.mapper.MySteryBoxMapper;
import com.wikex.wikex.user.service.MySteryBoxService;
import org.springframework.stereotype.Service;

@Service
public class MySteryBoxServiceImpl extends ServiceImpl<MySteryBoxMapper, MySteryBox> implements MySteryBoxService {

    @Override
    public MySteryBox findAllByCode(String code) {
        QueryWrapper<MySteryBox> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code)
                .eq("is_active", true)
                .and(q -> q.isNull("member_id").or().eq("member_id", 0));
        return this.getOne(queryWrapper);
    }

    @Override
    public MySteryBox findAllByMemberId(Long member_id) {
        QueryWrapper<MySteryBox> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", member_id).eq("is_active", true);
        return this.getOne(queryWrapper);
    }
}
