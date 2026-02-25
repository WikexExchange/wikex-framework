package com.wikex.wikex.user.task;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberInviteStastic;
import com.wikex.wikex.user.entity.MemberInviteStasticRank;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.service.*;
import com.wikex.wikex.user.system.CoinExchangeFactory;
import com.wikex.wikex.user.vo.MemberPromotionStasticVO;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class MemberInviteStasticJob {
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private CoinExchangeFactory coinExchangeFactory;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberTransactionService memberTransactionService;

	@Autowired
	private MemberPromotionService memberPromotionService;

	@Autowired
	private MemberInviteStasticService memberInviteStatsticService;
	@Autowired
	private MemberInviteStasticRankService memberInviteStasticRankService;

	@Value("${spring.mail.username}")
	private String from;
	@Value("${spark.system.host}")
	private String host;
	@Value("${spark.system.name}")
	private String company;

	@Value("${spark.system.admins}")
	private String admins;

	private String serviceName = "bitrade-market";
	private Random random = new Random();

	@XxlJob("stasticMemberInviteAll")
	public void stasticMemberInviteAll() {

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateNow = df.format(new Date());

		int pageNo = 0;
		int pageSize = 100;
		while (true) {
			Page<Member> page = new Page<>(pageNo, pageSize);
			Page<Member> members = memberService.page(page);
			List<Member> all = members.getRecords();
			if (all != null && all.size() > 0) {
				for (Member item : all) {
					if (item.getId() > 10000) {
						List<MemberTransaction> transactions = memberTransactionService.queryAllByMember(item.getId(),
								TransactionType.PROMOTION_AWARD);

						BigDecimal btcTotal = BigDecimal.ZERO;
						BigDecimal ethTotal = BigDecimal.ZERO;
						BigDecimal usdtTotal = BigDecimal.ZERO;
						BigDecimal estimatedTotal = BigDecimal.ZERO;

						if (transactions != null && transactions.size() > 0) {
							for (MemberTransaction tItem : transactions) {
								if (tItem.getSymbol().equals("BTC")) {
									btcTotal = btcTotal.add(tItem.getAmount());
								}
								if (tItem.getSymbol().equals("ETH")) {
									ethTotal = ethTotal.add(tItem.getAmount());
								}
								if (tItem.getSymbol().equals("USDT")) {
									usdtTotal = usdtTotal.add(tItem.getAmount());
								}
							}

							CoinExchangeFactory.ExchangeRate rateBTC = coinExchangeFactory.get("BTC");
							estimatedTotal = estimatedTotal.add(btcTotal.multiply(rateBTC.usdRate));

							CoinExchangeFactory.ExchangeRate rateETH = coinExchangeFactory.get("ETH");
							estimatedTotal = estimatedTotal.add(ethTotal.multiply(rateETH.usdRate));

							estimatedTotal = estimatedTotal.add(usdtTotal).setScale(2);
						}

						MemberInviteStastic mis = memberInviteStatsticService.findByMemberId(item.getId());
						if (mis != null) {
							mis.setUsdtReward(usdtTotal);
							mis.setBtcReward(btcTotal);
							mis.setEthReward(ethTotal);
							mis.setLevelOne(item.getFirstLevel());
							mis.setLevelTwo(item.getSecondLevel());
							mis.setEstimatedReward(estimatedTotal);
							mis.setStasticDate(dateNow);
							memberInviteStatsticService.updateById(mis);
						} else {
							mis = new MemberInviteStastic();

							mis.setMemberId(item.getId());
							mis.setUserIdentify(item.getMobilePhone());
							mis.setIsRobot(0);
							mis.setUsdtReward(usdtTotal);
							mis.setBtcReward(btcTotal);
							mis.setEthReward(ethTotal);
							mis.setLevelOne(item.getFirstLevel());
							mis.setLevelTwo(item.getSecondLevel());
							mis.setEstimatedReward(estimatedTotal);
							mis.setExtraReward(BigDecimal.ZERO);
							mis.setStasticDate(dateNow);

							memberInviteStatsticService.save(mis);
						}
					} else {

						MemberInviteStastic mis = memberInviteStatsticService.findByMemberId(item.getId());
						if (mis != null) {
							int rand1 = random.nextInt(100);
							mis.setUsdtReward(BigDecimal.ZERO);
							mis.setBtcReward(BigDecimal.ZERO);
							mis.setEthReward(BigDecimal.ZERO);
							mis.setLevelOne(mis.getLevelOne() + (rand1 % 5));
							mis.setLevelTwo(mis.getLevelTwo() + (rand1 % 10));
							if (mis.getLevelOne() > 100) {
								mis.setEstimatedReward(mis.getEstimatedReward()
										.add(BigDecimal.valueOf(80 + (rand1 % 80)).add(BigDecimal
												.valueOf(random.nextDouble()).setScale(6, BigDecimal.ROUND_DOWN))));
							} else if (mis.getLevelOne() > 80) {
								mis.setEstimatedReward(mis.getEstimatedReward()
										.add(BigDecimal.valueOf(70 + (rand1 % 70)).add(BigDecimal
												.valueOf(random.nextDouble()).setScale(6, BigDecimal.ROUND_DOWN))));
							} else if (mis.getLevelOne() > 60) {
								mis.setEstimatedReward(mis.getEstimatedReward()
										.add(BigDecimal.valueOf(60 + (rand1 % 60)).add(BigDecimal
												.valueOf(random.nextDouble()).setScale(6, BigDecimal.ROUND_DOWN))));
							} else if (mis.getLevelOne() > 50) {
								mis.setEstimatedReward(mis.getEstimatedReward()
										.add(BigDecimal.valueOf(50 + (rand1 % 50)).add(BigDecimal
												.valueOf(random.nextDouble()).setScale(6, BigDecimal.ROUND_DOWN))));
							} else if (mis.getLevelOne() > 40) {
								mis.setEstimatedReward(mis.getEstimatedReward()
										.add(BigDecimal.valueOf(40 + (rand1 % 40)).add(BigDecimal
												.valueOf(random.nextDouble()).setScale(6, BigDecimal.ROUND_DOWN))));
							} else if (mis.getLevelOne() > 30) {
								mis.setEstimatedReward(mis.getEstimatedReward()
										.add(BigDecimal.valueOf(30 + (rand1 % 30)).add(BigDecimal
												.valueOf(random.nextDouble()).setScale(6, BigDecimal.ROUND_DOWN))));
							} else if (mis.getLevelOne() > 20) {
								mis.setEstimatedReward(mis.getEstimatedReward()
										.add(BigDecimal.valueOf(20 + (rand1 % 20)).add(BigDecimal
												.valueOf(random.nextDouble()).setScale(6, BigDecimal.ROUND_DOWN))));
							} else if (mis.getLevelOne() > 10) {
								mis.setEstimatedReward(mis.getEstimatedReward()
										.add(BigDecimal.valueOf(10 + (rand1 % 10)).add(BigDecimal
												.valueOf(random.nextDouble()).setScale(6, BigDecimal.ROUND_DOWN))));
							} else if (mis.getLevelOne() > 0) {
								mis.setEstimatedReward(mis.getEstimatedReward().add(BigDecimal.valueOf(rand1 % 10).add(
										BigDecimal.valueOf(random.nextDouble()).setScale(6, BigDecimal.ROUND_DOWN))));
							} else {

							}
							mis.setStasticDate(dateNow);

							memberInviteStatsticService.updateById(mis);
						} else {
							mis = new MemberInviteStastic();

							mis.setMemberId(item.getId());
							mis.setUserIdentify(item.getMobilePhone());
							mis.setIsRobot(1);
							mis.setUsdtReward(BigDecimal.ZERO);
							mis.setBtcReward(BigDecimal.ZERO);
							mis.setEthReward(BigDecimal.ZERO);
							mis.setLevelOne(0);
							mis.setLevelTwo(0);
							mis.setEstimatedReward(BigDecimal.ZERO);
							mis.setExtraReward(BigDecimal.ZERO);
							mis.setStasticDate(dateNow);

							memberInviteStatsticService.save(mis);
						}
					}
				}

				pageNo++;
			} else {
				break;
			}
		}

	}

	@XxlJob("stasticMemberInviteDay")
	public void stasticMemberInviteDay() {

		Date cTime = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(cTime);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		Date endDate = calendar.getTime();

		Calendar calendar2 = Calendar.getInstance();
		Date yd = new Date(cTime.getTime() - 24 * 3600 * 1000);
		calendar2.setTime(yd);
		calendar2.set(Calendar.HOUR_OF_DAY, 0);
		calendar2.set(Calendar.MINUTE, 0);
		calendar2.set(Calendar.SECOND, 0);

		Date startDate = calendar2.getTime();

		List<MemberPromotionStasticVO> result = memberPromotionService.getDateRangeRank(0, startDate, endDate, 20);
		List<MemberInviteStasticRank> allList = new ArrayList<MemberInviteStasticRank>();

		for (MemberPromotionStasticVO vo : result) {
			MemberInviteStasticRank misr = new MemberInviteStasticRank();
			misr.setLevelOne(vo.getCount());
			misr.setLevelTwo(0);
			misr.setMemberId(vo.getInviterId());
			misr.setStasticDate(endDate);
			misr.setType(0);
			Member m = memberService.getById(vo.getInviterId());
			misr.setUserIdentify(m.getMobilePhone());

			if (m.getId().compareTo(Long.valueOf(10000)) >= 0) {
				misr.setIsRobot(0);
			} else {
				misr.setIsRobot(1);
			}
			memberInviteStasticRankService.save(misr);
			allList.add(misr);
		}

	}

	@XxlJob("stasticMemberInviteWeek")
	public void stasticMemberInviteWeek() {
		Date cTime = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(cTime);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		Date endDate = calendar.getTime();

		Calendar calendar2 = Calendar.getInstance();
		Date yd = new Date(cTime.getTime() - 7 * 24 * 3600 * 1000);
		calendar2.setTime(yd);
		calendar2.set(Calendar.HOUR, 0);
		calendar2.set(Calendar.MINUTE, 0);
		calendar2.set(Calendar.SECOND, 0);

		Date startDate = calendar2.getTime();

		List<MemberPromotionStasticVO> result = memberPromotionService.getDateRangeRank(0, startDate, endDate, 20);

		List<MemberInviteStasticRank> allList = new ArrayList<MemberInviteStasticRank>();
		for (MemberPromotionStasticVO vo : result) {
			MemberInviteStasticRank misr = new MemberInviteStasticRank();
			misr.setLevelOne(vo.getCount());
			misr.setLevelTwo(0);
			misr.setMemberId(vo.getInviterId());
			misr.setStasticDate(endDate);
			misr.setType(1);
			Member m = memberService.getById(vo.getInviterId());
			misr.setUserIdentify(m.getMobilePhone());

			if (m.getId().compareTo(Long.valueOf(10000)) >= 0) {
				misr.setIsRobot(0);
			} else {
				misr.setIsRobot(1);
			}
			memberInviteStasticRankService.save(misr);

			allList.add(misr);
		}

	}

	@XxlJob("stasticMemberInviteMonth")
	public void stasticMemberInviteMonth() {
		Date cTime = new Date();

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, 0);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		Date endDate = calendar.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.add(Calendar.MONTH, -1);
		calendar2.set(Calendar.DAY_OF_MONTH, 1);
		calendar2.set(Calendar.HOUR_OF_DAY, 0);
		calendar2.set(Calendar.MINUTE, 0);
		calendar2.set(Calendar.SECOND, 0);

		Date startDate = calendar2.getTime();

		List<MemberPromotionStasticVO> result = memberPromotionService.getDateRangeRank(0, startDate, endDate, 20);

		List<MemberInviteStasticRank> allList = new ArrayList<MemberInviteStasticRank>();
		for (MemberPromotionStasticVO vo : result) {
			MemberInviteStasticRank misr = new MemberInviteStasticRank();
			misr.setLevelOne(vo.getCount());
			misr.setLevelTwo(0);
			misr.setMemberId(vo.getInviterId());
			misr.setStasticDate(endDate);
			misr.setType(2);
			Member m = memberService.getById(vo.getInviterId());
			misr.setUserIdentify(m.getMobilePhone());

			if (m.getId().compareTo(Long.valueOf(10000)) >= 0) {
				misr.setIsRobot(0);
			} else {
				misr.setIsRobot(1);
			}
			memberInviteStasticRankService.save(misr);

			allList.add(misr);
		}

	}

	@XxlJob("staticSync")
	public void staticSync() {

		ValueOperations valueOperations = redisTemplate.opsForValue();
		int top = 20;
		JSONObject resultObj = new JSONObject();
		List<MemberInviteStastic> topReward = memberInviteStatsticService.topRewardAmount(top);
		List<MemberInviteStastic> topInvite = memberInviteStatsticService.topInviteCount(top);
		for (MemberInviteStastic item1 : topReward) {
			item1.setUserIdentify(item1.getUserIdentify().substring(0, 3) + "****" + item1.getUserIdentify()
					.substring(item1.getUserIdentify().length() - 4, item1.getUserIdentify().length()));
		}

		for (MemberInviteStastic item2 : topInvite) {
			item2.setUserIdentify(item2.getUserIdentify().substring(0, 3) + "****" + item2.getUserIdentify()
					.substring(item2.getUserIdentify().length() - 4, item2.getUserIdentify().length()));
		}
		resultObj.put("topreward", topReward);
		resultObj.put("topinvite", topInvite);
		valueOperations.set(SysConstant.MEMBER_PROMOTION_TOP_RANK + top, resultObj,
				SysConstant.MEMBER_PROMOTION_TOP_RANK_EXPIRE_TIME, TimeUnit.SECONDS);
	}

}
