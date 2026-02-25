package com.wikex.wikex.p2p.controller;


import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.CertifiedBusinessStatus;
import com.wikex.wikex.constant.CommonStatus;
import com.wikex.wikex.constant.MemberLevelEnum;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.exception.WikexRuntimeException;
import com.wikex.wikex.p2p.entity.BusinessAuthApply;
import com.wikex.wikex.p2p.entity.BusinessAuthDeposit;
import com.wikex.wikex.p2p.entity.BusinessCancelApply;
import com.wikex.wikex.p2p.entity.CertifiedBusinessInfo;
import com.wikex.wikex.p2p.service.AdvertiseService;
import com.wikex.wikex.p2p.service.BusinessAuthApplyService;
import com.wikex.wikex.p2p.service.BusinessAuthDepositService;
import com.wikex.wikex.p2p.service.BusinessCancelApplyService;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.MessageResult;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

import static com.wikex.wikex.constant.CertifiedBusinessStatus.*;
import static org.springframework.util.Assert.isTrue;


/**
 * User Center Certification
 *
 * @author Hevin
 * @date 2020-12-19
 */
@RestController
@RequestMapping("/approve")
@Slf4j
public class ApproveController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(ApproveController.class);

    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private MemberWalletFeign memberWalletFeign;
    @Autowired
    private BusinessAuthDepositService businessAuthDepositService;
    @Autowired
    private BusinessCancelApplyService businessCancelApplyService ;
    @Autowired
    private BusinessAuthApplyService businessAuthApplyService;

    @Autowired
    private AdvertiseService advertiseService;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @Autowired
    private CoinFeign coinFeign;

    /**
     * Certified business application status
     *
     * @return
     */
    @PermissionOperation
    @RequestMapping("/certified/business/status")
    public MessageResult certifiedBusinessStatus(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member =  memberFeign.findMemberById(user.getId());
        if(member==null){
            return this.error(msService.getMessage("MEMBER_NOT_EXISTS"));
        }
        CertifiedBusinessInfo certifiedBusinessInfo = new CertifiedBusinessInfo();
        certifiedBusinessInfo.setCertifiedBusinessStatus(member.getCertifiedBusinessStatus().getCode());
        certifiedBusinessInfo.setEmail(member.getEmail());
        certifiedBusinessInfo.setMemberLevel(member.getMemberLevel());
        logger.info("Member status info:{}",certifiedBusinessInfo);
        if(member.getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.FAILED)){
            List<BusinessAuthApply> businessAuthApplyList=businessAuthApplyService.findByMemberAndCertifiedBusinessStatus(member.getId(),member.getCertifiedBusinessStatus());
            logger.info("Member business certification application info:{}",businessAuthApplyList);
            if(businessAuthApplyList!=null&&businessAuthApplyList.size()>0){
                certifiedBusinessInfo.setCertifiedBusinessStatus(businessAuthApplyList.get(0).getCertifiedBusinessStatus().getCode());
                logger.info("Latest business certification application info:{}",businessAuthApplyList.get(0));
                certifiedBusinessInfo.setDetail(businessAuthApplyList.get(0).getDetail());
            }
        }

        List<BusinessCancelApply> businessCancelApplies = businessCancelApplyService.findByMember(member.getId());
        if(businessCancelApplies!=null&&businessCancelApplies.size()>0){
            if(businessCancelApplies.get(0).getStatus()==RETURN_SUCCESS.getCode()) {
                if(member.getCertifiedBusinessStatus()!=VERIFIED) {
                    certifiedBusinessInfo.setCertifiedBusinessStatus(RETURN_SUCCESS.getCode());
                }
            }else if(businessCancelApplies.get(0).getStatus()==RETURN_FAILED.getCode()){
                certifiedBusinessInfo.setCertifiedBusinessStatus(RETURN_FAILED.getCode());
            }else{
                certifiedBusinessInfo.setCertifiedBusinessStatus(CANCEL_AUTH.getCode());
            }
        }

        MessageResult result = MessageResult.success();
        result.setData(certifiedBusinessInfo);
        return result;
    }

    /**
     * Certified business application
     *
     * @param
     * @return
     */
    @RequestMapping("/certified/business/apply")
    @PermissionOperation
    @GlobalTransactional(rollbackFor = Exception.class)
    public MessageResult certifiedBusiness(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, String json,
                                           @RequestParam Long businessAuthDepositId) throws WikexRuntimeException {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if(member==null){
            return this.error(msService.getMessage("MEMBER_NOT_EXISTS"));
        }
        // Only users who are not certified or failed certification can initiate an application
        isTrue(member.getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.NOT_CERTIFIED)
                ||member.getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.FAILED), msService.getMessage("REPEAT_APPLICATION"));
        isTrue(member.getMemberLevel().equals(MemberLevelEnum.REALNAME.getCode()), msService.getMessage("NO_REAL_NAME"));
        List<BusinessAuthDeposit> depositList=businessAuthDepositService.findAllByStatus(CommonStatus.NORMAL);
        // If there are active deposit types, one must be selected before applying for business certification
        BusinessAuthDeposit businessAuthDeposit=null;
        if(depositList!=null&&depositList.size()>0){
            if(businessAuthDepositId==null){
                return MessageResult.error("must select a kind of business auth deposit");
            }
            boolean flag=false;
            for(BusinessAuthDeposit deposit:depositList){
                if(deposit.getId().equals(businessAuthDepositId)){
                    businessAuthDeposit=deposit;
                    flag=true;
                }
            }
            if(!flag){
                return MessageResult.error("business auth deposit is not found");
            }
            MemberWallet memberWallet = memberWalletFeign.findByCoinUnitAndMemberId(businessAuthDeposit.getCoinId(), member.getId());
            if(memberWallet==null){
                return MessageResult.error(messageSource.getMessage("INSUFFICIENT_BALANCE"));
            }
            if(memberWallet.getBalance().compareTo(businessAuthDeposit.getAmount())<0){
                return MessageResult.error(messageSource.getMessage("INSUFFICIENT_BALANCE"));
            }
            // Freeze the deposit amount required
            MessageResult result = memberWalletFeign.freezeBalance(memberWallet.getId(),businessAuthDeposit.getAmount());
            if (result.getCode() != 0) {
                throw new WikexRuntimeException("UNABLE_TO_LOCK_ASSET");
            }
        }
        // Application record
        BusinessAuthApply businessAuthApply=new BusinessAuthApply();
        businessAuthApply.setCreateTime(new Date());
        businessAuthApply.setAuthInfo(json);
        businessAuthApply.setCertifiedBusinessStatus(CertifiedBusinessStatus.AUDITING);
        businessAuthApply.setMemberId(member.getId());
        // Not all may have a deposit policy
        if(businessAuthDeposit!=null){
            businessAuthApply.setBusinessAuthDepositId(businessAuthDeposit.getId());
            businessAuthApply.setAmount(businessAuthDeposit.getAmount());
        }
        businessAuthApplyService.save(businessAuthApply);

        member.setCertifiedBusinessApplyTime(new Date());
        member.setCertifiedBusinessStatus(CertifiedBusinessStatus.AUDITING);
        MessageResult updateMemberResult = memberFeign.updateMemberById(member);
        if (updateMemberResult.getCode() != 0) {
            throw new WikexRuntimeException("UNABLE_TO_LOCK_ASSET");
        }
        CertifiedBusinessInfo certifiedBusinessInfo = new CertifiedBusinessInfo();
        certifiedBusinessInfo.setCertifiedBusinessStatus(member.getCertifiedBusinessStatus().getCode());
        certifiedBusinessInfo.setEmail(member.getEmail());
        certifiedBusinessInfo.setMemberLevel(member.getMemberLevel());
        MessageResult result = MessageResult.success();
        result.setData(certifiedBusinessInfo);
        return result;
    }

    @RequestMapping("/business-auth-deposit/list")
    public MessageResult listBusinessAuthDepositList(){
        List<BusinessAuthDeposit> depositList=businessAuthDepositService.findAllByStatus(CommonStatus.NORMAL);
        depositList.forEach(deposit->{
            deposit.setCoin(coinFeign.findByUnit(deposit.getCoinId()));
            deposit.setAdminId(null);
        });
        MessageResult result=MessageResult.success();
        result.setData(depositList);
        return result;
    }

    /**
     * Apply to cancel certified business
     * @return
     */
    @PostMapping("/cancel/business")
    @PermissionOperation
    @GlobalTransactional(rollbackFor = Exception.class)
    public MessageResult cancelBusiness(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                        @RequestParam(value = "detail",defaultValue = "")String detail) throws WikexRuntimeException {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if(member==null){
            return this.error(msService.getMessage("MEMBER_NOT_EXISTS"));
        }
        if(member.getCertifiedBusinessStatus()==CANCEL_AUTH){
            return MessageResult.error(messageSource.getMessage("WITHDRAWAL_UNDER_REVIEW_DO_NOT_RESUBMIT"));
        }
        if(!member.getCertifiedBusinessStatus().equals(VERIFIED)){
            return MessageResult.error("you are not verified business");
        }

        List<BusinessAuthApply> businessAuthApplyList=businessAuthApplyService.findByMemberAndCertifiedBusinessStatus(member.getId(), VERIFIED);
        if(businessAuthApplyList==null||businessAuthApplyList.size()<1){
            return MessageResult.error("you are not verified business,business auth apply not exist......");
        }

        if(businessAuthApplyList.get(0).getCertifiedBusinessStatus()!= VERIFIED){
            return MessageResult.error("data exception, state inconsistency(CertifiedBusinessStatus in BusinessAuthApply and Member)");
        }

        member.setCertifiedBusinessStatus(CANCEL_AUTH);
        memberFeign.updateMemberById(member);

        BusinessCancelApply cancelApply = new BusinessCancelApply();
        cancelApply.setDepositRecordId(businessAuthApplyList.get(0).getDepositRecordId());
        cancelApply.setMemberId(businessAuthApplyList.get(0).getMemberId());
        cancelApply.setStatus(CANCEL_AUTH.getCode());
        cancelApply.setReason(detail);
        cancelApply.setCancelApplyTime(DateUtil.getCurrentDate());
        businessCancelApplyService.save(cancelApply);

        return MessageResult.success();
    }

}
