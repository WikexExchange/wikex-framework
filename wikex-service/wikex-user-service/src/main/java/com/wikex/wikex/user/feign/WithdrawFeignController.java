package com.wikex.wikex.user.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.WithdrawScreen;
import com.wikex.wikex.user.entity.Withdraw;
import com.wikex.wikex.user.service.WithdrawService;
import com.wikex.wikex.user.vo.WithdrawVO;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Api(tags = "Withdrawal")
@RestController
@Slf4j
@RequestMapping(value = "/withdrawFeign", method = RequestMethod.POST)
public class WithdrawFeignController extends BaseController {

    @Autowired
    private WithdrawService withdrawService;

    @PostMapping("findAllOut")
    public List<Withdraw> findAllOut(@RequestBody WithdrawScreen withdrawScreen){
        return withdrawService.findAllOut(withdrawScreen);
    }


    @PostMapping("findAll")
    public Page<Withdraw> findAll(@RequestBody WithdrawScreen withdrawScreen){
        return withdrawService.findAll(withdrawScreen);
    }

    @PostMapping("findOne")
    public Withdraw findOne(@RequestParam("id") Long id){
        return withdrawService.getById(id);
    }

    @PostMapping("updateById")
    public MessageResult updateById(@RequestBody Withdraw update){
        withdrawService.updateById(update);
        return success();
    }

    @PostMapping("joinFind")
    public Page<Withdraw> joinFind(@RequestBody WithdrawScreen withdrawScreen){
        return withdrawService.joinFind(withdrawScreen);
    }

    @PostMapping("getWithdrawStatistics")
    public List<WithdrawVO> getWithdrawStatistics(@RequestParam("dateStr")String dateStr){
        return withdrawService.getWithdrawStatistics(dateStr);
    }

    @PostMapping("countAuditing")
    public Integer countAuditing(){
        return withdrawService.countAuditing();
    }

    @PostMapping("withdrawSuccess")
    public void withdrawSuccess(@RequestParam("withdrawId")Long withdrawId, @RequestParam("txid")String txid){
        withdrawService.withdrawSuccess(withdrawId,txid);
    }
    @PostMapping("withdrawFail")
    public void withdrawFail(@RequestParam("withdrawId")Long withdrawId){
        withdrawService.withdrawFail(withdrawId);
    }

    @PostMapping("findWithdrawByStatus")
    public List<Withdraw> findWithdrawByStatus(@RequestParam("status") Integer status){
       return withdrawService.findWithdrawByStatus(status);
    }

    @PostMapping("updateWithdrawStatus")
    void updateWithdrawStatus(@RequestParam("id")Long id, @RequestParam("status") Integer status){
        withdrawService.updateWithdrawStatus(id,status);
    }

}
