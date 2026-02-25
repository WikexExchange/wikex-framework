package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.InviteManagementScreen;
import com.wikex.wikex.screen.MemberScreen;
import com.wikex.wikex.user.entity.Member;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface MemberService extends IService<Member> {

    public Member login(String username, String password) throws Exception;

    boolean usernameIsExist(String username);

    boolean emailIsExist(String email);

    boolean walletAddressIsExist(String address);

    Member findMemberByPromotionCode(String promotionCode);

    boolean phoneIsExist(String phone);

    boolean userPromotionCodeIsExist(String promotion);

    Member findByEmail(String account);

    Member findByGoogleSub(String googleSub);

    Member findByAppleSub(String appleSub);

    Member findByPhone(String account);

    List<Member> findSuperPartnerMembersByIds(String upper);

    IPage<Member> findPromotionMemberPage(Integer pageNo, Integer pageSize, long id);

    Page<Member> findAll(MemberScreen screen, Integer pageNo, Integer pageSize);

    Page<Member> lookAll(InviteManagementScreen screen);

    List<Member> findAllWithCondition(MemberScreen screen);

    Page<Member> queryFirstAndSecondById(InviteManagementScreen screen);

    List<Member> findPromotionMember(Long id);

    List<Long> findPromotionMemberIds(Long inviter_id);

    List<Long> findMemberIdsByAccount(String account);

    List<Long> findMemberIdsByAccountAndNotCertified(String account);

    Map<Long, Member> mapByMemberIds(List<Long> ids);

    Member findByUsername(String name);

    Member loginWithPassword(String username, String password, String country) throws Exception;

    int getRegistrationNum(String dateStr);

    int getBussinessNum(String dateStr);

    int getApplicationNum(String dateStr);

    Date getStartRegistrationDate();

    Member findById(Long memberId);
}
