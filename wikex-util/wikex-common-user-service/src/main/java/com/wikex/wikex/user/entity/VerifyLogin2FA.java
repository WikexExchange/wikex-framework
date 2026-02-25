package com.wikex.wikex.user.entity;

import lombok.Data;

import javax.annotation.Nullable;

import org.hibernate.validator.constraints.NotBlank;

@Data
public class VerifyLogin2FA {
  @NotBlank(message = "{VerifyLogin2FA.secondAuthToken.null}")
  private String secondAuthToken;

  @NotBlank(message = "{VerifyLogin2FA.googleCode.null}")
  private String googleCode;

  @Nullable()
  private Long expiredDays;

}
