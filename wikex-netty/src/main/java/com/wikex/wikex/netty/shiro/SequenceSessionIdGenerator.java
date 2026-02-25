/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: SequenceSessionIdGenerator.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019-07-24
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-24, Create
 */
package com.wikex.wikex.netty.shiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Title: SequenceSessionIdGenerator</p>
 * <p>Description: </p>
 * @author MrGao
 * @date 2019-07-24
 */
public class SequenceSessionIdGenerator implements SessionIdGenerator {
    private final static int MIN_SEQ_ID = 0x1fffffff;
    private static AtomicInteger idWoker = new AtomicInteger(MIN_SEQ_ID);

	@Override
    public Serializable generateId(Session session) {
    	//long result=0;
    	int seqId = idWoker.getAndIncrement();
        while (seqId < MIN_SEQ_ID) {
            seqId = idWoker.addAndGet(MIN_SEQ_ID);
        }
        //result=seqId<<32;
        //long time = System.currentTimeMillis();
        //return result+(int)time;
        return (long)seqId;
    }
//    @SuppressWarnings("static-access")
//	public static void main(String[] args){
//    	long seqId = idWoker.getAndIncrement();
////    	System.out.println(seqId+":"+new Long(seqId).toBinaryString(seqId));
//    	seqId=seqId<<32;
////    	System.out.println(seqId+":"+new Long(seqId).toBinaryString(seqId));
//    	long time = System.currentTimeMillis();
////    	System.out.println(time+":"+new Long(time).toBinaryString(time));
////    	System.out.println(time+":"+new Integer((int)time).toBinaryString((int)time));
//    	seqId= seqId+(int)time;
////    	System.out.println(seqId+":"+new Long(seqId).toBinaryString(seqId));
//        while (seqId < MIN_SEQ_ID) {
//            seqId = idWoker.addAndGet(MIN_SEQ_ID);
//        }
//        //seqId<<4+(int)(System.currentTimeMillis());
//    }
}
