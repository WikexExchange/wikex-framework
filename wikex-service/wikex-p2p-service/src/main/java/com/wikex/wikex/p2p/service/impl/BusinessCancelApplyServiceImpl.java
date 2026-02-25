package com.wikex.wikex.p2p.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.CertifiedBusinessStatus;
import com.wikex.wikex.p2p.entity.BusinessCancelApply;
import com.wikex.wikex.p2p.entity.DepositRecord;
import com.wikex.wikex.p2p.mapper.BusinessCancelApplyMapper;
import com.wikex.wikex.p2p.service.BusinessCancelApplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.p2p.service.DepositRecordService;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.feign.MemberFeign;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class BusinessCancelApplyServiceImpl extends ServiceImpl<BusinessCancelApplyMapper, BusinessCancelApply> implements BusinessCancelApplyService {
    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private DepositRecordService depositRecordService;

    @Override
    public List<BusinessCancelApply> findByMember(Long memberId) {
        QueryWrapper<BusinessCancelApply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId).orderByDesc("id");
        return this.list(queryWrapper);
    }

    @Override
    public Page<BusinessCancelApply> findAllCancelApply(Integer pageNo, Integer pageSize, CertifiedBusinessStatus status, String account, Date startDate, Date endDate) {
        Page<BusinessCancelApply> page = new Page<BusinessCancelApply>(pageNo,pageSize);
        LambdaQueryWrapper<BusinessCancelApply> query = new LambdaQueryWrapper<>();
        if (status != null) {
            query.eq(BusinessCancelApply::getStatus,status.getCode());
        }else {
            query.in(BusinessCancelApply::getStatus,CertifiedBusinessStatus.CANCEL_AUTH.getCode(), CertifiedBusinessStatus.RETURN_FAILED.getCode(), CertifiedBusinessStatus.RETURN_SUCCESS.getCode());
        }
        if (startDate != null) {
            query.ge(BusinessCancelApply::getCancelApplyTime,startDate);
        }
        if (endDate != null) {
            query.le(BusinessCancelApply::getCancelApplyTime,startDate);
        }
        query.orderByDesc(BusinessCancelApply::getId);
        if (!StringUtils.isEmpty(account)) {
            List<Long> ids = memberFeign.findMemberIdsByAccount(account);
            if(ids!=null && ids.size()>0){
                query.in(BusinessCancelApply::getMemberId,ids);
            }else {
                return page;
            }
        }
        Page<BusinessCancelApply> list = this.page(page,query);
        List<Long> memberIds = list.getRecords().stream()
                .map(BusinessCancelApply::getMemberId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Member> memberMap = memberFeign.mapByMemberIds(memberIds);

        for (BusinessCancelApply businessCancelApply : list.getRecords()) {
            DepositRecord depositRecord = depositRecordService.getById(businessCancelApply.getDepositRecordId());
            businessCancelApply.setDepositRecord(depositRecord);
            businessCancelApply.setMember(memberMap.get(businessCancelApply.getMemberId()));
        }
        return list;
    }

    @Override
    public Map<String, Object> getBusinessOrderStatistics(Long memberId) {
        return this.baseMapper.getBusinessStatistics(memberId);
    }

    @Override
    public Map<String, Object> getBusinessAppealStatistics(Long memberId) {
        Map<String,Object> map = new HashedMap();
        Long complainantNum = this.baseMapper.getBusinessAppealInitiatorIdStatistics(memberId);
        Long defendantNum = this.baseMapper.getBusinessAppealAssociateIdStatistics(memberId);
        map.put("defendantNum",defendantNum);
        map.put("complainantNum",complainantNum);
        return map ;
    }

    @Override
    public Integer countAuditing() {
        LambdaQueryWrapper<BusinessCancelApply> query = new LambdaQueryWrapper<>();
        query.eq(BusinessCancelApply::getStatus,CertifiedBusinessStatus.CANCEL_AUTH);
        return this.count(query);
    }
}
