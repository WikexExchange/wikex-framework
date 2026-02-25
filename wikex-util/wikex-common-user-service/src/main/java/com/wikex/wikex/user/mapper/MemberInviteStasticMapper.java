package com.wikex.wikex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.user.entity.MemberInviteStastic;
import com.wikex.wikex.user.entity.MemberInviteStasticRank;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface MemberInviteStasticMapper extends BaseMapper<MemberInviteStastic> {

    List<MemberInviteStasticRank> topInviteCountByType(@Param("type") Integer type, @Param("count") Integer count);

    List<MemberInviteStastic> getTopTotalAmount(@Param("count") Integer count);

    List<MemberInviteStastic> getTopInviteCount(@Param("count") Integer count);
}
