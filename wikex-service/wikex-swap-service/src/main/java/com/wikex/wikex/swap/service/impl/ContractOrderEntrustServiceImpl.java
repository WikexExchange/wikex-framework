package com.wikex.wikex.swap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.admin.entity.DataDictionary;
import com.wikex.wikex.admin.feign.DataDictionaryFeign;
import com.wikex.wikex.constant.ContractOrderDirection;
import com.wikex.wikex.constant.ContractOrderEntrustStatus;
import com.wikex.wikex.constant.ContractOrderEntrustType;
import com.wikex.wikex.constant.ContractOrderPattern;
import com.wikex.wikex.screen.ContractOrderEntrustScreen;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.swap.entity.ContractOrderEntrust;
import com.wikex.wikex.swap.entity.MemberContractPosition;
import com.wikex.wikex.swap.mapper.ContractOrderEntrustMapper;
import com.wikex.wikex.swap.service.ContractOrderEntrustService;
import com.wikex.wikex.swap.service.MemberContractPositionService;
import com.wikex.wikex.user.entity.AgentWallet;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWeightUpper;
import com.wikex.wikex.user.feign.AgentRewardRecordFeign;
import com.wikex.wikex.user.feign.AgentWalletFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberWeightUpperFeign;
import com.wikex.wikex.user.vo.AgentRewardRecordType;
import com.wikex.wikex.util.BigDecimalUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ContractOrderEntrustServiceImpl extends ServiceImpl<ContractOrderEntrustMapper, ContractOrderEntrust>
        implements ContractOrderEntrustService {

    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private MemberWeightUpperFeign memberWeightUpperFeign;

    @Autowired
    private DataDictionaryFeign dataDictionaryFeign;

    @Autowired
    private MemberContractPositionService memberContractPositionService;

    @Autowired
    private AgentWalletFeign agentWalletFeign;

    @Autowired
    private AgentRewardRecordFeign agentRewardRecordFeign;

    @Override
    public List<ContractOrderEntrust> loadUnMatchOrders(Long id) {
        return baseMapper.loadUnMatchOrders(id);
    }

    @Override
    public List<ContractOrderEntrust> queryAllEntrustClosingOrdersByContractCoin(Long memberId, Long contractId,
            ContractOrderDirection direction) {
        QueryWrapper<ContractOrderEntrust> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", memberId);
        queryWrapper.eq("contract_id", contractId);
        queryWrapper.eq("direction", direction);
        queryWrapper.eq("entrust_type", ContractOrderEntrustType.CLOSE);
        queryWrapper.eq("status", ContractOrderEntrustStatus.ENTRUST_ING);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<ContractOrderEntrust> queryAllClosingOrdersByPositionId(Long memberId, Long positionId, Long contractId,
            ContractOrderDirection direction) {
        QueryWrapper<ContractOrderEntrust> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("position_id", positionId);
        queryWrapper.eq("member_id", memberId);
        queryWrapper.eq("contract_id", contractId);
        queryWrapper.eq("direction", direction);
        queryWrapper.eq("entrust_type", ContractOrderEntrustType.CLOSE);
        queryWrapper.eq("status", ContractOrderEntrustStatus.ENTRUST_ING);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public void updateStatus(Long id, ContractOrderEntrustStatus status) {
        baseMapper.updateStatus(id, status);
    }

    @Override
    public IPage<ContractOrderEntrust> queryPageEntrustingOrdersBySymbol(Long memberId, Long contractCoinId, int pageNo,
            int pageSize) {
        IPage<ContractOrderEntrust> page = new Page<>(pageNo, pageSize);
        QueryWrapper<ContractOrderEntrust> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", memberId);
        queryWrapper.eq("contract_id", contractCoinId);
        queryWrapper.eq("status", ContractOrderEntrustStatus.ENTRUST_ING);
        queryWrapper.orderByDesc("create_time");
        IPage<ContractOrderEntrust> result = page(page, queryWrapper);

        List<ContractOrderEntrust> records = result.getRecords();
        if (records != null) {
            for (ContractOrderEntrust record : records) {
                if (record.getPositionId() != null) {
                    MemberContractPosition position = memberContractPositionService.getById(record.getPositionId());
                    record.setLeverage(position.getLeverage());
                }
            }
        }
        return result;
    }

    @Override
    public IPage<ContractOrderEntrust> queryPageEntrustHistoryOrdersBySymbol(Long memberId, Long contractCoinId,
            int pageNo, int pageSize) {
        IPage<ContractOrderEntrust> page = new Page<>(pageNo, pageSize);
        QueryWrapper<ContractOrderEntrust> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", memberId);
        queryWrapper.eq("contract_id", contractCoinId);
        queryWrapper.ne("status", ContractOrderEntrustStatus.ENTRUST_ING);
        queryWrapper.orderByDesc("create_time");
        IPage<ContractOrderEntrust> result = page(page, queryWrapper);

        List<ContractOrderEntrust> records = result.getRecords();
        if (records != null) {
            for (ContractOrderEntrust record : records) {
                if (record.getPositionId() != null) {
                    MemberContractPosition position = memberContractPositionService.getById(record.getPositionId());
                    record.setLeverage(position.getLeverage());
                }
            }
        }
        return result;
    }

    @Override
    public long queryEntrustingOrdersCountByContractCoinIdAndPattern(Long memberId, Long contractCoinId,
            ContractOrderPattern pattern) {
        QueryWrapper<ContractOrderEntrust> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", memberId);
        queryWrapper.eq("contract_id", contractCoinId);
        queryWrapper.eq("patterns", pattern);
        queryWrapper.eq("status", ContractOrderEntrustStatus.ENTRUST_ING);
        return count(queryWrapper);
    }

    @Override
    public long queryEntrustingOrdersCountByContractCoinId(Long memberId, Long contractCoinId) {
        QueryWrapper<ContractOrderEntrust> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", memberId);
        queryWrapper.eq("contract_id", contractCoinId);
        queryWrapper.eq("status", ContractOrderEntrustStatus.ENTRUST_ING);
        return count(queryWrapper);
    }

    @Override
    public List<ContractOrderEntrust> findCanRewardOrders() {
        QueryWrapper<ContractOrderEntrust> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_reward", 0);
        queryWrapper.eq("status", ContractOrderEntrustStatus.ENTRUST_SUCCESS);
        return list(queryWrapper);
    }

    @Override
    public Page<ContractOrderEntrust> pageQuery(ContractOrderEntrustScreen screen) {
        Page<ContractOrderEntrust> page = new Page<>(screen.getPageNo(), screen.getPageSize());
        QueryWrapper<ContractOrderEntrust> queryWrapper = new QueryWrapper<>();
        if (screen.getContractId() != null) {
            queryWrapper.eq("contract_id", screen.getContractId());
        }
        if (screen.getStartTime() != null) {
            queryWrapper.ge("create_time", screen.getStartTime().getTime());
        }
        if (screen.getEndTime() != null) {
            queryWrapper.le("create_time", screen.getEndTime().getTime());
        }
        if (screen.getDirection() != null) {
            queryWrapper.eq("direction", screen.getDirection().getCode());
        }
        if (screen.getEntrustType() != null) {
            queryWrapper.eq("entrust_type", screen.getEntrustType());
        }
        if (screen.getIsBlast() != null) {
            queryWrapper.eq("is_blast", screen.getIsBlast());
        }
        if (screen.getIsFromSpot() != null) {
            queryWrapper.eq("is_from_spot", screen.getIsFromSpot());
        }
        if (screen.getMemberId() != null) {
            queryWrapper.eq("member_id", screen.getMemberId());
        }
        if (screen.getStatus() != null) {
            queryWrapper.eq("status", screen.getStatus());
        }
        if (screen.getType() != null) {
            queryWrapper.eq("type", screen.getType());
        }
        if (screen.getVolume() != null) {
            queryWrapper.ge("volume", screen.getVolume());
        }
        if (StringUtils.isNotEmpty(screen.getPhone())) {
            Member member = memberFeign.findByPhone(screen.getPhone());
            queryWrapper.eq("member_id", member.getId());
        }
        if (StringUtils.isNotEmpty(screen.getEmail())) {
            Member member = memberFeign.findByEmail(screen.getEmail());
            queryWrapper.eq("member_id", member.getId());
        }
        if (screen.getProfitAndLoss() != null) {
            queryWrapper.gt("profit_and_loss", screen.getProfitAndLoss());
        }
        queryWrapper.orderByDesc("create_time");

        return this.page(page, queryWrapper);
    }

    @Override
    public Page<ContractOrderEntrust> findAll4Agent(Long memberId, PageParam pageParam,
            ContractOrderEntrustScreen screen) {
        Page<ContractOrderEntrust> page = new Page<>(pageParam.getPageNo(), pageParam.getPageSize());
        LambdaQueryWrapper<ContractOrderEntrust> queryWrapper = new LambdaQueryWrapper<>();
        List<Member> memberList = memberFeign.findPromotionMember(memberId);
        if (screen.getContractId() != null) {
            queryWrapper.eq(ContractOrderEntrust::getContractId, screen.getContractId());
        }
        if (screen.getStartTime() != null) {
            queryWrapper.ge(ContractOrderEntrust::getCreateTime, screen.getStartTime().getTime());
        }
        if (screen.getEndTime() != null) {
            queryWrapper.le(ContractOrderEntrust::getCreateTime, screen.getEndTime().getTime());
        }
        if (screen.getEntrustType() != null) {
            queryWrapper.eq(ContractOrderEntrust::getEntrustType, screen.getEntrustType());
        }
        if (screen.getIsBlast() != null) {
            queryWrapper.eq(ContractOrderEntrust::getIsBlast, screen.getIsBlast());
        }
        if (screen.getIsFromSpot() != null) {
            queryWrapper.eq(ContractOrderEntrust::getIsFromSpot, screen.getIsFromSpot());
        }
        if (screen.getMemberId() != null) {
            queryWrapper.eq(ContractOrderEntrust::getMemberId, screen.getMemberId());
        }
        if (screen.getStatus() != null) {
            queryWrapper.eq(ContractOrderEntrust::getStatus, screen.getStatus());
        }
        if (screen.getType() != null) {
            queryWrapper.eq(ContractOrderEntrust::getType, screen.getType());
        }
        if (screen.getVolume() != null) {
            queryWrapper.eq(ContractOrderEntrust::getVolume, screen.getVolume());
        }
        if (screen.getProfitAndLoss() != null) {
            queryWrapper.eq(ContractOrderEntrust::getProfitAndLoss, screen.getProfitAndLoss());
        }
        Long[] ids = new Long[memberList.size()];
        for (int i = 0; i < memberList.size(); i++) {
            ids[i] = memberList.get(i).getId();
        }
        if (StringUtils.isNotEmpty(screen.getPhone())) {
            Member member = memberFeign.findByPhone(screen.getPhone());
            if (memberList.contains(member)) {
                queryWrapper.eq(ContractOrderEntrust::getMemberId, member.getId());
            }
        } else if (StringUtils.isNotEmpty(screen.getEmail())) {
            Member member = memberFeign.findByEmail(screen.getEmail());
            if (memberList.contains(member)) {
                queryWrapper.eq(ContractOrderEntrust::getMemberId, member.getId());
            }
        } else {
            queryWrapper.in(ContractOrderEntrust::getMemberId, ids);
        }
        queryWrapper.orderByDesc(ContractOrderEntrust::getCreateTime);

        return this.page(page, queryWrapper);
    }

    @Override
    public void sendReward() {
        List<ContractOrderEntrust> list = this.findCanRewardOrders();
        if (list != null && list.size() > 0) {
            for (ContractOrderEntrust orderEntrust : list) {
                doSendReward(orderEntrust);
            }
        }
    }

    @Override
    public List<ContractOrderEntrust> queryEntrustingOrdersByContractCoinId(Long memberId, Long contractCoinId) {
        QueryWrapper<ContractOrderEntrust> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", memberId);
        queryWrapper.eq("contract_id", contractCoinId);
        queryWrapper.eq("status", ContractOrderEntrustStatus.ENTRUST_ING);
        return list(queryWrapper);
    }

    private BigDecimal doSendReward(ContractOrderEntrust orderEntrust) {
        BigDecimal lave = BigDecimal.ZERO;
        if (orderEntrust == null) {
            return lave;
        }
        BigDecimal fee = BigDecimal.ZERO;
        if (orderEntrust.getEntrustType() == ContractOrderEntrustType.OPEN) {
            fee = orderEntrust.getOpenFee();
        } else {
            fee = orderEntrust.getCloseFee();
        }

        fee = fee.subtract(orderEntrust.getProfitAndLoss());
        lave = fee;

        this.baseMapper.updateReward(orderEntrust.getId(), 1);

        MemberWeightUpper upper = memberWeightUpperFeign.findMemberWeightUpperByMemberId(orderEntrust.getMemberId());
        if (upper == null || upper.getFirstMemberId() == null) {

            return lave;
        }

        Member member = memberFeign.findMemberById(orderEntrust.getMemberId());
        if (member == null) {

            return lave;
        }
        if (org.apache.commons.lang.StringUtils.isEmpty(upper.getUpper())) {

            return lave;
        }

        List<MemberWeightUpper> uppers = memberWeightUpperFeign.findAllByUpperIds(upper.getUpper());
        if (uppers == null || uppers.size() == 0) {

            return lave;
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

            Member upMember = memberFeign.findMemberById(weightUpper.getMemberId());
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

            AgentWallet wallet = agentWalletFeign.findWalletByMemberIdAndCoinUnit(weightUpper.getMemberId(), "USDT");
            BigDecimal reward = BigDecimalUtils.mulDown(totalReward, rate, 8);
            if (reward.compareTo(BigDecimal.ZERO) != 0) {
                agentWalletFeign.increaseBalance(wallet.getId(), reward);
                agentRewardRecordFeign.saveAgentRewardRecord(member.getId(), upMember.getId(), reward, "USDT",
                        AgentRewardRecordType.SWAP, orderEntrust.getId());
            }
            lave = lave.subtract(reward);
            if (currentRate >= 100) {

                break;
            }
        }

        return lave;
    }
}
