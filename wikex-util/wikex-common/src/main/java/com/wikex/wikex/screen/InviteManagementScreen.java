package com.wikex.wikex.screen;

import lombok.Data;

@Data
public class InviteManagementScreen extends PageParam{
    private Long id;
    private String realName;
    private String mobilePhone;
    private String email;
    private Integer pageNumber;
}
