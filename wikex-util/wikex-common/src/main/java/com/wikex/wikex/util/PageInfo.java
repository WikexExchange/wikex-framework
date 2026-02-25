package com.wikex.wikex.util;

import java.io.Serializable;

public class PageInfo implements Serializable {

    private long currentpage;

    private long total;

    private int size;

    private int next;

    private int last;

    private int lpage;

    private int rpage;

    private long start;

    public int offsize = 2;

    public PageInfo() {
        super();
    }

    public void setCurrentpage(long currentpage, long total, long pagesize) {

        long pagecount = total / pagesize;

        int totalPages = (int) (total % pagesize == 0 ? total / pagesize : (total / pagesize) + 1);

        this.last = totalPages;

        if (currentpage > totalPages) {
            this.currentpage = totalPages;
        } else {
            this.currentpage = currentpage;
        }

        this.start = (this.currentpage - 1) * pagesize;
    }

    public long getUpper() {
        return currentpage > 1 ? currentpage - 1 : currentpage;
    }

    public void setLast(int last) {
        this.last = (int) (total % size == 0 ? total / size : (total / size) + 1);
    }

    public PageInfo(long total, int currentpage, int pagesize, int offsize) {
        this.offsize = offsize;
        initPage(total, currentpage, pagesize);
    }

    
    public PageInfo(long total, int currentpage, int pagesize) {
        initPage(total, currentpage, pagesize);
    }

    public void initPage(long total, int currentpage, int pagesize) {

        this.total = total;

        this.size = pagesize;

        setCurrentpage(currentpage, total, pagesize);

        int leftcount = this.offsize,
                rightcount = this.offsize;

        this.lpage = currentpage;

        this.rpage = currentpage;

        this.lpage = currentpage - leftcount;
        this.rpage = currentpage + rightcount;

        int topdiv = this.last - rpage;

        this.lpage = topdiv < 0 ? this.lpage + topdiv : this.lpage;

        this.rpage = this.lpage <= 0 ? this.rpage + (this.lpage * -1) + 1 : this.rpage;

        this.lpage = this.lpage <= 0 ? 1 : this.lpage;

        this.rpage = this.rpage > last ? this.last : this.rpage;
    }

    public long getNext() {
        return currentpage < last ? currentpage + 1 : last;
    }

    public void setNext(int next) {
        this.next = next;
    }

    public long getCurrentpage() {
        return currentpage;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getLast() {
        return last;
    }

    public long getLpage() {
        return lpage;
    }

    public void setLpage(int lpage) {
        this.lpage = lpage;
    }

    public long getRpage() {
        return rpage;
    }

    public void setRpage(int rpage) {
        this.rpage = rpage;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setCurrentpage(long currentpage) {
        this.currentpage = currentpage;
    }
}