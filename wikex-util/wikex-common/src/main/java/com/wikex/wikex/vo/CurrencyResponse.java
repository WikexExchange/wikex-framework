package com.wikex.wikex.vo;

import lombok.Data;

import java.util.List;

@Data
public class CurrencyResponse {
    private String code;
    private List<CurrencyVO> data;
    private String message;
    private String messageDetail;
    private Boolean success;

}
