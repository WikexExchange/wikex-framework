package com.wikex.wikex.user.deposit.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositEvent implements Serializable {

  private static final long serialVersionUID = 1L;

  private String type;

  private DepositPayload payload;
}
