package com.wikex.wikex.admin.mapper;

import com.wikex.wikex.admin.entity.Announcement;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
* Announcement table Mapper interface
 * </p>
 *
 * @author markchao
 * @since 2021-08-20
 */
public interface AnnouncementMapper extends BaseMapper<Announcement> {
    @Select("select * from announcement where id < #{id}  AND is_show=1 AND lang = #{lang}  ORDER by id desc limit 0,1")
    Announcement getBack(@Param("id") long id, @Param("lang") String lang);

    @Select("select * from announcement where id > #{id} AND is_show=1 AND lang = #{lang} ORDER by id asc limit 0,1")
    Announcement getNext(@Param("id") long id, @Param("lang") String lang);

    @Select("select max(s.sort) from announcement s")
    int getMaxSort();
}
