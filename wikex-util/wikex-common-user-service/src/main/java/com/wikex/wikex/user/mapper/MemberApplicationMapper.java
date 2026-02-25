package com.wikex.wikex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.MemberApplicationScreen;
import com.wikex.wikex.user.entity.MemberApplication;
import com.wikex.wikex.user.vo.MemberApplicationVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


public interface MemberApplicationMapper extends BaseMapper<MemberApplication> {

    @Select("select count(1) from member_application where  id_card = #{idCard}  and audit_status=0")
    int queryByIdCard(@Param("idCard") String idCard);

    Page<MemberApplicationVo> findAll(Page<MemberApplication> page,@Param("screen")MemberApplicationScreen screen);
}
