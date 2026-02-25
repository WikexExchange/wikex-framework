package com.wikex.wikex.screen;

import com.wikex.wikex.constant.CertifiedBusinessStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ApplyScreen extends PageParam{
    private String account;
    private CertifiedBusinessStatus status;

}
