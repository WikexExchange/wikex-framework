package com.wikex.wikex.user.vo;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;


@Data
@Embeddable
public class Location implements Serializable {
    private String country;
    private String province;
    private String city;
    private String district;
}
