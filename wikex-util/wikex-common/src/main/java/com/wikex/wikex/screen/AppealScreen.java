package com.wikex.wikex.screen;


import lombok.Data;

@Data
public class AppealScreen extends PageParam{
    private Integer advertiseType ;
    private String complainant ;
    private String negotiant;
    private Integer success;
    private String unit ;
    private Integer status ;
    private Boolean auditing = false ;
}
