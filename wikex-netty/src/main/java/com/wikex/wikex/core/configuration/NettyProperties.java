/*
 * Copyright (c) 2017-2018 Archimedes All Rights Reserved.
 * @Version: 1.0
 * History:
 * v1.0.0, sanfeng,  2018/3/12 11:59, Create
 */
package com.wikex.wikex.core.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>Description: </p>
 *
 */
@ConfigurationProperties(prefix = "aqmd.netty")
public class NettyProperties {
    private int workerThreadSize;
    private int maxTimeout;
    private int defaultTimeout;
    private int dealHandlerThreadSize;
    private String serviceLoggerLevel;

    private int port;
    private int bossThreadSize;
    /**
     * The length of the package header indicating the length occupied by the packet length
     */
    private int packetHeaderLength;
    private int maxFrameLength;
    private int readerIdle;
    private int writerIdle;
    private int bothIdle;
    private int maxTimeoutInterval;

    /**
     * Whether to enable WebSocket
     */
    private int websocketFlag;
    private int websocketPort;

    /**
     * Whether to enable direct access
     */
    private int directAccessFlag;
    /**
     * Commands allowed for direct access to the system
     */
    private String directAccessCommand;

    public NettyProperties() {
    }

    public int getWorkerThreadSize() {
        return workerThreadSize;
    }

    public void setWorkerThreadSize(int workerThreadSize) {
        this.workerThreadSize = workerThreadSize;
    }

    public int getMaxTimeout() {
        return maxTimeout;
    }

    public void setMaxTimeout(int maxTimeout) {
        this.maxTimeout = maxTimeout;
    }

    public int getDefaultTimeout() {
        return defaultTimeout;
    }

    public void setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public int getDealHandlerThreadSize() {
        return dealHandlerThreadSize;
    }

    public void setDealHandlerThreadSize(int dealHandlerThreadSize) {
        this.dealHandlerThreadSize = dealHandlerThreadSize;
    }

    public String getServiceLoggerLevel() {
        return serviceLoggerLevel;
    }

    public void setServiceLoggerLevel(String serviceLoggerLevel) {
        this.serviceLoggerLevel = serviceLoggerLevel;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBossThreadSize() {
        return bossThreadSize;
    }

    public void setBossThreadSize(int bossThreadSize) {
        this.bossThreadSize = bossThreadSize;
    }

    public int getPacketHeaderLength() {
        return packetHeaderLength;
    }

    public void setPacketHeaderLength(int packetHeaderLength) {
        this.packetHeaderLength = packetHeaderLength;
    }

    public int getMaxFrameLength() {
        return maxFrameLength;
    }

    public void setMaxFrameLength(int maxFrameLength) {
        this.maxFrameLength = maxFrameLength;
    }

    public int getReaderIdle() {
        return readerIdle;
    }

    public void setReaderIdle(int readerIdle) {
        this.readerIdle = readerIdle;
    }

    public int getWriterIdle() {
        return writerIdle;
    }

    public void setWriterIdle(int writerIdle) {
        this.writerIdle = writerIdle;
    }

    public int getBothIdle() {
        return bothIdle;
    }

    public void setBothIdle(int bothIdle) {
        this.bothIdle = bothIdle;
    }

    public int getMaxTimeoutInterval() {
        return maxTimeoutInterval;
    }

    public void setMaxTimeoutInterval(int maxTimeoutInterval) {
        this.maxTimeoutInterval = maxTimeoutInterval;
    }

    public int getWebsocketFlag() {
        return websocketFlag;
    }

    public void setWebsocketFlag(int websocketFlag) {
        this.websocketFlag = websocketFlag;
    }

    public int getWebsocketPort() {
        return websocketPort;
    }

    public void setWebsocketPort(int websocketPort) {
        this.websocketPort = websocketPort;
    }

    public int getDirectAccessFlag() {
        return directAccessFlag;
    }

    public void setDirectAccessFlag(int directAccessFlag) {
        this.directAccessFlag = directAccessFlag;
    }

    public String getDirectAccessCommand() {
        return directAccessCommand;
    }

    public void setDirectAccessCommand(String directAccessCommand) {
        this.directAccessCommand = directAccessCommand;
    }
}
