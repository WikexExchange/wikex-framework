package com.wikex.wikex.screen;

import com.wikex.wikex.constant.CertifiedBusinessStatus;
import com.wikex.wikex.constant.CommonStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class MemberScreen extends PageParam{

    

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date startTime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date endTime;

    
    private CertifiedBusinessStatus status;
    
    private CommonStatus commonStatus ;

    
    private String superPartner;

    private String account;

    private Long inviterId;
}
