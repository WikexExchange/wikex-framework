package com.wikex.wikex.core;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
@Builder
public class Menu implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long parentId;
    private String name;
    private String url;
    private Integer sort;
    private List<Menu> subMenu;
    private String title;
    private String titleKey;
    private String description;
}
