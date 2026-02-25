package com.wikex.wikex.util;

import com.alibaba.fastjson.JSON;

import java.util.*;

public class Test {


    public static void main(String[] args) {
        TestQ testQ= new TestQ();
        testQ.showException();
        for (int i = 0; i < 1000; i++) {
//            synchronized (testQ.getRequest()) {
            System.out.println(testQ.toJSONString());
//            }
        }
        System.out.println("done");
    }
}

