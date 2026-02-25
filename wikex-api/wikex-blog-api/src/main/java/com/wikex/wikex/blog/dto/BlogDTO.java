package com.wikex.wikex.blog.dto;

import com.wikex.wikex.blog.entity.Tag;
import lombok.Data;

import java.util.List;

@Data
public class BlogDTO {
    private String title;
    private String shortTitle;
    private String content;
    private String coverImage;
    private List<Tag> tags;
}
