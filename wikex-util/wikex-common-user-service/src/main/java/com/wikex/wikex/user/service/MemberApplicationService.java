package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.MemberApplicationScreen;
import com.wikex.wikex.user.entity.MemberApplication;
import com.wikex.wikex.user.vo.MemberApplicationVo;

import java.util.List;


public interface MemberApplicationService extends IService<MemberApplication> {

    List<MemberApplication> findLatelyReject(Long memberId);

    int queryByIdCard(String idCard);

    Page<MemberApplicationVo> findAll(MemberApplicationScreen pageParam);

    void auditPass(MemberApplication application);

    void auditNotPass(MemberApplication application);

    Integer countAuditing();
}
