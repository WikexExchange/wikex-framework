package com.wikex.wikex.user.entity;

import lombok.Data;

import javax.annotation.Nullable;

@Data
public class UpdateMember {

    @Nullable()
    private String realName;

    @Nullable()
    private String avatar;

}
