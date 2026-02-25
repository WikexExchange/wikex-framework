package com.wikex.wikex.blog.service;

import com.wikex.wikex.blog.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Tag Service
 */
public interface TagService {

    Tag save(Tag tag);

    Optional<Tag> findById(String id);

    Page<Tag> findAll(Pageable pageable);

    List<Tag> findAll();

    void deleteById(String id);
}