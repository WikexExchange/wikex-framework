/*
 * Copyright (c) 2017-2018 Archimedes All Rights Reserved.
 * @Author: sanfeng
 * @Date: 2018/3/15 14:50
 * @Version: 1.0
 * History:
 * v1.0.0, sanfeng, 2018/3/15 14:50, Create
 */
package com.wikex.wikex.netty.common;

import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>Description: </p>
 *
 * @Author: sanfeng
 * @Date: 2018/3/15 14:50
 */
public class NettyCacheUtils {
    private static final Logger logger = LoggerFactory.getLogger(NettyCacheUtils.class);

    // Stores the relationship between a specific key and channels, used for pushing messages based on the key
    private static Map<String, Set<Channel>> channelIdCache = new HashMap<>();
    // Stores the relationship between a channel and a user, used to handle the user's channel info when the channel closes
    public static Map<Channel, String> keyChannelCache = new HashMap<>();
    // Stores the relationship between a user and multiple keys
    public static Map<String, Set<String>> userKey = new HashMap<>();

    /**
     * <p>Title: storeChannel</p>
     * <p>Description: </p>
     * Cache all TCP channel information, only for logged-in users
     * @param key Login username
     * @param channel Channel information
     */
    public static void storeChannel(String key, Channel channel) {
        logger.debug("store channel with key:{}, channel id:{}", key, channel.id().asLongText());
        Set<Channel> set = channelIdCache.get(key);
        if (set == null) {
            set = new HashSet<>();
            set.add(channel);
            channelIdCache.put(key, set);
        } else if (!set.contains(channel)) {
            set.add(channel);
        }
    }

    /**
     * <p>Title: getChannel</p>
     * <p>Description: </p>
     * Get channels by username
     * @param key Subscribed key
     * @return Set of channels
     */
    public static Set<Channel> getChannel(String key) {
        if (StringUtils.isEmpty(key)) {
            logger.debug("No channel subscribed for [{}]!", key);
        }
        return channelIdCache.get(key);
    }

    public static void removeChannel(String key) {
        if (StringUtils.isEmpty(key)) {
            logger.debug("No channel subscribed for [{}]!", key);
        }
        channelIdCache.remove(key);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Set<Channel> getAllChannels() {
        Set<Channel> channels = new HashSet<>();
        channelIdCache.forEach((key, value) -> {
            channels.addAll(value);
        });
        return channels;
    }
}
