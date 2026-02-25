package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.MemberApiKey;
import com.wikex.wikex.user.mapper.MemberApiKeyMapper;
import com.wikex.wikex.user.service.MemberApiKeyService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MemberApiKeyServiceImpl extends ServiceImpl<MemberApiKeyMapper, MemberApiKey> implements MemberApiKeyService {

    @Override
    public MemberApiKey findMemberApiKeyByApiKey(String apiKey) {
        LambdaQueryWrapper<MemberApiKey> query = new LambdaQueryWrapper<>();
        query.eq(MemberApiKey::getApiKey,apiKey);
        List<MemberApiKey> list = this.list(query);
        if(list!=null && list.size()>0){
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<MemberApiKey> findAllByMemberId(Long memberId) {
        LambdaQueryWrapper<MemberApiKey> query = new LambdaQueryWrapper<>();
        query.eq(MemberApiKey::getMemberId,memberId);
        return this.list(query);
    }

    @Override
    public MemberApiKey findByMemberIdAndId(Long memberId, Long id) {
        LambdaQueryWrapper<MemberApiKey> query = new LambdaQueryWrapper<>();
        query.eq(MemberApiKey::getMemberId,memberId);
        query.eq(MemberApiKey::getId,id);
        List<MemberApiKey> list = this.list(query);
        if(list!=null && list.size()>0){
            return list.get(0);
        }
        return null;
    }
}
