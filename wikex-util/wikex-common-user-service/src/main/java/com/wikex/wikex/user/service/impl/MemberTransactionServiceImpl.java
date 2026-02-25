package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.admin.entity.DataDictionary;
import com.wikex.wikex.admin.feign.DataDictionaryFeign;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.screen.MemberTransactionScreen;
import com.wikex.wikex.user.entity.AgentWallet;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberPromotion;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.entity.MemberWeightUpper;
import com.wikex.wikex.user.mapper.MemberTransactionMapper;
import com.wikex.wikex.user.service.*;
import com.wikex.wikex.user.vo.AgentRewardRecordType;
import com.wikex.wikex.user.vo.MemberTransaction4Front;
import com.wikex.wikex.user.vo.MemberTransactionVO;
import com.wikex.wikex.user.vo.SymbolAmountSum;
import com.wikex.wikex.user.vo.MemberSymbolAmountSum;
import com.wikex.wikex.util.BigDecimalUtils;
import com.wikex.wikex.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
public class MemberTransactionServiceImpl extends ServiceImpl<MemberTransactionMapper, MemberTransaction>
        implements MemberTransactionService {

    @Autowired
    private MemberWeightUpperService memberWeightUpperService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberPromotionService memberPromotionService;
    @Autowired
    private AgentWalletService agentWalletService;
    @Autowired
    private AgentRewardRecordService agentRewardRecordService;
    @Autowired
    private DataDictionaryFeign dataDictionaryFeign;

    @Override
    public IPage<MemberTransaction> queryByMember(Long memberId, int pageNo, int pageSize, TransactionType type) {
        IPage<MemberTransaction> page = new Page<>(pageNo, pageSize);
        QueryWrapper<MemberTransaction> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", memberId).eq("type", type.getCode());
        queryWrapper.orderByDesc("create_time");
        return this.page(page, queryWrapper);
    }

    @Override
    public BigDecimal getTotalVolumeOfF1(Long inviterId) {
        // get direct F1 list
        List<Long> f1Ids = memberPromotionService.lambdaQuery()
                .eq(MemberPromotion::getInviterId, inviterId)
                .list()
                .stream()
                .map(MemberPromotion::getInviteesId)
                .collect(Collectors.toList());

        if (f1Ids.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // calculate total volume of F1
        List<MemberTransaction> transactions = this.lambdaQuery()
                .in(MemberTransaction::getMemberId, f1Ids)
                .list();

        if (transactions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return transactions.stream()
                .map(MemberTransaction::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalFeeOfF1(Long inviterId) {
        // get direct F1 list
        List<Long> f1Ids = memberPromotionService.lambdaQuery()
                .eq(MemberPromotion::getInviterId, inviterId)
                .list()
                .stream()
                .map(MemberPromotion::getInviteesId)
                .collect(Collectors.toList());

        if (f1Ids.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // calculate total fee of F1
        List<MemberTransaction> transactions = this.lambdaQuery()
                .in(MemberTransaction::getMemberId, f1Ids)
                .list();

        if (transactions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return transactions.stream()
                .map(MemberTransaction::getFee)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Page<MemberTransaction4Front> queryByMember(Long memberId, int pageNo, int pageSize, TransactionType type,
            String startTime, String endTime, String symbol) {
        Page<MemberTransaction> page = new Page<>(pageNo, pageSize);
        QueryWrapper<MemberTransaction> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", memberId);
        if (type != null) {
            queryWrapper.eq("type", type.getCode());
        }
        if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
            try {
                queryWrapper.ge("create_time", DateUtil.YYYY_MM_DD_MM_HH_SS.parse(startTime + " 00:00:00"));
                queryWrapper.le("create_time", DateUtil.YYYY_MM_DD_MM_HH_SS.parse(endTime + " 23:59:59"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (StringUtils.isNotEmpty(symbol)) {
            queryWrapper.eq("symbol", symbol);
        }
        queryWrapper.orderByDesc("create_time");
        page = this.page(page, queryWrapper);
        Page<MemberTransaction4Front> pageVo = new Page<>(pageNo, pageSize);
        BeanUtils.copyProperties(page, pageVo);
        List<MemberTransaction4Front> list = new ArrayList<>();
        for (MemberTransaction record : page.getRecords()) {
            MemberTransaction4Front vo = new MemberTransaction4Front();
            BeanUtils.copyProperties(record, vo);
            vo.setType(record.getType());
            list.add(vo);
        }
        pageVo.setRecords(list);
        return pageVo;
    }

    @Override
    public boolean isOverMatchLimit(String format, double gcxMatchMaxLimit) {
        return false;
    }

    @Override
    public Page<MemberTransactionVO> joinFind(MemberTransactionScreen screen) {
        Page<MemberTransactionVO> page = new Page<>(screen.getPageNo(), screen.getPageSize());
        return this.baseMapper.joinFind(page, screen);
    }

    @Override
    public MemberTransaction findOne(Long id) {
        return this.baseMapper.selectById(id);
    }

    @Override
    public int deleteHistory(Date startTime) {
        LambdaQueryWrapper<MemberTransaction> query = new LambdaQueryWrapper<>();
        query.lt(MemberTransaction::getCreateTime, startTime);
        query.eq(MemberTransaction::getMemberId, 1L);
        return this.baseMapper.delete(query);
    }

    @Override
    public void updateRewardRobot() {
        this.baseMapper.updateRewardRobot();
    }

    @Override
    public void sendExchangeReward() {
        List<MemberTransaction> list = this.findCanRewardMemberTransactions(TransactionType.EXCHANGE);
        if (list != null && list.size() > 0) {
            for (MemberTransaction transaction : list) {
                doSendReward(transaction, transaction.getFee(), AgentRewardRecordType.EXCHANGE);
            }
        }
    }

    @Override
    public void sendSecondReward() {
        List<MemberTransaction> list = this.findCanRewardMemberTransactions(TransactionType.SECOND_REWARD);
        List<MemberTransaction> list1 = this.findCanRewardMemberTransactions(TransactionType.SECOND_FAIL);
        list.addAll(list1);
        if (list != null && list.size() > 0) {
            for (MemberTransaction transaction : list) {
                doSendReward(transaction, BigDecimal.ZERO.subtract(transaction.getAmount()),
                        AgentRewardRecordType.SECOND);
            }
        }
    }

    @Override
    public void sendOptionReward() {
        List<MemberTransaction> list = this.findCanRewardMemberTransactions(TransactionType.OPTION_FAIL);
        List<MemberTransaction> list1 = this.findCanRewardMemberTransactions(TransactionType.OPTION_REWARD);
        List<MemberTransaction> list2 = this.findCanRewardMemberTransactions(TransactionType.OPTION_FEE);
        list.addAll(list1);
        list.addAll(list2);
        if (list != null && list.size() > 0) {
            for (MemberTransaction transaction : list) {
                doSendReward(transaction, BigDecimal.ZERO.subtract(transaction.getAmount()),
                        AgentRewardRecordType.OPTION);
            }
        }
    }

    @Override
    public List<MemberTransaction> queryAllByMember(Long memberId, TransactionType type) {
        QueryWrapper<MemberTransaction> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", memberId).eq("type", type.getCode());
        return this.list(queryWrapper);
    }

    private List<MemberTransaction> findCanRewardMemberTransactions(TransactionType type) {
        LambdaQueryWrapper<MemberTransaction> query = new LambdaQueryWrapper<>();
        query.eq(MemberTransaction::getIsReward, 0);
        query.eq(MemberTransaction::getType, type);
        query.ne(MemberTransaction::getMemberId, 1);
        return this.list(query);
    }

    private void updateReward(Long id, int isReward) {
        LambdaUpdateWrapper<MemberTransaction> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MemberTransaction::getId, id);
        updateWrapper.set(MemberTransaction::getIsReward, isReward);
        this.update(updateWrapper);
    }

    private void doSendReward(MemberTransaction transaction, BigDecimal amount, int type) {
        if (transaction == null) {
            return;
        }
        BigDecimal fee = amount;

        this.updateReward(transaction.getId(), 1);

        MemberWeightUpper upper = memberWeightUpperService.findMemberWeightUpperByMemberId(transaction.getMemberId());
        if (upper == null || upper.getFirstMemberId() == null) {

            return;
        }

        Member member = memberService.getById(transaction.getMemberId());
        if (member == null) {

            return;
        }
        if (org.apache.commons.lang.StringUtils.isEmpty(upper.getUpper())) {

            return;
        }

        List<MemberWeightUpper> uppers = memberWeightUpperService.findAllByUpperIds(upper.getUpper());
        if (uppers == null || uppers.size() == 0) {

            return;
        }

        DataDictionary commission = dataDictionaryFeign.findByBond("commission_rate");
        BigDecimal totalReward = BigDecimal.ZERO;
        if (commission == null) {

            totalReward = fee;
        } else {
            totalReward = BigDecimalUtils.mulRound(fee, BigDecimal.valueOf(Double.parseDouble(commission.getValue())),
                    8);
        }

        int currentRate = 0;
        for (MemberWeightUpper weightUpper : uppers) {

            Member upMember = memberService.getById(weightUpper.getMemberId());
            if (upMember == null) {

                continue;
            }
            int userRate = 0;
            if ("1".equals(upMember.getSuperPartner())) {
                userRate = weightUpper.getRate();
            }

            int releaseRate = userRate - currentRate;
            if (releaseRate <= 0) {

                continue;
            }
            currentRate = userRate;
            BigDecimal rate = BigDecimal.valueOf(releaseRate).divide(BigDecimal.valueOf(100), 8, BigDecimal.ROUND_DOWN);

            AgentWallet wallet = agentWalletService.findWalletByMemberIdAndCoinUnit(weightUpper.getMemberId(),
                    transaction.getSymbol());
            BigDecimal reward = BigDecimalUtils.mulDown(totalReward, rate, 8);
            if (reward.compareTo(BigDecimal.ZERO) != 0) {
                agentWalletService.increaseBalance(wallet.getId(), reward);
                agentRewardRecordService.saveAgentRewardRecord(member.getId(), upMember.getId(), reward,
                        transaction.getSymbol(), type, transaction.getId());
            }
            if (currentRate >= 100) {

                break;
            }
        }
    }

    @Override
    public List<SymbolAmountSum> sumAmountByMemberSince(Long memberId, Date startTime) {
        return this.baseMapper.sumAmountByMemberSince(memberId, startTime);
    }

    @Override
    public List<MemberSymbolAmountSum> sumAmountByMembersSince(List<Long> memberIds, Date startTime) {
        return this.baseMapper.sumAmountByMembersSince(memberIds, startTime);
    }
}
