package com.wikex.wikex.user.dto;

import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWallet;
import lombok.Data;

import java.util.List;


@Data
public class MemberDTO {

    private Member member ;

    private List<MemberWallet> list ;

}
