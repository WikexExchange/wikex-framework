package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.CoinLink;
import java.util.List;

public interface CoinLinkService extends IService<CoinLink> {
    List<CoinLink> findByCoinId(Long coinId);
}
