package com.wikex.wikex.user.entity;

import lombok.Data;

import javax.annotation.Nullable;

import org.hibernate.validator.constraints.NotBlank;

@Data
public class WalletCreate {
  @NotBlank(message = "{WalletCreate.chain.null}")
  private String chain;

  @Nullable()
  private String vaultId;

}
