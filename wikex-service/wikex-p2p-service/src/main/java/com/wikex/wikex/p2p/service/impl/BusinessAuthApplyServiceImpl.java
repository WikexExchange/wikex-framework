package com.wikex.wikex.p2p.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.CertifiedBusinessStatus;
import com.wikex.wikex.p2p.entity.BusinessAuthApply;
import com.wikex.wikex.p2p.entity.BusinessAuthDeposit;
import com.wikex.wikex.p2p.mapper.BusinessAuthApplyMapper;
import com.wikex.wikex.p2p.service.BusinessAuthApplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.p2p.service.BusinessAuthDepositService;
import com.wikex.wikex.p2p.vo.BusinessAuthApplyDetailVO;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class BusinessAuthApplyServiceImpl extends ServiceImpl<BusinessAuthApplyMapper, BusinessAuthApply> implements BusinessAuthApplyService {

    @Autowired
    private MemberFeign memberFeign;

    @Autowired
    private BusinessAuthDepositService businessAuthDepositService;

    @Override
    public List<BusinessAuthApply> findByMemberAndCertifiedBusinessStatus(Long memberId, CertifiedBusinessStatus certifiedBusinessStatus) {
        QueryWrapper<BusinessAuthApply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId).eq("certified_business_status",certifiedBusinessStatus.getCode());
        queryWrapper.orderByDesc("id");
        return this.list(queryWrapper);
    }

    @Override
    public MessageResult detail(Long id) {
        BusinessAuthApplyDetailVO vo = new BusinessAuthApplyDetailVO();
        BusinessAuthApply authApply = this.getById(id);
        if(authApply!=null){
            vo.setId(authApply.getId());
            vo.setStatus(authApply.getCertifiedBusinessStatus());
            vo.setAmount(authApply.getAmount());
            vo.setAuthInfo(authApply.getAuthInfo());
            Member member = memberFeign.findMemberById(authApply.getMemberId());
            if(member!=null) {
                vo.setRealName(member.getRealName());
            }
            vo.setDetail(authApply.getDetail());
            vo.setCheckTime(authApply.getAuditingTime());
        }

        MessageResult result;
        String jsonStr = vo.getAuthInfo() ;
        
        if (StringUtils.isEmpty(jsonStr)) {
            result = MessageResult.error("AUTHENTICATION_INFORMATION_DOES_NOT_EXIST");
            result.setData(vo);
            return result;
        }
        try {
            JSONObject json = JSONObject.parseObject(jsonStr);
            vo.setInfo(json);
            result = MessageResult.success("CERTIFICATION_DETAILS");
            result.setData(vo);
            return result;
        } catch (Exception e) {
            
            result = MessageResult.error("ABNORMAL_AUTHENTICATION_INFORMATION_FORMAT");
            return result;
        }
    }

    @Override
    public Page<BusinessAuthApply> pageApply(Integer pageNo,Integer pageSize, CertifiedBusinessStatus status, String account) {
        Page<BusinessAuthApply> page = new Page<>(pageNo,pageSize);
        LambdaQueryWrapper<BusinessAuthApply> query = new LambdaQueryWrapper<>();
        if(status!=null){
            query.eq(BusinessAuthApply::getCertifiedBusinessStatus,status.getCode());
        }
        List<Long> ids = memberFeign.findMemberIdsByAccountAndNotCertified(account);
        if(ids!=null && ids.size()>0){
            query.in(BusinessAuthApply::getMemberId,ids);
        }else {
            
            query.eq(BusinessAuthApply::getMemberId,-1);
        }
        query.orderByDesc(BusinessAuthApply::getId);
        Page<BusinessAuthApply> businessAuthApplyPage = this.page(page, query);
        List<BusinessAuthApply> businessAuthApplyList = businessAuthApplyPage.getRecords();
        if (!businessAuthApplyList.isEmpty()) {
            List<Long> memberIds = businessAuthApplyList.stream()
                    .map(BusinessAuthApply::getMemberId)
                    .distinct()
                    .collect(Collectors.toList());
            List<Long> businessAuthDepositIds = businessAuthApplyList.stream()
                    .map(BusinessAuthApply::getBusinessAuthDepositId)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, Member> memberMap = memberFeign.mapByMemberIds(memberIds);
            List<BusinessAuthDeposit> businessAuthDepositList = businessAuthDepositService.listByIds(businessAuthDepositIds);

            for (BusinessAuthApply businessAuthApply : businessAuthApplyList) {
                businessAuthApply.setMember(memberMap.get(businessAuthApply.getMemberId()));
                businessAuthApply.setBusinessAuthDeposit(businessAuthDepositList.stream()
                        .filter(businessAuthDeposit -> businessAuthDeposit.getId().equals(businessAuthApply.getBusinessAuthDepositId()))
                        .findFirst()
                        .orElse(null));
            }
        }
        return businessAuthApplyPage;
    }

    @Override
    public Integer countAuditing() {
        LambdaQueryWrapper<BusinessAuthApply> query = new LambdaQueryWrapper<>();
        query.eq(BusinessAuthApply::getCertifiedBusinessStatus,CertifiedBusinessStatus.AUDITING);
        return this.count(query);
    }
}
