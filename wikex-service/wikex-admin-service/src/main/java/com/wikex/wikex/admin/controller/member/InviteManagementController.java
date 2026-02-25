package com.wikex.wikex.admin.controller.member;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.InviteManagementScreen;
import com.wikex.wikex.screen.MemberInviteStasticScreen;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberInviteStastic;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberInviteStasticFeign;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("invite/management")
public class InviteManagementController extends BaseController {

    @Autowired
    private MemberFeign memberFeign;
    
    @Autowired
    private MemberInviteStasticFeign memberInviteStasticFeign;

//    /**
//     * Invite management default query for all users
//     *
//     * @return
//     */
//    @RequiresPermissions("invite:management:query")
//    @AccessLog(module = AdminModule.CMS, operation = "Invite management default query for all users")
//    @RequestMapping(value = "look", method = RequestMethod.POST)
//    public MessageResult lookAll(@RequestBody InviteManagementScreen screen) {
//        
//        Page<Member> page = memberFeign.lookAll(screen);
//        return success(IPage2Page(page));
//    }

    /**
     * Conditional query
     */
    @RequiresPermissions("invite:management:query")
    @AccessLog(module = AdminModule.CMS, operation = "Invite management multi-condition query")
    @RequestMapping(value = "query", method = RequestMethod.POST)
    public MessageResult queryCondition(@RequestBody InviteManagementScreen screen) {
        
        Page<Member> page = memberFeign.lookAll(screen);
        return success(page);
    }

    /**
     * Query first-level and second-level users by ID
     */
    @AccessLog(module = AdminModule.CMS, operation = "Query first-level and second-level users by ID")
    @RequestMapping(value = "info", method = RequestMethod.POST)
    public MessageResult queryFirstAndSecondById(@RequestBody InviteManagementScreen screen) {
        
        Page<Member> page = memberFeign.queryFirstAndSecondById(screen);
        return success(page);
    }

    @RequiresPermissions("invite:management:rank")
    @AccessLog(module = AdminModule.CMS, operation = "Invite ranking conditional query")
    @RequestMapping(value = "rank", method = RequestMethod.POST)
    public MessageResult queryRankList(@RequestBody MemberInviteStasticScreen screen) {
    	// type: 0 = ranking by number of people   // type: 1 = ranking by commission
    	Page<MemberInviteStastic> page = memberInviteStasticFeign.queryRankList(screen);
    	List<MemberInviteStastic> list= page.getRecords();
    	for(MemberInviteStastic item : list) {
    		item.setUserIdentify(item.getIsRobot() + "-" + item.getUserIdentify());
    	}
    	return success(page);
    }
//    @RequiresPermissions("invite:management:update-rank")
//    @AccessLog(module = AdminModule.CMS, operation = "Update invitation information")
//    @PostMapping("update-rank")
//    public MessageResult updateRank(@RequestParam("id") Long id,
//    								@RequestParam("estimatedReward") BigDecimal estimatedReward,
//    								@RequestParam("extraReward") BigDecimal extraReward,
//    								@RequestParam("levelOne") Integer levelOne,
//    								@RequestParam("levelTwo") Integer levelTwo) {
//    	
//    	MemberInviteStastic detail = memberInviteStasticService.findById(id);
//    	if(detail == null) {
//    		return error("This ranked user does not exist");
//    	}
//    	if(estimatedReward != null) {
//    		detail.setEstimatedReward(estimatedReward);
//    	}
//    	if(extraReward != null) {
//    		detail.setExtraReward(extraReward);
//    	}
//    	if(levelOne != null) {
//    		detail.setLevelOne(levelOne);
//    	}
//    	if(levelTwo != null) {
//    		detail.setLevelTwo(levelTwo);
//    	}
//
//    	memberInviteStasticService.save(detail);
//
//    	return success(detail);
//    }
//    @RequiresPermissions("invite:management:detail-rank")
//    @AccessLog(module = AdminModule.CMS, operation = "Invitation information detail")
//    @RequestMapping(value = "detail-rank", method = RequestMethod.POST)
//    public MessageResult updateRank(@RequestParam(value = "id", defaultValue="0") Long id) {
//
//    	MemberInviteStastic detail = memberInviteStasticService.findById(id);
//
//    	return success(detail);
//    }
}
