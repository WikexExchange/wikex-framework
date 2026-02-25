package com.wikex.wikex.user.service;

import com.wikex.wikex.user.entity.Addressext;
import java.util.List;

public interface AddressextService {

    public Addressext read(Integer memberid, Integer coinprotocol);

    public List<Addressext> listByMemberId(Integer memberId);

    public Addressext findByAddress(String address);

    public Addressext notUsed(Integer coinprotocol);

    public Addressext saveAndFlush(Addressext addressext);

    public Integer create(Integer id, Integer memberid);

    public Addressext findByMemberIdAndChain(Long memberId, String chain);

    public long countByMemberIdAndChain(Long memberId, String chain);

}
