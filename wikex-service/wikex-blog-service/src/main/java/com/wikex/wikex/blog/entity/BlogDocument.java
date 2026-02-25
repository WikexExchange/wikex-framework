package com.wikex.wikex.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Blog MongoDB Document
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "blog")
public class BlogDocument extends Blog {
    @Id
    private String id;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}
