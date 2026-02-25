package com.wikex.wikex.user.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.MemberDepositScreen;
import com.wikex.wikex.user.entity.MemberDeposit;
import com.wikex.wikex.user.service.MemberDepositService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "Member recharge digital currency record")
@RestController
@RequestMapping("/memberDepositFeign")
public class MemberDepositFeignController {

    @Autowired
    private MemberDepositService memberDepositService;

    @PostMapping("/findAll")
    public Page<MemberDeposit> findAll(@RequestBody MemberDepositScreen screen) {
        return memberDepositService.findAll(screen);
    }

    @PostMapping("getDepositStatistics")
    public List<MemberDeposit> getDepositStatistics(@RequestParam("dateStr")String dateStr){
        return memberDepositService.getDepositStatistics(dateStr);
    }
}
