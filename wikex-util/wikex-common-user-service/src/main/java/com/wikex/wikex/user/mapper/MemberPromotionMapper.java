package com.wikex.wikex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.user.entity.MemberPromotion;
import com.wikex.wikex.user.vo.MemberPromotionStasticVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;


public interface MemberPromotionMapper extends BaseMapper<MemberPromotion> {

    @Select("select *, count(*) as count from member_promotion where level = #{level} and create_time > #{startDate} and create_time < #{endDate} group by (inviter_id) order by count desc limit #{topCount}")
    List<MemberPromotionStasticVO> getInviteGroupByTypeAndDate(@Param("level") int level, @Param("startDate") Date startDate, @Param("endDate")Date endDate, @Param("topCount")int topCount);

    @Select("select * from member_promotion where invitees_id = #{inviterId}")
    List<MemberPromotion> findAllParentByUserId(@Param("inviterId") long inviterId);
}
