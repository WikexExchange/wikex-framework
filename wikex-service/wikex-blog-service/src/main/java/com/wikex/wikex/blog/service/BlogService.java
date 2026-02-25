package com.wikex.wikex.blog.service;

import com.wikex.wikex.blog.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Blog Service
 */
public interface BlogService {

    Blog save(Blog blog);

    Optional<Blog> findById(String id);

    Page<Blog> findByStatus(Integer status, Pageable pageable);

    Page<Blog> getList(Integer status, List<String> tagIds, String keyword, Pageable pageable);

    Page<Blog> findAll(Pageable pageable);

    void deleteById(String id);
}