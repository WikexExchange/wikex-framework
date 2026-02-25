package com.wikex.wikex.robot.normal.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class JSONUtils {
    public static JSONObject getJsonObject(Object ask) {
        return JSONObject.parseObject(JSON.toJSONString(ask));
    }
}
