package com.wikex.wikex.blog.service.impl;

import com.wikex.wikex.blog.entity.Blog;
import com.wikex.wikex.blog.entity.BlogDocument;
import com.wikex.wikex.blog.repository.BlogRepository;
import com.wikex.wikex.blog.service.BlogService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.beans.PropertyDescriptor;

/**
 * Blog Service Implementation
 */
@Service
public class BlogServiceImpl implements BlogService {

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public static String[] getNullPropertyNames(Object source) {
        BeanWrapper src = new BeanWrapperImpl(source);
        return Arrays.stream(src.getPropertyDescriptors())
                .map(PropertyDescriptor::getName)
                .filter(name -> !"class".equals(name))
                .filter(name -> src.getPropertyValue(name) == null)
                .toArray(String[]::new);
    }

    private BlogDocument toDocument(Blog blog) {
        BlogDocument doc = new BlogDocument();
        BeanUtils.copyProperties(blog, doc);
        return doc;
    }

    private Blog fromDocument(BlogDocument doc) {
        Blog blog = new Blog();
        BeanUtils.copyProperties(doc, blog);
        return blog;
    }

    @Override
    public Blog save(Blog blog) {
        BlogDocument doc;

        if (blog.getId() != null) {
            Optional<BlogDocument> existingOpt = blogRepository.findById(blog.getId());
            if (existingOpt.isPresent()) {
                BlogDocument existing = existingOpt.get();
                BeanUtils.copyProperties(blog, existing, getNullPropertyNames(blog));
                doc = existing;
            } else {
                doc = toDocument(blog);
            }
        } else {
            doc = toDocument(blog);
        }

        return fromDocument(blogRepository.save(doc));
    }

    @Override
    public void deleteById(String id) {
        blogRepository.deleteById(id);
    }

    @Override
    public Optional<Blog> findById(String id) {
        return blogRepository.findById(id).map(this::fromDocument);
    }

    @Override
    public Page<Blog> findByStatus(Integer status, Pageable pageable) {
        Page<BlogDocument> docPage = blogRepository.findByStatusOrderByCreateTimeDesc(status, pageable);
        return new PageImpl<>(
                docPage.getContent().stream().map(this::fromDocument).collect(Collectors.toList()),
                pageable,
                docPage.getTotalElements());
    }

    @Override
    public Page<Blog> findAll(Pageable pageable) {
        Page<BlogDocument> docPage = blogRepository.findAll(pageable);
        return new PageImpl<>(
                docPage.getContent().stream().map(this::fromDocument).collect(Collectors.toList()),
                pageable,
                docPage.getTotalElements());
    }

    @Override
    public Page<Blog> getList(Integer status, List<String> tagIds, String keyword, Pageable pageable) {
        Query query = new Query();

        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }

        if (tagIds != null && !tagIds.isEmpty()) {
            query.addCriteria(Criteria.where("tags.id").in(tagIds));
        }

        if (StringUtils.hasText(keyword)) {
            query.addCriteria(Criteria.where("title").regex(keyword, "i"));
        }

        long total = mongoTemplate.count(query, BlogDocument.class);

        query.with(pageable);
        List<BlogDocument> documents = mongoTemplate.find(query, BlogDocument.class);

        return new PageImpl<>(
                documents.stream().map(this::fromDocument).collect(Collectors.toList()),
                pageable,
                total);
    }
}
