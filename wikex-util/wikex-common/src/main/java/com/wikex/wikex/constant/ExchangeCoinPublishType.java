package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;

public enum ExchangeCoinPublishType implements Serializable {

	UNKNOW(0, "Unknown"),

	NONE(1, "None"),
	
	QIANGGOU(3, "Flash Sale"),
	
	FENTAN(4, "Apportionment");
	

	@EnumValue
	private final int code;

	public int getCode() {
		return this.code;
	}

	public String getDescription() {
		return description;
	}

	private final String description;

	ExchangeCoinPublishType(int val, String description) {
		this.code = val;
		this.description = description;
	}

	@JsonCreator
	public static ExchangeCoinPublishType creator(Object v) {
		if(v instanceof String){
			for (ExchangeCoinPublishType value : ExchangeCoinPublishType.values()) {
				if (Objects.equals(v, value.name())) {
					return value;
				}
			}
		}else {
			for (ExchangeCoinPublishType value : ExchangeCoinPublishType.values()) {
				if (Objects.equals(v, value.getCode())) {
					return value;
				}
			}
		}
		return null;
	}
}
