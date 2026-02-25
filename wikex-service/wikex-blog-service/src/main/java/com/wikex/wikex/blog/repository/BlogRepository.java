package com.wikex.wikex.blog.repository;

import com.wikex.wikex.blog.entity.BlogDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Blog Repository
 */
@Repository
public interface BlogRepository extends MongoRepository<BlogDocument, String> {

        /**
         * Find blogs by status with pagination
         */
        Page<BlogDocument> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);

        /**
         * Find blogs by tag id
         */
        List<BlogDocument> findByTags_Id(String tagId);
}
