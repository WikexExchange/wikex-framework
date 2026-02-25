package com.wikex.wikex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.user.entity.PaymentTypeRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author zhujunjun
 */
public interface PaymentTypeRecordMapper extends BaseMapper<PaymentTypeRecord> {
    List<PaymentTypeRecord> getRecordsByUserId(@Param("userId") Long userId);
}