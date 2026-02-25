/*
 * Copyright (c) 2017-2018 All Rights Reserved.
 * @Version: 1.0
 */
package com.wikex.wikex.service;

/**
 * <p>Description: </p>
 * Special handling service for connection establishment or disconnection events
 */
public interface ChannelEventDealService {

    /**
     * Handle connection activation request
     * @param serverIp Server-side IP address
     * @param clientIp Client-side IP address
     * @param clientPort Client port number
     */
    void dealChannelActive(String serverIp, String clientIp, int clientPort);
    
    /**
     * Handle connection disconnection request
     * @param serverIp Server-side IP address
     * @param clientIp Client-side IP address
     * @param clientPort Client port number
     */
    void dealChannelDestory(String serverIp, String clientIp, int clientPort);
}
