package com.wikex.wikex.admin.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;


@Data
@EqualsAndHashCode(callSuper = false)
public class AutomainconfigVo implements Serializable {

    private static final long serialVersionUID = 1L;


    private Integer id;
    
    private Integer coinId;

    
    private String coinName;

    
    private BigDecimal minNum;

    
    private Integer protocol;

    
    private String address;

    private String password;


}
