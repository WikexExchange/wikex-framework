package com.wikex.wikex.swap.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.ContractRewardRecordScreen;
import com.wikex.wikex.swap.entity.MemberTradeLimit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * <p>
 * Contract Trading Limits Front-end Controller
 * </p>
 * 
 * @author markchao
 * @since 2024-01-30
 */
@FeignClient(value = "wikex-swap", contextId = "memberTradeLimitFeign")
public interface MemberTradeLimitFeign {

    /**
     * Paginated query
     * @param screen query filter
     * @return paginated result of MemberTradeLimit
     */
    @PostMapping("/memberTradeLimitFeign/page-query")
    Page<MemberTradeLimit> findAll(@RequestBody ContractRewardRecordScreen screen);

    /**
     * Find one by id
     * @param id
     * @return MemberTradeLimit object
     */
    @PostMapping(value = "/memberTradeLimitFeign/findOne")
    MemberTradeLimit findOne(@RequestParam("id") Long id);

    /**
     * Save or update a MemberTradeLimit
     * @param limit
     * @return saved MemberTradeLimit
     */
    @PostMapping("/memberTradeLimitFeign/save")
    MemberTradeLimit save(@RequestBody MemberTradeLimit limit);

    /**
     * Delete by list of ids
     * @param ids list of MemberTradeLimit ids to delete
     */
    @PostMapping("/memberTradeLimitFeign/del")
    void del(@RequestParam("ids") List<Long> ids);

    /**
     * Find limit by memberId and contractId
     * @param memberId
     * @param contractId
     * @return MemberTradeLimit matching the member and contract
     */
    @PostMapping(value = "/memberTradeLimitFeign/findLimitByMemberIdAndContractId")
    MemberTradeLimit findLimitByMemberIdAndContractId(@RequestParam("memberId") Long memberId,@RequestParam("contractId") Long contractId);
}
