package com.wikex.wikex.user.vo;

import com.wikex.wikex.user.entity.MemberTransaction;
import lombok.Data;

@Data
public class MemberTransactionVO extends MemberTransaction {

    private String memberUsername ;

    private String memberRealName ;

    private String phone ;

    private String email ;
    
}
