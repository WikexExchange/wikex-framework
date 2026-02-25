package com.wikex.wikex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.user.entity.Member;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;


public interface MemberMapper extends BaseMapper<Member> {

    @Select("select * from member where mobile_phone = #{username} or email = #{email} ")
    public Member findMemberByMobilePhoneOrEmail(@Param("username") String username, @Param("email")String email);

    List<Member> findSuperPartnerMembersByIds(@Param("ids") List<Long> ids);

    Page<Member> queryFirstAndSecondById(Page<Member> page, @Param("id")Long id);

    int getRegistrationNum(@Param("dateStr")String dateStr);

    int getBussinessNum(@Param("dateStr")String dateStr);

    int getApplicationNum(@Param("dateStr")String dateStr);

    Date getStartRegistrationDate();
}
