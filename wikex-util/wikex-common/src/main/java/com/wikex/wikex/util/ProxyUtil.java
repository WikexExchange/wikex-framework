package com.wikex.wikex.util;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class ProxyUtil {

  public static Proxy getProxy() {
    return Proxy.NO_PROXY;
//    return new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 7890));
  }
}