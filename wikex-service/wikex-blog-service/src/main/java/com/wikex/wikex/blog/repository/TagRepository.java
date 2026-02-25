package com.wikex.wikex.blog.repository;

import com.wikex.wikex.blog.entity.TagDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Tag Repository
 */
@Repository
public interface TagRepository extends MongoRepository<TagDocument, String> {
    boolean existsBySlug(String slug);

    TagDocument findBySlug(String slug);
}