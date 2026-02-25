package com.wikex.wikex.vo;

import lombok.Data;

import java.util.List;


@Data
public class SpecialPage<E> {

    private List<E> context;
    private int currentPage;
    private int totalPage;
    private int pageNumber;
    private int totalElement;
}
