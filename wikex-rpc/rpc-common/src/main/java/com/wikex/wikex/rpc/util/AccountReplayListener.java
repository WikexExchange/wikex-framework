package com.wikex.wikex.rpc.util;


import com.wikex.wikex.rpc.entity.Account;

public interface AccountReplayListener {

    void replay(Account account);
}
