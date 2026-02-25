package com.wikex.wikex.util;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Data
public class TestQ {


    private List<String> request = Collections.synchronizedList(new LinkedList<String>());

    public void showException() {
        for (int i = 0; i < 10000; i++) {
            request.add(i, i+"");
        }
        new Thread(() -> {
            for (int i = 10000; i < 20000; i++) {
                request.add(i,i+"");
                for (int j = 0 ;j<10000;j++){
                    request.add(j,j+"");
                    request.remove(j);
                }
            }
        }).start();


    }

    public String toJSONString() {
        synchronized (request){
            return JSON.toJSONString(this);
        }
    }

//    public static void main(String[] args) {
//        showException();
//        for (int i = 0; i < 1000; i++) {
////            synchronized (request) {
//                JSON.toJSONString(TestQ);
////            }
//        }
//        System.out.println("done");
//    }
}

