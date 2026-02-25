package com.wikex.wikex.rpc.entity;

public class TokenInputData {
    public TokenInputData(String method, String to, String amount){
        this.method=method;
        this.amount=amount;
        this.to=to;
    }
   private String to;
   private String amount;
   private String method;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}