package com.wikex.wikex.user.deposit.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositPayload implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;

  private String externalUserId;

  @JsonProperty("blockchain")
  @JSONField(name = "blockchain")
  private String blockchain;

  @JsonProperty("chainKey")
  @JSONField(name = "chainKey")
  private String chainKey;

  private String assetSymbol;

  private String assetContract;

  private String address;

  private BigDecimal amount;

  @JsonProperty("amountRaw")
  @JSONField(name = "amountRaw")
  private String amountRaw;

  private Integer decimals;

  @JsonProperty("txHash")
  @JSONField(name = "txHash")
  private String txHash;

  private Integer logIndex;

  private Long blockNumber;

  private Integer confirmations;

  // Status: PENDING, CONFIRMED, CREDITED
  private String status;

  private Map<String, Object> meta;

  private String occurredAt;

  @JSONField(name = "tx_hash")
  public void setTx_hash(String txHash) {
    this.txHash = txHash;
  }
}
