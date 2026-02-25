package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.MySteryBox;

public interface MySteryBoxService extends IService<MySteryBox> {

    MySteryBox findAllByCode(String code);

    MySteryBox findAllByMemberId(Long member_id);
}
