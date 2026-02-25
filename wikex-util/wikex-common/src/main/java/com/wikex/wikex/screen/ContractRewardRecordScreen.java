package com.wikex.wikex.screen;

import com.wikex.wikex.constant.ContractRewardRecordType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.domain.Sort;

import java.util.Date;
import java.util.List;

@Data
public class ContractRewardRecordScreen extends PageParam{
    private ContractRewardRecordType type;
    private Long memberId;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date startTime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date endTime;

    private List<Sort.Direction> direction;
    private List<String> property;
}
