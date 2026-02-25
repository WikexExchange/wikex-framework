/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: HawkFilterValue.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019-07-18
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-18, Create
 */
package com.wikex.wikex.core.annotation;

import com.wikex.wikex.core.filter.HFilter;

/**
 * <p>
 * Title: HawkFilterValue
 * </p>
 * <p>
 * Description:
 * </p>
 * Represents filter configuration details including execution order,
 * command lists, and filter instance.
 * 
 * @author MrGao
 * @date 2019-07-18
 */
public class HawkFilterValue implements Comparable<HawkFilterValue> {
    // Execution order
    private int order;
    private int[] cmds;
    private int[] ignoreCmds;
    private HFilter hfilter;

    public HawkFilterValue(int order, int[] cmds, int[] ignoreCmds, HFilter hfilter) {
        this.order = order;
        this.cmds = cmds;
        this.ignoreCmds = ignoreCmds;
        this.hfilter = hfilter;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int[] getCmds() {
        return cmds;
    }

    public void setCmds(int[] cmds) {
        this.cmds = cmds;
    }

    public int[] getIgnoreCmds() {
        return ignoreCmds;
    }

    public void setIgnoreCmds(int[] ignoreCmds) {
        this.ignoreCmds = ignoreCmds;
    }

    public HFilter getHfilter() {
        return hfilter;
    }

    public void setHfilter(HFilter hfilter) {
        this.hfilter = hfilter;
    }

    @Override
    public int compareTo(HawkFilterValue another) {
        return Integer.compare(this.order, another.order);
    }
}
