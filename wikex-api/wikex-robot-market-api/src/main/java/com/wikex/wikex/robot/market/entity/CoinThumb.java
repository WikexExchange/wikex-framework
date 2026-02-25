package com.wikex.wikex.robot.market.entity;

import java.math.BigDecimal;

public class CoinThumb {
	private BigDecimal price; // Current price
    private BigDecimal high; // Highest price
    private BigDecimal low; // Lowest price
    private Long lastUpdate; // Last update time

	public CoinThumb() {
		price = BigDecimal.ZERO;
		high = BigDecimal.ZERO;
		low = BigDecimal.ZERO;
		lastUpdate = 0L;
	}
	
	public Long getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(Long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public BigDecimal getHigh() {
		return high;
	}
	public void setHigh(BigDecimal high) {
		this.high = high;
	}
	public BigDecimal getLow() {
		return low;
	}
	public void setLow(BigDecimal low) {
		this.low = low;
	}
}
