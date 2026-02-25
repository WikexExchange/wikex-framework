package com.wikex.wikex.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.constant.CertifiedBusinessStatus;
import com.wikex.wikex.constant.CommonStatus;
import com.wikex.wikex.exception.AuthenticationException;
import com.wikex.wikex.exception.GAException;
import com.wikex.wikex.screen.InviteManagementScreen;
import com.wikex.wikex.screen.MemberScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.mapper.MemberMapper;
import com.wikex.wikex.user.service.MemberService;
import com.wikex.wikex.util.GoogleAuthenticatorUtil;
import com.wikex.wikex.util.EmailUtil;
import com.wikex.wikex.util.MD5;
import com.wikex.wikex.util.PasswordCheckUtil;
import com.wikex.wikex.util.PasswordHasherUtil;
import com.wikex.wikex.util.PasswordHasherUtil.PasswordVerificationResult;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    @Autowired
    private LocaleMessageSourceService messageSourceService;

    @Override
    public Member login(String username, String password) throws Exception {
        Member member = baseMapper.findMemberByMobilePhoneOrEmail(username, username);
        if (member == null) {
            throw new AuthenticationException(messageSourceService.getMessage("USER_PASSWORD_ERROR"));
        } else if (!MD5.md5(password + member.getSalt()).toLowerCase().equals(member.getPassword())) {
            throw new AuthenticationException(messageSourceService.getMessage("USER_PASSWORD_ERROR"));
        } else if (member.getStatus().equals(CommonStatus.ILLEGAL)) {
            throw new AuthenticationException(messageSourceService.getMessage("ACCOUNT_ACTIVATION_DISABLED"));
        }
        return member;
    }

    @Override
    public boolean usernameIsExist(String username) {
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        int count = this.count(queryWrapper);
        if (count > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean emailIsExist(String email) {
        email = EmailUtil.normalize(email);
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        int count = this.count(queryWrapper);
        if (count > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean walletAddressIsExist(String address) {
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reward_wallet_address", address);
        int count = this.count(queryWrapper);
        if (count > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Member findMemberByPromotionCode(String promotionCode) {
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("promotion_code", promotionCode);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean phoneIsExist(String phone) {
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile_phone", phone);
        int count = this.count(queryWrapper);
        if (count > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean userPromotionCodeIsExist(String promotion) {
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("promotion_code", promotion);
        int count = this.count(queryWrapper);
        if (count > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Member findByEmail(String account) {
        account = EmailUtil.normalize(account);
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", account);
        return this.getOne(queryWrapper);
    }

    @Override
    public Member findByGoogleSub(String googleSub) {
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("google_sub", googleSub);
        return this.getOne(queryWrapper);
    }

    @Override
    public Member findByAppleSub(String appleSub) {
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("apple_sub", appleSub);
        return this.getOne(queryWrapper);
    }

    @Override
    public Member findByPhone(String account) {
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile_phone", account);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<Member> findSuperPartnerMembersByIds(String upper) {
        String[] idss = upper.split(",");
        List<Long> ids = new ArrayList<>();
        for (String id : idss) {
            ids.add(Long.parseLong(id));
        }
        return baseMapper.findSuperPartnerMembersByIds(ids);
    }

    @Override
    public IPage<Member> findPromotionMemberPage(Integer pageNo, Integer pageSize, long id) {
        IPage<Member> page = new Page<>(pageNo, pageSize);
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("inviter_id", id);
        queryWrapper.orderByAsc("id");
        return this.page(page, queryWrapper);
    }

    @Override
    public Page<Member> findAll(MemberScreen screen, Integer pageNo, Integer pageSize) {
        Page<Member> page = new Page<>(pageNo, pageSize);
        QueryWrapper<Member> queryWrapper = getQueryWrapper(screen);
        return this.page(page, queryWrapper);

    }

    @Override
    public Page<Member> lookAll(InviteManagementScreen screen) {
        Page<Member> page = new Page<>(screen.getPageNo(), screen.getPageSize());
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(screen.getRealName())) {
            queryWrapper.eq("real_name", screen.getRealName());
        }
        if (StringUtils.isNotEmpty(screen.getMobilePhone())) {
            queryWrapper.eq("mobile_phone", screen.getMobilePhone());
        }
        if (StringUtils.isNotEmpty(screen.getEmail())) {
            queryWrapper.eq("email", screen.getEmail());
        }
        queryWrapper.orderByDesc("registration_time");
        return this.page(page, queryWrapper);
    }

    @Override
    public List<Member> findAllWithCondition(MemberScreen screen) {
        LambdaQueryWrapper<Member> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Member::getStatus, screen.getStatus());
        queryWrapper.gt(Member::getRegistrationTime, screen.getStartTime());
        queryWrapper.lt(Member::getRegistrationTime, screen.getEndTime());
        queryWrapper.gt(Member::getRegistrationTime, screen.getStartTime());
        queryWrapper.like(Member::getUsername, screen.getAccount())
                .or().like(Member::getMobilePhone, screen.getAccount())
                .or().like(Member::getEmail, screen.getAccount());
        return this.list(queryWrapper);
    }

    @Override
    public Page<Member> queryFirstAndSecondById(InviteManagementScreen screen) {
        Page<Member> page = new Page<>(screen.getPageNo(), screen.getPageSize());
        Page<Member> firstAndSecondMembers = this.baseMapper.queryFirstAndSecondById(page, screen.getId());
        return firstAndSecondMembers;
    }

    @Override
    public List<Member> findPromotionMember(Long id) {
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("inviter_id", id);
        return this.list(queryWrapper);
    }

    public List<Long> findPromotionMemberIds(Long inviter_id) {
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id").eq("inviter_id", inviter_id);
        List<Member> members = this.list(queryWrapper);
        List<Long> list = new ArrayList<>();
        for (Member member : members) {
            list.add(member.getId());
        }
        return list;
    }

    @Override
    public List<Long> findMemberIdsByAccount(String account) {
        List<Long> list = new ArrayList<>();
        LambdaQueryWrapper<Member> memberQuery = new LambdaQueryWrapper<>();
        if (!"".equals(account)) {
            memberQuery.and(
                    wrapper -> wrapper.like(Member::getUsername, account).or().like(Member::getMobilePhone, account)
                            .or().like(Member::getEmail, account).or().like(Member::getRealName, account));
            List<Member> members = this.list(memberQuery);
            if (members != null) {
                for (Member member : members) {
                    list.add(member.getId());
                }
            }
        }
        return list;
    }

    @Override
    public List<Long> findMemberIdsByAccountAndNotCertified(String account) {
        List<Long> list = new ArrayList<>();
        LambdaQueryWrapper<Member> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.ne(Member::getCertifiedBusinessStatus, CertifiedBusinessStatus.NOT_CERTIFIED);
        if (!"".equals(account)) {
            memberQuery.and(
                    wrapper -> wrapper.like(Member::getUsername, account).or().like(Member::getMobilePhone, account)
                            .or().like(Member::getEmail, account).or().like(Member::getRealName, account));
        }
        List<Member> members = this.list(memberQuery);
        if (members != null) {
            for (Member member : members) {
                list.add(member.getId());
            }
        }
        return list;
    }

    @Override
    public Map<Long, Member> mapByMemberIds(List<Long> ids) {
        Map<Long, Member> map = new HashMap<>();
        LambdaQueryWrapper<Member> query = new LambdaQueryWrapper<>();
        query.in(Member::getId, ids);
        List<Member> allByIdIn = this.list(query);
        allByIdIn.forEach(v -> {
            map.put(v.getId(), v);
        });

        return map;
    }

    @Override
    public Member findByUsername(String username) {
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return this.getOne(queryWrapper);
    }

    @Override
    public Member loginWithPassword(String username, String password, String country) throws Exception {
        Member member = baseMapper.findMemberByMobilePhoneOrEmail(username, username);

        if (!PasswordCheckUtil.isCorrectPassword(password, member.getPassword(), member.getSalt())) {
            throw new AuthenticationException(messageSourceService.getMessage("USER_PASSWORD_ERROR"));
        }

        if (!StringUtils.isEmpty(country) && member != null) {
            if (!member.getCountry().equals(country)) {
                throw new AuthenticationException(messageSourceService.getMessage("USER_PASSWORD_ERROR"));
            }
        }
        if (member == null) {
            throw new AuthenticationException(messageSourceService.getMessage("USER_PASSWORD_ERROR"));
        } else if (member.getStatus().equals(CommonStatus.ILLEGAL.getCode())) {
            throw new AuthenticationException(messageSourceService.getMessage("ACCOUNT_ACTIVATION_DISABLED"));
        }
        return member;
    }

    @Override
    public int getRegistrationNum(String dateStr) {
        return this.baseMapper.getRegistrationNum(dateStr);
    }

    @Override
    public int getBussinessNum(String dateStr) {
        return this.baseMapper.getBussinessNum(dateStr);
    }

    @Override
    public int getApplicationNum(String dateStr) {
        return this.baseMapper.getApplicationNum(dateStr);
    }

    @Override
    public Date getStartRegistrationDate() {
        return this.baseMapper.getStartRegistrationDate();
    }

    @Override
    public Member findById(Long memberId) {
        return this.getById(memberId);
    }

    private QueryWrapper<Member> getQueryWrapper(MemberScreen screen) {
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        if (screen.getStatus() != null) {
            queryWrapper.eq("certified_business_status", screen.getStatus());
        }
        if (screen.getStartTime() != null) {
            queryWrapper.ge("registration_time", screen.getStartTime());
        }
        if (screen.getInviterId() != null) {
            queryWrapper.ge("inviter_id", screen.getInviterId());
        }
        if (screen.getEndTime() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(screen.getEndTime());
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            queryWrapper.lt("registration_time", calendar.getTime());
        }

        if (!StringUtils.isEmpty(screen.getAccount())) {
            Long accountId = coverToLong(screen.getAccount());
            if (accountId == null) {
                queryWrapper.and(
                        wrapper -> wrapper.like("username", screen.getAccount())
                                .or().likeRight("mobile_phone", screen.getAccount())
                                .or().likeRight("email", screen.getAccount())
                                .or().like("real_name", screen.getAccount()));
            } else {
                queryWrapper.and(
                        wrapper -> wrapper.like("username", screen.getAccount())
                                .or().likeRight("mobile_phone", screen.getAccount())
                                .or().likeRight("email", screen.getAccount())
                                .or().eq("id", accountId)
                                .or().like("real_name", screen.getAccount()));
            }
        }
        if (screen.getCommonStatus() != null) {
            queryWrapper.eq("status", screen.getCommonStatus());
        }

        if (screen.getSuperPartner() != null && !screen.getSuperPartner().equals("")) {
            queryWrapper.eq("super_partner", screen.getSuperPartner());
        }
        return queryWrapper;
    }

    private Long coverToLong(String str) {
        try {
            Long num = Long.valueOf(str);
            return num;
        } catch (Exception e) {
            return null;
        }
    }

}
