package com.wikex.wikex.netty.shiro.listener;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;

public class HSessionListener implements SessionListener {
    @Override
    public void onStart(Session session) {
        // Session created: session ID
        // System.out.println("Session created: " + session.getId());
    }

    @Override
    public void onStop(Session session) {
        // Session stopped
    }

    @Override
    public void onExpiration(Session session) {
        // Session expired
    }
}
