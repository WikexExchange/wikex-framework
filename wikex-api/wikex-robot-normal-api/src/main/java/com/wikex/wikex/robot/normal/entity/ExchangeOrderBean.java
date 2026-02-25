package com.wikex.wikex.robot.normal.entity;


import java.math.BigDecimal;

public class ExchangeOrderBean {
    // Trading pair (e.g.: BTC/USDT)
    private String symbol;
    // Order price (e.g.: 11761.0000)
    private BigDecimal price;
    // Order amount (e.g.: 0.01)
    private BigDecimal amount;
    // Order direction (BUY or SELL)
    private String direction;
    // Order type (LIMIT_PRICE or MARKET_PRICE)
    private String type;
    // Use discount (e.g.: 0)
    private int useDiscount;
    // Order user ID (e.g.: 1)
    private int uid;
    // Order signature (e.g.: 987654321asdf)
	private String sign;
	
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getUseDiscount() {
		return useDiscount;
	}
	public void setUseDiscount(int useDiscount) {
		this.useDiscount = useDiscount;
	}
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}

}
