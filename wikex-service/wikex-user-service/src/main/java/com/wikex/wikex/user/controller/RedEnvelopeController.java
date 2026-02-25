package com.wikex.wikex.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.sms.SMSProvider;
import com.wikex.wikex.user.entity.*;
import com.wikex.wikex.user.event.MemberEvent;
import com.wikex.wikex.user.service.*;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.user.vo.MemberPromotionStasticVO;
import com.wikex.wikex.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

@RestController
@RequestMapping(value = "/redenvelope")
public class RedEnvelopeController extends BaseController {

	@Autowired
	private RedEnvelopeService redEnvelopeService;

	@Autowired
	private RedEnvelopeDetailService redEnvelopeDetailService;

	@Autowired
	private MemberPromotionService memberPromotionService;

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberWalletService memberWalletService;

	@Autowired
	private MemberTransactionService memberTransactionService;

	@Resource
	private LocaleMessageSourceService localeMessageSourceService;

	@Autowired
	private CoinService coinService;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorkByTwitter idWorkByTwitter;

	@Autowired
	private MemberEvent memberEvent;

	@Autowired
	private SMSProvider smsProvider;

	@Autowired
	private CountryService countryService;

	private Random rand = new Random();

	@RequestMapping(value = "/query")
	private MessageResult envelopeDetail(@RequestParam(value = "envelopeNo", defaultValue = "") String envelopeNo,
			@RequestParam(value = "code", defaultValue = "") String code) {
		Assert.notNull(envelopeNo, localeMessageSourceService.getMessage("INVALID_RED_ENVELOPE"));
		RedEnvelope redEnvelope = redEnvelopeService.findByEnvelopeNo(envelopeNo);
		Assert.notNull(redEnvelope, localeMessageSourceService.getMessage("INVALID_RED_ENVELOPE"));
		if (StringUtils.hasText(code)) {
			Member member = memberService.findMemberByPromotionCode(code);
			if (member != null) {
				if (StringUtils.hasText(member.getMobilePhone())) {
					redEnvelope.setInviteUser(
							member.getMobilePhone().substring(0, 3) + "****" + member.getMobilePhone().substring(7));
				}
				redEnvelope.setInviteUserAvatar(member.getAvatar());
			}
		}
		return success(redEnvelope);
	}

	@PostMapping("/query-detail")
	@AccessLog(module = AdminModule.REDENVELOPE, operation = "View red envelope receive details RedEnvelopeController")
	public MessageResult envelopeDetailList(@RequestParam(value = "envelopeId", defaultValue = "0") Long envelopeId,
			@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {

		Page<RedEnvelopeDetail> detailList = redEnvelopeDetailService.findByEnvelope(envelopeId, pageNo, pageSize);
		for (int i = 0; i < detailList.getRecords().size(); i++) {
			detailList.getRecords().get(i).setUserIdentify(
					detailList.getRecords().get(i).getUserIdentify().substring(0, 3)
							+ "****"
							+ detailList.getRecords().get(i).getUserIdentify().substring(7, 11));
		}
		return success(IPage2Page(detailList));
	}

	@PermissionOperation
	@RequestMapping(value = "/myenvelope")
	private MessageResult envelopeList(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
			@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
		AuthMember member = AuthMember.toAuthMember(authMember);
		Assert.notNull(member, localeMessageSourceService.getMessage("INVALID_USER"));
		Page<RedEnvelope> redEnvelopeList = redEnvelopeService.findByMember(member.getId(), pageNo, pageSize);
		Assert.notNull(redEnvelopeList, localeMessageSourceService.getMessage("NO_RED_ENVELOPE"));

		return success(IPage2Page(redEnvelopeList));
	}

	@PermissionOperation
	@RequestMapping(value = "/myreceive")
	private MessageResult envelopeList(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
			@RequestParam(value = "envelopeNo", defaultValue = "") String envelopeNo) {
		AuthMember member = AuthMember.toAuthMember(authMember);
		Assert.notNull(envelopeNo, localeMessageSourceService.getMessage("INVALID_RED_ENVELOPE"));
		RedEnvelope redEnvelope = redEnvelopeService.findByEnvelopeNo(envelopeNo);
		Assert.notNull(redEnvelope, localeMessageSourceService.getMessage("INVALID_RED_ENVELOPE"));

		Member auth = memberService.getById(member.getId());
		Assert.notNull(auth, localeMessageSourceService.getMessage("ILLEGAL_OPERATION"));

		List<RedEnvelopeDetail> detailList = redEnvelopeDetailService.findByEnvelopeIdAndMemberId(redEnvelope.getId(),
				member.getId());

		return success(detailList);
	}

	@PermissionOperation
	@RequestMapping(value = "/lefttimes")
	private MessageResult leftTimes(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
			@RequestParam(value = "envelopeNo", defaultValue = "") String envelopeNo) {
		AuthMember member = AuthMember.toAuthMember(authMember);
		int times = 0;

		Assert.notNull(envelopeNo, localeMessageSourceService.getMessage("INVALID_RED_ENVELOPE"));
		RedEnvelope redEnvelope = redEnvelopeService.findByEnvelopeNo(envelopeNo);
		Assert.notNull(redEnvelope, localeMessageSourceService.getMessage("INVALID_RED_ENVELOPE"));

		Member authMem = memberService.getById(member.getId());
		Assert.notNull(authMem, localeMessageSourceService.getMessage("ILLEGAL_OPERATION"));

		if (redEnvelope.getState() == 1) {
			return error(localeMessageSourceService.getMessage("RED_ENVELOPE_FULLY_CLAIMED"));
		}

		if (redEnvelope.getState() == 2) {
			return error(localeMessageSourceService.getMessage("RED_ENVELOPE_EXPIRED"));
		}

		if (redEnvelope.getState() == 0) {
			long currentTime = Calendar.getInstance().getTimeInMillis();
			if (currentTime >= (redEnvelope.getCreateTime().getTime()
					+ redEnvelope.getExpiredHours() * 60 * 60 * 1000)) {
				if (redEnvelope.getReceiveCount() < redEnvelope.getCount()) {
					return error(localeMessageSourceService.getMessage("RED_ENVELOPE_EXPIRED"));
				} else {
					return error(localeMessageSourceService.getMessage("RED_ENVELOPE_FULLY_CLAIMED"));
				}
			}
			if (redEnvelope.getReceiveCount() >= redEnvelope.getCount()) {
				return error(localeMessageSourceService.getMessage("RED_ENVELOPE_FULLY_CLAIMED"));
			}
		}

		List<RedEnvelopeDetail> detailList = redEnvelopeDetailService.findByEnvelopeIdAndMemberId(redEnvelope.getId(),
				member.getId());
		if (detailList != null && detailList.size() > 0) {
			if (redEnvelope.getInvite() == 0) {
				return success(0);
			}
			if (redEnvelope.getInvite() == 1) {
				Date endDate = new Date();
				List<MemberPromotionStasticVO> inviteList = memberPromotionService.getDateRangeRank(0,
						redEnvelope.getCreateTime(), endDate, 10000);
				if (inviteList.size() <= detailList.size()) {
					return success(0);
				} else {
					return success(inviteList.size() - detailList.size());
				}
			}
		} else {
			if (redEnvelope.getInvite() == 0) {
				return success(1);
			}
			if (redEnvelope.getInvite() == 1) {
				Date endDate = new Date();
				List<MemberPromotionStasticVO> inviteList = memberPromotionService.getDateRangeRank(0,
						redEnvelope.getCreateTime(), endDate, 10000);
				if (inviteList.size() == 0) {
					return success(1);
				} else {
					return success(inviteList.size() + 1);
				}
			}
		}
		return success(0);
	}

	@PermissionOperation
	@RequestMapping(value = "/receivelogin")
	private MessageResult receiveEnvelopeLogin(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
			@RequestParam(value = "envelopeNo", defaultValue = "") String envelopeNo) {
		AuthMember member = AuthMember.toAuthMember(authMember);

		Assert.notNull(envelopeNo, localeMessageSourceService.getMessage("INVALID_RED_ENVELOPE"));
		RedEnvelope redEnvelope = redEnvelopeService.findByEnvelopeNo(envelopeNo);
		Assert.notNull(redEnvelope, localeMessageSourceService.getMessage("INVALID_RED_ENVELOPE"));

		Member authMem = memberService.getById(member.getId());
		Assert.notNull(authMem, localeMessageSourceService.getMessage("ILLEGAL_OPERATION"));

		if (redEnvelope.getState() == 1) {
			return error(localeMessageSourceService.getMessage("RED_ENVELOPE_FULLY_CLAIMED"));
		}

		if (redEnvelope.getState() == 2) {
			return error(localeMessageSourceService.getMessage("RED_ENVELOPE_EXPIRED"));
		}

		if (redEnvelope.getState() == 0) {
			long currentTime = Calendar.getInstance().getTimeInMillis();
			if (currentTime >= (redEnvelope.getCreateTime().getTime()
					+ redEnvelope.getExpiredHours() * 60 * 60 * 1000)) {
				if (redEnvelope.getReceiveCount() < redEnvelope.getCount()) {
					return error(localeMessageSourceService.getMessage("RED_ENVELOPE_EXPIRED"));
				} else {
					return error(localeMessageSourceService.getMessage("RED_ENVELOPE_FULLY_CLAIMED"));
				}
			}
			if (redEnvelope.getReceiveCount() >= redEnvelope.getCount()) {
				return error(localeMessageSourceService.getMessage("RED_ENVELOPE_FULLY_CLAIMED"));
			}
		}

		List<RedEnvelopeDetail> detailList = redEnvelopeDetailService.findByEnvelopeIdAndMemberId(redEnvelope.getId(),
				member.getId());
		if (detailList != null && detailList.size() > 0) {
			if (redEnvelope.getInvite() == 0) {
				return error(localeMessageSourceService.getMessage("RED_ENVELOPE_ALREADY_CLAIMED"));
			}
			if (redEnvelope.getInvite() == 1) {
				Date endDate = new Date();
				List<MemberPromotionStasticVO> inviteList = memberPromotionService.getDateRangeRank(0,
						redEnvelope.getCreateTime(), endDate, 10000);
				if (inviteList.size() <= detailList.size()) {
					return error(localeMessageSourceService.getMessage("NO_NEW_INVITED_FRIENDS"));
				}
			}
		}

		BigDecimal redAmount = BigDecimal.ZERO;

		if (redEnvelope.getType() == 0) {
			BigDecimal leftAmount = redEnvelope.getTotalAmount().subtract(redEnvelope.getReceiveAmount());
			if (redEnvelope.getCount() - redEnvelope.getReceiveCount() == 1) {
				redAmount = leftAmount;
			} else {
				BigDecimal randSeed = redEnvelope.getMaxRand().compareTo(leftAmount) > 0 ? leftAmount
						: redEnvelope.getMaxRand();
				redAmount = randSeed.divide(new BigDecimal(rand.nextInt(1000) + 2), 6, BigDecimal.ROUND_HALF_DOWN);
			}
		} else if (redEnvelope.getType() == 1) {
			redAmount = redEnvelope.getTotalAmount().divide(new BigDecimal(redEnvelope.getCount()), 6,
					BigDecimal.ROUND_HALF_DOWN);
		}

		if (redAmount.add(redEnvelope.getReceiveAmount()).compareTo(redEnvelope.getTotalAmount()) > 0) {
			return error(localeMessageSourceService.getMessage("RED_ENVELOPE_CLAIM_FAILED"));
		}

		MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(redEnvelope.getUnit(),
				member.getId());
		Assert.notNull(memberWallet, localeMessageSourceService.getMessage("WALLET_ERROR"));
		memberWallet.setBalance(memberWallet.getBalance().add(redAmount));

		MemberTransaction memberTransaction = new MemberTransaction();
		memberTransaction.setFee(BigDecimal.ZERO);
		memberTransaction.setAmount(redAmount);
		memberTransaction.setMemberId(memberWallet.getMemberId());
		memberTransaction.setSymbol(redEnvelope.getUnit());
		memberTransaction.setType(TransactionType.RED_IN.getCode());
		memberTransaction.setCreateTime(DateUtil.getCurrentDate());
		memberTransaction.setRealFee("0");
		memberTransaction.setDiscountFee("0");
		memberTransactionService.save(memberTransaction);

		RedEnvelopeDetail detailRecord = new RedEnvelopeDetail();
		detailRecord.setAmount(redAmount);
		detailRecord.setCate(0);
		detailRecord.setMemberId(member.getId());
		detailRecord.setEnvelopeId(redEnvelope.getId());
		detailRecord.setCreateTime(DateUtil.getCurrentDate());
		detailRecord.setUserIdentify(member.getMobilePhone());
		detailRecord.setCate(0);
		redEnvelopeDetailService.save(detailRecord);

		redEnvelope.setReceiveCount(redEnvelope.getReceiveCount() + 1);
		redEnvelope.setReceiveAmount(redEnvelope.getReceiveAmount().add(redAmount));
		redEnvelopeService.updateById(redEnvelope);

		return success(detailRecord);
	}

	@RequestMapping(value = "/receive")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult receiveEnvelope(String phone, String verifyCode, String promotionCode,
			@RequestParam(value = "envelopeNo", defaultValue = "") String envelopeNo,
			@RequestHeader(value = "lang", required = false) String lang) throws Exception {
		Member member = null;
		if (!memberService.phoneIsExist(phone)) {
			ValueOperations valueOperations = redisTemplate.opsForValue();
			Object code = valueOperations.get(SysConstant.PHONE_RECEIVE_ENVELOPE_PREFIX + phone);
			isTrue(!memberService.phoneIsExist(phone), localeMessageSourceService.getMessage("PHONE_ALREADY_EXISTS"));
			isTrue(!memberService.usernameIsExist(phone),
					localeMessageSourceService.getMessage("USERNAME_ALREADY_EXISTS"));
			if (StringUtils.hasText(promotionCode.trim())) {
				isTrue(memberService.userPromotionCodeIsExist(promotionCode),
						localeMessageSourceService.getMessage("USER_PROMOTION_CODE_EXISTS"));
			}

			notNull(code, localeMessageSourceService.getMessage("VERIFICATION_CODE_NOT_EXISTS"));
			if (!code.toString().equals(verifyCode)) {
				return error(localeMessageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
			} else {
				valueOperations.getOperations().delete(SysConstant.PHONE_RECEIVE_ENVELOPE_PREFIX + phone);
			}

			String credentialsSalt = String.valueOf(idWorkByTwitter.nextId());
			String generatePWD = phone.substring(7, 11) + GeneratorUtil.getNonceString(4).toLowerCase();
			smsProvider.sendCustomMessage(phone,
					"Congratulations on your successful registration! Default login password: " + generatePWD
							+ ", please change after login!");
			String password = MD5.md5Digest(generatePWD + credentialsSalt).toLowerCase();
			Member member1 = new Member();
			member1.setStatus(CommonStatus.NORMAL.getCode());
			member1.setMemberLevel(MemberLevelEnum.GENERAL.getCode());
			member1.setCountry("Vietnam");
			member1.setUsername(phone);
			member1.setPassword(password);
			member1.setMobilePhone(phone);
			member1.setSalt(credentialsSalt);
			member1.setAvatar("https://wikex01.oss-cn-hongkong.aliyuncs.com/defaultavatar.png");
			memberService.save(member1);
			member = member1;
			if (member != null) {
				member.setPromotionCode(GeneratorUtil.getPromotionCode(member.getId()));
				memberEvent.onRegisterSuccess(member, promotionCode.trim(), lang);
			} else {
				return error(localeMessageSourceService.getMessage("REGISTRATION_FAILED"));
			}
		} else {
			ValueOperations valueOperations = redisTemplate.opsForValue();
			Object code = valueOperations.get(SysConstant.PHONE_RECEIVE_ENVELOPE_PREFIX + phone);

			notNull(code, localeMessageSourceService.getMessage("VERIFICATION_CODE_NOT_EXISTS"));
			if (!code.toString().equals(verifyCode)) {
				return error(localeMessageSourceService.getMessage("VERIFICATION_CODE_INCORRECT"));
			} else {
				valueOperations.getOperations().delete(SysConstant.PHONE_RECEIVE_ENVELOPE_PREFIX + phone);
			}
			member = memberService.findByPhone(phone);
			if (member == null) {
				return error(localeMessageSourceService.getMessage("USER_DOES_NOT_EXIST"));
			}
		}

		Assert.notNull(envelopeNo, localeMessageSourceService.getMessage("INVALID_RED_ENVELOPE"));
		RedEnvelope redEnvelope = redEnvelopeService.findByEnvelopeNo(envelopeNo);
		Assert.notNull(redEnvelope, localeMessageSourceService.getMessage("INVALID_RED_ENVELOPE"));

		Member authMember = memberService.getById(member.getId());
		Assert.notNull(authMember, localeMessageSourceService.getMessage("ILLEGAL_OPERATION"));

		Coin coin = coinService.findByUnit(redEnvelope.getUnit());
		Assert.notNull(redEnvelope, localeMessageSourceService.getMessage("RED_ENVELOPE_COIN_NOT_FOUND"));

		if (redEnvelope.getState() == 1) {
			return error(localeMessageSourceService.getMessage("RED_ENVELOPE_FULLY_CLAIMED"));
		}

		if (redEnvelope.getState() == 2) {
			return error(localeMessageSourceService.getMessage("RED_ENVELOPE_EXPIRED"));
		}

		if (redEnvelope.getState() == 0) {
			long currentTime = Calendar.getInstance().getTimeInMillis();
			if (currentTime >= (redEnvelope.getCreateTime().getTime()
					+ redEnvelope.getExpiredHours() * 60 * 60 * 1000)) {
				if (redEnvelope.getReceiveCount() < redEnvelope.getCount()) {
					return error(localeMessageSourceService.getMessage("RED_ENVELOPE_EXPIRED"));
				} else {
					return error(localeMessageSourceService.getMessage("RED_ENVELOPE_FULLY_CLAIMED"));
				}
			}
			if (redEnvelope.getReceiveCount() >= redEnvelope.getCount()) {
				return error(localeMessageSourceService.getMessage("RED_ENVELOPE_FULLY_CLAIMED"));
			}
		}

		List<RedEnvelopeDetail> detailList = redEnvelopeDetailService.findByEnvelopeIdAndMemberId(redEnvelope.getId(),
				member.getId());
		if (detailList != null && detailList.size() > 0) {
			if (redEnvelope.getInvite() == 0) {
				return error(localeMessageSourceService.getMessage("RED_ENVELOPE_ALREADY_CLAIMED"));
			}
			if (redEnvelope.getInvite() == 1) {
				Date endDate = new Date();
				List<MemberPromotionStasticVO> inviteList = memberPromotionService.getDateRangeRank(0,
						redEnvelope.getCreateTime(), endDate, 10000);
				if (inviteList.size() <= detailList.size()) {
					return error(localeMessageSourceService.getMessage("NO_NEW_INVITED_FRIENDS"));
				}
			}
		}

		BigDecimal redAmount = BigDecimal.ZERO;
		if (redEnvelope.getType() == 0) {
			BigDecimal leftAmount = redEnvelope.getTotalAmount().subtract(redEnvelope.getReceiveAmount());
			if (redEnvelope.getCount() - redEnvelope.getReceiveCount() == 1) {
				redAmount = leftAmount;
			} else {
				BigDecimal randSeed = redEnvelope.getMaxRand().compareTo(leftAmount) > 0 ? leftAmount
						: redEnvelope.getMaxRand();
				redAmount = randSeed.divide(new BigDecimal(rand.nextInt(1000) + 2), 6, BigDecimal.ROUND_HALF_DOWN);
			}
		} else if (redEnvelope.getType() == 1) {
			redAmount = redEnvelope.getTotalAmount().divide(new BigDecimal(redEnvelope.getCount()), 6,
					BigDecimal.ROUND_HALF_DOWN);
		}

		if (redAmount.add(redEnvelope.getReceiveAmount()).compareTo(redEnvelope.getTotalAmount()) > 0) {
			return error(localeMessageSourceService.getMessage("RED_ENVELOPE_CLAIM_FAILED"));
		}

		MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(redEnvelope.getUnit(),
				member.getId());
		Assert.notNull(memberWallet, localeMessageSourceService.getMessage("WALLET_ERROR"));
		memberWallet.setBalance(memberWallet.getBalance().add(redAmount));

		MemberTransaction memberTransaction = new MemberTransaction();
		memberTransaction.setFee(BigDecimal.ZERO);
		memberTransaction.setAmount(redAmount);
		memberTransaction.setMemberId(memberWallet.getMemberId());
		memberTransaction.setSymbol(redEnvelope.getUnit());
		memberTransaction.setType(TransactionType.RED_IN.getCode());
		memberTransaction.setCreateTime(DateUtil.getCurrentDate());
		memberTransaction.setRealFee("0");
		memberTransaction.setDiscountFee("0");
		memberTransactionService.save(memberTransaction);

		RedEnvelopeDetail detailRecord = new RedEnvelopeDetail();
		detailRecord.setAmount(redAmount);
		detailRecord.setCate(0);
		detailRecord.setMemberId(member.getId());
		detailRecord.setEnvelopeId(redEnvelope.getId());
		detailRecord.setCreateTime(DateUtil.getCurrentDate());
		detailRecord.setUserIdentify(member.getMobilePhone());
		detailRecord.setCate(0);
		redEnvelopeDetailService.save(detailRecord);
		detailRecord.setPromotionCode(member.getPromotionCode());

		redEnvelope.setReceiveCount(redEnvelope.getReceiveCount() + 1);
		redEnvelope.setReceiveAmount(redEnvelope.getReceiveAmount().add(redAmount));
		redEnvelopeService.updateById(redEnvelope);

		return success(detailRecord);
	}

	@RequestMapping(value = "/mockreceivedudalsldds")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult receiveEnvelopeMock(@RequestParam(value = "sign", defaultValue = "") String sign,
			@RequestParam(value = "memberId", defaultValue = "") Long memberId,
			@RequestParam(value = "envelopeNo", defaultValue = "") String envelopeNo) throws Exception {

		if (!sign.equals("77585211314qazwsx")) {
			return error(localeMessageSourceService.getMessage("ILLEGAL_REQUEST"));
		}

		Assert.notNull(envelopeNo, localeMessageSourceService.getMessage("INVALID_RED_ENVELOPE"));
		RedEnvelope redEnvelope = redEnvelopeService.findByEnvelopeNo(envelopeNo);
		Assert.notNull(redEnvelope, localeMessageSourceService.getMessage("INVALID_RED_ENVELOPE"));

		Member authMember = memberService.getById(memberId);
		Assert.notNull(authMember, localeMessageSourceService.getMessage("ILLEGAL_OPERATION"));

		if (redEnvelope.getState() == 1) {
			return error(localeMessageSourceService.getMessage("RED_ENVELOPE_FULLY_CLAIMED"));
		}

		if (redEnvelope.getState() == 2) {
			return error(localeMessageSourceService.getMessage("RED_ENVELOPE_EXPIRED"));
		}

		if (redEnvelope.getState() == 0) {
			long currentTime = Calendar.getInstance().getTimeInMillis();
			if (currentTime >= (redEnvelope.getCreateTime().getTime()
					+ redEnvelope.getExpiredHours() * 60 * 60 * 1000)) {
				if (redEnvelope.getReceiveCount() < redEnvelope.getCount()) {
					return error(localeMessageSourceService.getMessage("RED_ENVELOPE_EXPIRED"));
				} else {
					return error(localeMessageSourceService.getMessage("RED_ENVELOPE_FULLY_CLAIMED"));
				}
			}

			if (redEnvelope.getReceiveCount() >= redEnvelope.getCount()) {
				return error(localeMessageSourceService.getMessage("RED_ENVELOPE_FULLY_CLAIMED"));
			}
		}

		List<RedEnvelopeDetail> detailList = redEnvelopeDetailService.findByEnvelopeIdAndMemberId(redEnvelope.getId(),
				memberId);
		if (detailList != null && detailList.size() > 0) {
			if (redEnvelope.getInvite() == 0) {
				return error(localeMessageSourceService.getMessage("RED_ENVELOPE_ALREADY_CLAIMED"));
			}
			if (redEnvelope.getInvite() == 1) {
				Date endDate = new Date();
				List<MemberPromotionStasticVO> inviteList = memberPromotionService.getDateRangeRank(0,
						redEnvelope.getCreateTime(), endDate, 10000);
				if (inviteList.size() <= detailList.size()) {
					return error(localeMessageSourceService.getMessage("NO_NEW_INVITED_FRIENDS"));
				}
			}
		}

		BigDecimal redAmount = BigDecimal.ZERO;

		if (redEnvelope.getType() == 0) {
			BigDecimal leftAmount = redEnvelope.getTotalAmount().subtract(redEnvelope.getReceiveAmount());
			if (redEnvelope.getCount() - redEnvelope.getReceiveCount() == 1) {
				redAmount = leftAmount;
			} else {
				BigDecimal randSeed = redEnvelope.getMaxRand().compareTo(leftAmount) > 0 ? leftAmount
						: redEnvelope.getMaxRand();
				redAmount = randSeed.divide(new BigDecimal(rand.nextInt(1000) + 2), 6, BigDecimal.ROUND_HALF_DOWN);
			}
		} else if (redEnvelope.getType() == 1) {
			redAmount = redEnvelope.getTotalAmount().divide(new BigDecimal(redEnvelope.getCount()), 6,
					BigDecimal.ROUND_HALF_DOWN);
		}

		if (redAmount.add(redEnvelope.getReceiveAmount()).compareTo(redEnvelope.getTotalAmount()) > 0) {
			return error(localeMessageSourceService.getMessage("RED_ENVELOPE_CLAIM_FAILED"));
		}

		RedEnvelopeDetail detailRecord = new RedEnvelopeDetail();
		detailRecord.setAmount(redAmount);
		detailRecord.setCate(0);
		detailRecord.setMemberId(memberId);
		detailRecord.setEnvelopeId(redEnvelope.getId());
		detailRecord.setUserIdentify(authMember.getMobilePhone());
		detailRecord.setCreateTime(DateUtil.getCurrentDate());
		detailRecord.setCate(1);
		redEnvelopeDetailService.save(detailRecord);

		redEnvelope.setReceiveCount(redEnvelope.getReceiveCount() + 1);
		redEnvelope.setReceiveAmount(redEnvelope.getReceiveAmount().add(redAmount));
		redEnvelopeService.updateById(redEnvelope);

		return success(detailRecord);
	}

	@PostMapping("/code")
	public MessageResult envelopeCode(String phone, String country, Long envelopeId) throws Exception {
		Assert.notNull(country, localeMessageSourceService.getMessage("REQUEST_ILLEGAL"));
		Country country1 = countryService.findOne(country);
		Assert.notNull(country1, localeMessageSourceService.getMessage("REQUEST_ILLEGAL"));
		Assert.notNull(envelopeId, localeMessageSourceService.getMessage("REQUEST_ILLEGAL"));

		RedEnvelope redEnvelope = redEnvelopeService.getById(envelopeId);
		Assert.notNull(redEnvelope, localeMessageSourceService.getMessage("INVALID_RED_ENVELOPE"));

		if (redEnvelope.getState() == 1) {
			return error(localeMessageSourceService.getMessage("RED_ENVELOPE_FULLY_CLAIMED"));
		}

		if (redEnvelope.getState() == 2) {
			return error(localeMessageSourceService.getMessage("RED_ENVELOPE_EXPIRED"));
		}

		if (redEnvelope.getState() == 0) {
			long currentTime = Calendar.getInstance().getTimeInMillis();
			if (currentTime >= (redEnvelope.getCreateTime().getTime()
					+ redEnvelope.getExpiredHours() * 60 * 60 * 1000)) {
				if (redEnvelope.getReceiveCount() < redEnvelope.getCount()) {
					return error("This red envelope has expired!");
				} else {
					return error(localeMessageSourceService.getMessage("RED_ENVELOPE_FULLY_CLAIMED"));
				}
			}

			if (redEnvelope.getReceiveCount() >= redEnvelope.getCount()) {
				return error(localeMessageSourceService.getMessage("RED_ENVELOPE_FULLY_CLAIMED"));
			}
		}

		Member member = null;
		if (memberService.phoneIsExist(phone)) {
			member = memberService.findByPhone(phone);
			List<RedEnvelopeDetail> detailList = redEnvelopeDetailService
					.findByEnvelopeIdAndMemberId(redEnvelope.getId(), member.getId());
			if (detailList != null && detailList.size() > 0) {
				if (redEnvelope.getInvite() == 0) {
					return error(localeMessageSourceService.getMessage("RED_ENVELOPE_ALREADY_CLAIMED"));
				}
				if (redEnvelope.getInvite() == 1) {
					Date endDate = new Date();
					List<MemberPromotionStasticVO> inviteList = memberPromotionService.getDateRangeRank(0,
							redEnvelope.getCreateTime(), endDate, 10000);
					if (inviteList.size() <= detailList.size()) {
						return error(localeMessageSourceService.getMessage("NO_NEW_INVITED_FRIENDS"));
					}
				}
			}
		}

		ValueOperations valueOperations = redisTemplate.opsForValue();
		String key = SysConstant.PHONE_RECEIVE_ENVELOPE_PREFIX + phone;
		Object code = valueOperations.get(key);
		if (code != null) {
			if (!BigDecimalUtils.compare(DateUtil.diffMinute((Date) (valueOperations.get(key + "Time"))),
					BigDecimal.ONE)) {
				return error(localeMessageSourceService.getMessage("FREQUENTLY_REQUEST"));
			}
		}

		String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
		MessageResult result;
		if ("86".equals(country1.getAreaCode())) {
			Assert.isTrue(ValidateUtil.isMobilePhone(phone.trim()),
					localeMessageSourceService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
			result = smsProvider.sendVerifyMessage(phone, randomCode);
		} else {
			result = smsProvider.sendInternationalMessage(randomCode, country1.getAreaCode() + phone);
		}
		if (result.getCode() == 0) {
			valueOperations.getOperations().delete(key);
			valueOperations.getOperations().delete(key + "Time");

			valueOperations.set(key, randomCode, 10, TimeUnit.MINUTES);
			valueOperations.set(key + "Time", new Date(), 10, TimeUnit.MINUTES);
			return success(localeMessageSourceService.getMessage("SEND_SMS_SUCCESS"));
		} else {
			return error(localeMessageSourceService.getMessage("SEND_SMS_FAILED"));
		}
	}
}
