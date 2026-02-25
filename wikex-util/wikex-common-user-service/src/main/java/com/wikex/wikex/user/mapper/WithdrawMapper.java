package com.wikex.wikex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.WithdrawScreen;
import com.wikex.wikex.user.entity.Withdraw;
import com.wikex.wikex.user.vo.WithdrawVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface WithdrawMapper extends BaseMapper<Withdraw> {

    Page<Withdraw> joinFind(WithdrawScreen screen);

    List<WithdrawVO> getWithdrawStatistics(@Param("dateStr") String dateStr);
}
