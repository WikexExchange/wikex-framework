package com.wikex.wikex.swap.feign;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.ContractRewardRecordScreen;
import com.wikex.wikex.swap.entity.MemberTradeLimit;
import com.wikex.wikex.swap.service.MemberTradeLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;


@RestController
@Slf4j
@RequestMapping("/memberTradeLimitFeign")
public class MemberTradeLimitFeignController {


    @Autowired
    private MemberTradeLimitService memberTradeLimitService;

    
    @PostMapping("page-query")
    public Page<MemberTradeLimit> findAll(ContractRewardRecordScreen screen) {
        Page<MemberTradeLimit> all = memberTradeLimitService.findAll(screen);
        return all;
    }

    @PostMapping(value = "findOne")
    public MemberTradeLimit findOne(@RequestParam("id") Long id){
        return memberTradeLimitService.getById(id);
    }

    @PostMapping(value = "findLimitByMemberIdAndContractId")
    public MemberTradeLimit findLimitByMemberIdAndContractId(@RequestParam("memberId") Long memberId,@RequestParam("contractId") Long contractId){
        return memberTradeLimitService.findLimitByMemberIdAndContractId(memberId,contractId);
    }


    
    @PostMapping("save")
    public MemberTradeLimit save(@RequestBody MemberTradeLimit limit) {
        if(limit.getId()==null){
            limit.setCreateTime(new Date().getTime());
        }else {
            limit.setUpdateTime(new Date().getTime());
        }
        memberTradeLimitService.saveOrUpdate(limit);
        return limit;
    }

    
    @PostMapping("del")
    public void del(@RequestParam("ids") List<Long> ids) {
        memberTradeLimitService.removeByIds(ids);
    }
}

