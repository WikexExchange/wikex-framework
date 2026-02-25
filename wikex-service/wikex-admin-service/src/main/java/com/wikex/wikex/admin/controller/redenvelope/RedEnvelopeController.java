package com.wikex.wikex.admin.controller.redenvelope;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.RedEnvelope;
import com.wikex.wikex.user.entity.RedEnvelopeDetail;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.user.feign.RedEnvelopDetailFeign;
import com.wikex.wikex.user.feign.RedEnvelopeFeign;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.GeneratorUtil;
import com.wikex.wikex.util.MessageResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Red Envelope Management
 * @author Hevin
 *
 */
@RestController
@RequestMapping("/envelope")
public class RedEnvelopeController extends BaseController {
	@Autowired
	private RedEnvelopeFeign redEnveloperService;

	@Autowired
	private RedEnvelopDetailFeign redEnvelopDetailFeign;

	@Autowired
    private LocaleMessageSourceService messageSource;
	
	@Autowired
	private CoinFeign coinService;
	
	/**
	 * Paginated list of red envelopes
	 * @param pageParam
	 * @return
	 */
	@RequiresPermissions("envelope:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.REDENVELOPE, operation = "Paginated red envelope list RedEnvelopeController")
    public MessageResult envelopeList(PageParam pageParam) {
        Page<RedEnvelope> all = redEnveloperService.findAll(pageParam);
        return success(all);
    }
	
	/**
	 * Red envelope details
	 * @param id
	 * @return
	 */
	@RequiresPermissions("envelope:detail")
	@GetMapping("{id}/detail")
    @AccessLog(module = AdminModule.REDENVELOPE, operation = "View red envelope details RedEnvelopeController")
    public MessageResult envelopeDetail(@PathVariable Long id) {
		RedEnvelope redEnvelope = redEnveloperService.findOne(id);
		Assert.notNull(redEnvelope, "validate id!");
        return success(redEnvelope);
    }
	
	/**
	 * Paginated receiving details
	 * @param envelopeId
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	@RequiresPermissions("envelope:receive-detail")
    @PostMapping("receive-detail")
    @AccessLog(module = AdminModule.REDENVELOPE, operation = "View red envelope receiving details RedEnvelopeController")
    public MessageResult envelopeDetailList(@RequestParam(value = "envelopeId", defaultValue = "0") Long envelopeId,
    		@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
    		@RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
		
		Page<RedEnvelopeDetail> detailList = redEnvelopDetailFeign.findByEnvelope(envelopeId, pageNo, pageSize);

        return success(detailList);
    }
	
	/**
	 * Create new red envelope
	 * @param redEnvelope
	 * @return
	 */
	@RequiresPermissions("envelope:add")
    @PostMapping("add")
    @AccessLog(module = AdminModule.REDENVELOPE, operation = "Add new red envelope RedEnvelopeController")
    public MessageResult addRedEnvelope(
            @Valid RedEnvelope redEnvelope) {
		// Check if currency exists
		Coin coin = coinService.findByUnit(redEnvelope.getUnit());
		Assert.notNull(coin, messageSource.getMessage("INVALID_CURRENCY"));
		
		// Generate red envelope number
		SimpleDateFormat f = new SimpleDateFormat("MMddHHmmss");
		redEnvelope.setEnvelopeNo(f.format(new Date()) + GeneratorUtil.getNonceString(5).toUpperCase());
		
		redEnvelope.setMemberId(1L); // Fixed to user ID 1 for platform-issued red envelopes
		redEnvelope.setPlateform(1); // Fixed to 1 for platform-issued red envelopes
		redEnvelope.setState(0);
		redEnvelope.setReceiveAmount(BigDecimal.ZERO);
		redEnvelope.setReceiveCount(0);
		
		redEnvelope.setCreateTime(DateUtil.getCurrentDate());
		redEnvelope = redEnveloperService.save(redEnvelope);
        return MessageResult.getSuccessInstance(messageSource.getMessage("SUCCESS"), redEnvelope);
    }
	
	/**
	 * Modify red envelope
	 * @param id
	 * @param type
	 * @param invite
	 * @param unit
	 * @param maxRand
	 * @param totalAmount
	 * @param count
	 * @param logoImage
	 * @param bgImage
	 * @param name
	 * @param detail
	 * @param expiredHours
	 * @param state
	 * @return
	 */
	@RequiresPermissions("envelope:modify")
    @PostMapping("modify")
    @AccessLog(module = AdminModule.REDENVELOPE, operation = "Modify red envelope RedEnvelopeController")
    public MessageResult modifyRedEnvelope(
    		@RequestParam("id") Long id,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "invite", required = false) Integer invite,
            @RequestParam(value = "unit", required = false) String unit,
            @RequestParam(value = "maxRand", required = false) BigDecimal maxRand,
            @RequestParam(value = "totalAmount", required = false) BigDecimal totalAmount,
            @RequestParam(value = "count", required = false) Integer count,
            @RequestParam(value = "logoImage", required = false) String logoImage,
            @RequestParam(value = "bgImage", required = false) String bgImage,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "detail", required = false) String detail,
            @RequestParam(value = "expiredHours", required = false) Integer expiredHours,
            @RequestParam(value = "state", required = false) Integer state) {
		
		RedEnvelope redEnvelope = redEnveloperService.findOne(id);
		notNull(redEnvelope, "Validate Red Envelope!");
		
		if(type != null) redEnvelope.setType(type);
		if(invite != null) redEnvelope.setInvite(invite);
		if(unit != null) {
			// Check if currency exists
			Coin coin = coinService.findByUnit(redEnvelope.getUnit());
			Assert.notNull(coin, messageSource.getMessage("INVALID_CURRENCY"));
			redEnvelope.setUnit(unit);
		};
		if(maxRand != null) redEnvelope.setMaxRand(maxRand);
		if(totalAmount != null) redEnvelope.setTotalAmount(totalAmount);
		if(count != null) redEnvelope.setCount(count);
		if(logoImage != null) redEnvelope.setLogoImage(logoImage);
		if(bgImage != null) redEnvelope.setBgImage(bgImage);
		if(name != null) redEnvelope.setName(name);
		if(detail != null) redEnvelope.setDetail(detail);
		if(expiredHours != null) redEnvelope.setExpiredHours(expiredHours);
		if(state != null) redEnvelope.setState(state);
		
		redEnvelope = redEnveloperService.save(redEnvelope);
		
        return MessageResult.getSuccessInstance(messageSource.getMessage("SUCCESS"), redEnvelope);
    }
}
