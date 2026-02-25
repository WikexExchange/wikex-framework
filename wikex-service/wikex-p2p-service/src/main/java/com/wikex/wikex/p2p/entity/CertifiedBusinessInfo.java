package com.wikex.wikex.p2p.entity;
import lombok.Data;


@Data
public class CertifiedBusinessInfo {
    private int memberLevel;
    private int certifiedBusinessStatus;
    private String email;
    
    private String detail;
    
    private String reason ;
}
