package com.wikex.wikex.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Tag MongoDB Document
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "tag")
public class TagDocument extends Tag {
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