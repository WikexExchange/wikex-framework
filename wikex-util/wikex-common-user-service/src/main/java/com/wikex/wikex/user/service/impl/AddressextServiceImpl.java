package com.wikex.wikex.user.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.Addressext;
import com.wikex.wikex.user.mapper.AddressextMapper;
import com.wikex.wikex.user.service.AddressextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class AddressextServiceImpl extends ServiceImpl<AddressextMapper, Addressext> implements AddressextService {


    public Addressext read(Integer memberId, Integer coinprotocol) {
        LambdaQueryWrapper<Addressext> query = new LambdaQueryWrapper<>();
        query.eq(Addressext::getMemberId,memberId);
        query.eq(Addressext::getCoinProtocol,coinprotocol);
        List<Addressext> list = this.list(query);
        if(list!=null && list.size()>0){
            return list.get(0);
        }
        return null;
    }

    public List<Addressext> listByMemberId(Integer memberId) {
        LambdaQueryWrapper<Addressext> query = new LambdaQueryWrapper<>();
        query.eq(Addressext::getMemberId, memberId);
        query.eq(Addressext::getStatus, 1);
        return this.list(query);
    }

    public Addressext findByAddress(String address) {
        LambdaQueryWrapper<Addressext> query = new LambdaQueryWrapper<>();
        query.eq(Addressext::getAddress,address);
        query.eq(Addressext::getStatus,1);
        List<Addressext> list = this.list(query);
        if(list!=null && list.size()>0){
            return list.get(0);
        }
        return null;
    }

    
    public Addressext notUsed(Integer coinprotocol) {

        LambdaQueryWrapper<Addressext> query = new LambdaQueryWrapper<>();
        query.eq(Addressext::getCoinProtocol,coinprotocol);
        query.eq(Addressext::getStatus,0);
        List<Addressext> list = this.list(query);
        if(list!=null && list.size()>0){
            return list.get(0);
        }
        return null;
    }

    public Addressext saveAndFlush(Addressext addressext) {
        this.save(addressext);
        return addressext;
    }

    
    @Transactional
    public Integer create(Integer id, Integer memberid) {
        UpdateWrapper<Addressext> update = new UpdateWrapper<>();
        update.set("member_id",memberid);
        update.eq("id",id);
        boolean update1 = this.update(update);
        if(update1){
            return 1;
        }else {
            return 0;
        }
    }

    public Addressext findByMemberIdAndChain(Long memberId, String chain) {
        LambdaQueryWrapper<Addressext> query = new LambdaQueryWrapper<>();
        query.eq(Addressext::getMemberId,memberId);
        query.eq(Addressext::getChain,chain);
        query.eq(Addressext::getStatus,1);
        return this.getOne(query, false);
    }

    public long countByMemberIdAndChain(Long memberId, String chain) {
        LambdaQueryWrapper<Addressext> query = new LambdaQueryWrapper<>();
        query.eq(Addressext::getMemberId,memberId);
        query.eq(Addressext::getChain,chain);
        query.eq(Addressext::getStatus,1);
        return this.count(query);
    }

}
