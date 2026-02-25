package com.wikex.wikex.user.dto;

import lombok.Data;

@Data
public class SetInviterDTO {
    private String promotionCode;

    public String getInviterCode() {
        return promotionCode;
    }

    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }
}