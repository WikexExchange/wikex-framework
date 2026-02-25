package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.MemberApiKey;

import java.util.List;


public interface MemberApiKeyService extends IService<MemberApiKey> {

    MemberApiKey findMemberApiKeyByApiKey(String apiKey);

    List<MemberApiKey> findAllByMemberId(Long memberId);

    MemberApiKey findByMemberIdAndId(Long memberId, Long id);
}
