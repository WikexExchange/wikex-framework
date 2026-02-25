package com.wikex.wikex.agent.controller;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.swap.feign.ContractRewardRecordFeign;
import com.wikex.wikex.swap.vo.RewardSetVo;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWeightUpper;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberWeightUpperFeign;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author sulinxin
 * @description Transaction rebate configuration for members (Agent management)
 */
@Slf4j
@RestController
@RequestMapping("transactionRebateSet")
public class MemberTransactionRebateSetController extends BaseController {

    @Resource
    private MemberFeign memberFeign;
    @Resource
    private MemberWeightUpperFeign memberWeightUpperFeign;
    @Resource
    private ContractRewardRecordFeign contractRewardRecordFeign;
    @Autowired
    private LocaleMessageSourceService messageSource;

    /**
     * Query rebate configuration for the current agent
     */
    @RequestMapping(value = "query")
    @PermissionOperation
    public MessageResult query(
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember
    ) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member checkMember = memberFeign.findMemberById(user.getId());
        if (!checkMember.getSuperPartner().equals("1")) {
            return error(messageSource.getMessage("NOT_AN_AGENT"));
        }
        RewardSetVo vo = memberWeightUpperFeign.findRewardSetVoById(user.getId());
        return success(vo);
    }

    /**
     * Clear rebate configuration for the current agent
     */
    @RequestMapping(value = "clear")
    @PermissionOperation
    public MessageResult clear(
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember
    ){
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member checkMember = memberFeign.findMemberById(user.getId());
        if (!checkMember.getSuperPartner().equals("1")) {
            return error(messageSource.getMessage("NOT_AN_AGENT"));
        }
        contractRewardRecordFeign.clearRewardSetVoById(user.getId());
        return success();
    }

    /**
     * Set rebate ratio for a specific member
     */
    @PermissionOperation
    @RequestMapping(value = "set")
    public MessageResult set(
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam("id") Long memberId,
            @RequestParam("rate") Integer rate
    ) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        if (memberId == null) {
            return error(messageSource.getMessage("USER_ID_CANNOT_BE_EMPTY"));
        }
        if (rate == null) {
            return error(messageSource.getMessage("USER_PROPORTION_CANNOT_BE_EMPTY"));
        }
        if (rate.intValue() < 0 || rate.intValue() > 100) {
            return error(messageSource.getMessage("USER_PROPORTION_CANNOT_BE_LESS_THAN_0_OR_MORE_THAN_100_PERCENT"));
        }

        Member checkMember = memberFeign.findMemberById(user.getId());
        if (!"1".equals(checkMember.getSuperPartner())) {
            return error(messageSource.getMessage("NOT_AN_AGENT"));
        }

        Member setMember = memberFeign.findMemberById(memberId);
        // Validation: cannot set rebate for sub-agents
        // if("1".equals(setMember.getSuperPartner())){
        //     return error("You cannot set rebate for sub-agents!");
        // }

        MemberWeightUpper upperMemberId = memberWeightUpperFeign.findMemberWeightUpperByMemberId(setMember.getInviterId());
        Member upperMember = memberFeign.findMemberById(setMember.getInviterId());

        // Validate rate compared to upper level referee
        if ("1".equals(upperMember.getSuperPartner())) {
            if (rate.intValue() >= 100) {
                return error(messageSource.getMessage("PROPORTION_SHOULD_BE_LESS_THAN_THE_SUPERIOR_REFEREE"));
            }
        } else {
            if (rate.intValue() >= upperMemberId.getRate().intValue()) {
                return error(messageSource.getMessage("PROPORTION_SHOULD_BE_LESS_THAN_THE_SUPERIOR_REFEREE"));
            }
        }

        // Validate rate compared to direct referees
        List<Member> promotionMember = memberFeign.findPromotionMember(setMember.getId());
        if (promotionMember != null && !promotionMember.isEmpty()) {
            String idString = promotionMember.stream().map(e -> e.getId().toString()).collect(Collectors.joining(","));
            List<MemberWeightUpper> uppers = memberWeightUpperFeign.findAllByUpperIds(idString);
            for (MemberWeightUpper memberWeightUpper : uppers) {
                if (rate.intValue() <= memberWeightUpper.getRate().intValue()) {
                    return error(messageSource.getMessage("PROPORTION_SHOULD_BE_GREATER_THAN_THE_DIRECT_REFEREE"));
                }
            }
        }

        
        MemberWeightUpper memberWeightUpper = memberWeightUpperFeign.saveMemberWeightUpper(setMember);
        

        // Validation of higher-level super partners
        // if(memberWeightUpper.getUpper() != null){
        //     MessageResult<List<Member>> allByIds = memberFeign.findSuperPartnerMembersByIds(memberWeightUpper.getUpper());
        //     
        //     if(allByIds!=null){
        //         Optional<Member> firstSuperPartner = allByIds.getData().stream().filter(e -> "1".equals(e.getSuperPartner())).findFirst();
        //         if(firstSuperPartner.isPresent()){
        //             if (!firstSuperPartner.get().getId().equals(checkMember.getId())){
        //                 return error("You cannot set rebate for users under another agent!");
        //             }
        //         }
        //     }
        // }

        memberWeightUpper.setRate(rate);
        memberWeightUpperFeign.modifyMemberWeightUpper(memberWeightUpper);
        contractRewardRecordFeign.clearRewardSetVoById(user.getId());
        return success();
    }
}
