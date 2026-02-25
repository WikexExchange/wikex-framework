package com.wikex.wikex.screen;

import com.wikex.wikex.constant.CertifiedBusinessStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

@Data
public class CancelApplyScreen extends PageParam{

    private String account;
    private CertifiedBusinessStatus status;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date startDate;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date endDate;

}
