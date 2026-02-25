package com.wikex.wikex.blog.service.impl;

import com.wikex.wikex.blog.entity.Tag;
import com.wikex.wikex.blog.entity.TagDocument;
import com.wikex.wikex.blog.entity.BlogDocument;
import com.wikex.wikex.blog.repository.TagRepository;
import com.wikex.wikex.blog.repository.BlogRepository;
import com.wikex.wikex.blog.service.TagService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Locale;

/**
 * Tag Service Implementation
 */
@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private BlogRepository blogRepository;

    private TagDocument toDocument(Tag tag) {
        TagDocument doc = new TagDocument();
        BeanUtils.copyProperties(tag, doc);
        return doc;
    }

    private Tag fromDocument(TagDocument doc) {
        Tag tag = new Tag();
        BeanUtils.copyProperties(doc, tag);
        return tag;
    }

    private String slugify(String name) {
        if (name == null) {
            return null;
        }

        String s = name.trim().toLowerCase(Locale.ROOT);
        s = s.replaceAll("\\s+", "-");
        s = s.replaceAll("-{2,}", "-");
        return s;
    }

    @Override
    public Tag save(Tag tag) {
        if (tag == null || !StringUtils.hasText(tag.getName())) {
            throw new IllegalArgumentException("TAG_NAME_REQUIRED");
        }
        tag.setName(tag.getName().trim());
        String slug = tag.getSlug();
        if (!StringUtils.hasText(slug)) {
            slug = slugify(tag.getName());
        }
        tag.setSlug(slug);

        TagDocument saved;
        if (StringUtils.hasText(tag.getId())) {
            Optional<TagDocument> existingOpt = tagRepository.findById(tag.getId());
            if (!existingOpt.isPresent()) {
                throw new IllegalStateException("TAG_NOT_FOUND");
            }
            TagDocument existing = existingOpt.get();
            TagDocument bySlug = tagRepository.findBySlug(slug);
            if (bySlug != null && !bySlug.getId().equals(existing.getId())) {
                throw new IllegalStateException("TAG_ALREADY_EXISTS");
            }
            existing.setName(tag.getName());
            existing.setSlug(slug);
            saved = tagRepository.save(existing);
        } else {
            if (tagRepository.existsBySlug(slug)) {
                throw new IllegalStateException("TAG_ALREADY_EXISTS");
            }
            TagDocument doc = toDocument(tag);
            saved = tagRepository.save(doc);
        }

        List<BlogDocument> blogs = blogRepository.findByTags_Id(saved.getId());
        if (blogs != null && !blogs.isEmpty()) {
            for (BlogDocument b : blogs) {
                if (b.getTags() != null) {
                    for (Tag t : b.getTags()) {
                        if (saved.getId().equals(t.getId())) {
                            t.setName(saved.getName());
                            t.setSlug(saved.getSlug());
                        }
                    }
                }
            }
            blogRepository.saveAll(blogs);
        }

        return fromDocument(saved);
    }

    @Override
    public void deleteById(String id) {
        if (!tagRepository.existsById(id)) {
            throw new IllegalStateException("TAG_NOT_FOUND");
        }
        tagRepository.deleteById(id);
    }

    @Override
    public Optional<Tag> findById(String id) {
        return tagRepository.findById(id).map(this::fromDocument);
    }

    @Override
    public Page<Tag> findAll(Pageable pageable) {
        Page<TagDocument> docPage = tagRepository.findAll(pageable);

        return new PageImpl<>(
                docPage.getContent().stream().map(this::fromDocument).collect(Collectors.toList()),
                pageable,
                docPage.getTotalElements());
    }

    @Override
    public List<Tag> findAll() {
        return tagRepository.findAll().stream().map(this::fromDocument).collect(Collectors.toList());
    }
}