package com.wikex.wikex.blog.controller;

import com.wikex.wikex.admin.entity.Admin;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.blog.dto.BlogDTO;
import com.wikex.wikex.blog.entity.Blog;
import com.wikex.wikex.blog.service.BlogService;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.MessageResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.List;

/**
 * Blog Controller
 */
@Api(tags = "Blog Management")
@RestController
@RequestMapping("/post")
public class BlogController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private LocaleMessageSourceService msService;

    @PostMapping()
    @AccessLog(module = AdminModule.CMS, operation = "Create blog")
    public MessageResult createBlog(@SessionAttribute(value = SysConstant.SESSION_ADMIN, required = false) Admin admin,
            @RequestBody BlogDTO dto) {
        // if (admin == null) {
        // return MessageResult.error(msService.getMessage("UNAUTHORIZED"));
        // }

        if (dto == null || !StringUtils.hasText(dto.getTitle())) {
            return MessageResult.error(msService.getMessage("BLOG_TITLE_REQUIRED"));
        }

        if (!StringUtils.hasText(dto.getShortTitle())) {
            return MessageResult.error(msService.getMessage("BLOG_SHORT_TITLE_REQUIRED"));
        }

        if (!StringUtils.hasText(dto.getContent())) {
            return MessageResult.error(msService.getMessage("BLOG_CONTENT_REQUIRED"));
        }

        Blog blog = new Blog();
        blog.setTitle(dto.getTitle());
        blog.setShortTitle(dto.getShortTitle());
        blog.setContent(dto.getContent());
        blog.setImgURL(dto.getCoverImage());
        blog.setTags(dto.getTags());
        blog.setStatus(0);
        blog.setViewCount(0);
        blog.setLikeCount(0);
        blog.setCommentCount(0);
        blog.setCreateTime(new Date());
        blog.setUpdateTime(new Date());
        // blog.setUserId(admin.getId());

        Blog savedBlog = blogService.save(blog);

        MessageResult ms = MessageResult.success("success");
        ms.setData(savedBlog);
        return ms;
    }

    @PutMapping("/{id}")
    @AccessLog(module = AdminModule.CMS, operation = "Update blog")
    public MessageResult updateBlog(@SessionAttribute(value = SysConstant.SESSION_ADMIN, required = false) Admin admin,
            @PathVariable String id, @RequestBody BlogDTO dto,
            @RequestParam(required = false) Integer status) {
        // if (admin == null) {
        // return MessageResult.error(msService.getMessage("UNAUTHORIZED"));
        // }

        if (dto == null || !StringUtils.hasText(dto.getTitle())) {
            return MessageResult.error(msService.getMessage("BLOG_TITLE_REQUIRED"));
        }

        if (!StringUtils.hasText(dto.getShortTitle())) {
            return MessageResult.error(msService.getMessage("BLOG_SHORT_TITLE_REQUIRED"));
        }

        if (!StringUtils.hasText(dto.getContent())) {
            return MessageResult.error(msService.getMessage("BLOG_CONTENT_REQUIRED"));
        }

        Optional<Blog> blogOpt = blogService.findById(id);
        if (!blogOpt.isPresent()) {
            return MessageResult.error(msService.getMessage("BLOG_NOT_FOUND"));
        }

        Blog blog = new Blog();
        blog.setId(id);
        blog.setTitle(dto.getTitle());
        blog.setShortTitle(dto.getShortTitle());
        blog.setContent(dto.getContent());
        blog.setImgURL(dto.getCoverImage());
        blog.setTags(dto.getTags());
        blog.setUpdateTime(new Date());
        if (status != null) {
            if (!(status == 0 || status == 1 || status == 2)) {
                return MessageResult.error(msService.getMessage("BLOG_STATUS_INVALID"));
            }
            blog.setStatus(status);
        }
        Blog updatedBlog = blogService.save(blog);

        MessageResult ms = MessageResult.success("success");
        ms.setData(updatedBlog);
        return ms;
    }

    @DeleteMapping("/{id}")
    @AccessLog(module = AdminModule.CMS, operation = "Delete blog")
    public MessageResult deleteBlog(@SessionAttribute(value = SysConstant.SESSION_ADMIN, required = false) Admin admin,
            @PathVariable String id) {
        // if (admin == null) {
        // return MessageResult.error(msService.getMessage("UNAUTHORIZED"));
        // }

        Optional<Blog> blogOpt = blogService.findById(id);
        if (blogOpt.isPresent()) {
            Blog blog = blogOpt.get();
            blog.setStatus(2); // Mark as deleted
            blog.setUpdateTime(new Date());
            blogService.save(blog);
        }

        MessageResult ms = MessageResult.success("success");
        ms.setData(blogOpt);
        return ms;
    }

    @GetMapping("/{id}")
    @ApiOperation("Get blog by id")
    public MessageResult getBlogById(@PathVariable String id) {
        Optional<Blog> blogOpt = blogService.findById(id);
        if (!blogOpt.isPresent()) {
            return MessageResult.error(msService.getMessage("BLOG_NOT_FOUND"));
        }

        Blog blog = blogOpt.get();
        // Increment view count
        blog.setViewCount(blog.getViewCount() == null ? 1 : blog.getViewCount() + 1);
        blog.setUpdateTime(new Date());
        blogService.save(blog);

        MessageResult ms = MessageResult.success("success");
        ms.setData(blog);
        return ms;
    }

    @GetMapping("/list")
    @ApiOperation("Get blog list with pagination")
    public MessageResult getBlogList(@RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String tagId,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.Direction.DESC, "createTime");
        boolean hasFilters = (tagId != null && !tagId.isEmpty()) || StringUtils.hasText(keyword);
        Page<Blog> blogPage = hasFilters
                ? blogService.getList(1, StringUtils.hasText(tagId) ? Collections.singletonList(tagId) : null, keyword,
                        pageable)
                : blogService.findByStatus(1, pageable);
        MessageResult ms = MessageResult.success("success");
        ms.setData(blogPage);
        return ms;
    }

    @GetMapping("/admin/list")
    @ApiOperation("Get all blogs for admin")
    @AccessLog(module = AdminModule.CMS, operation = "List all blogs")
    public MessageResult getAllBlogs(@SessionAttribute(value = SysConstant.SESSION_ADMIN, required = false) Admin admin,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) List<String> tagIds,
            @RequestParam(required = false) String keyword) {
        // if (admin == null) {
        // return MessageResult.error(msService.getMessage("UNAUTHORIZED"));
        // }

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.Direction.DESC, "createTime");
        if (status != null && !(status == 0 || status == 1 || status == 2)) {
            return MessageResult.error(msService.getMessage("BLOG_STATUS_INVALID"));
        }
        boolean hasFilters = (status != null) || (tagIds != null && !tagIds.isEmpty()) || StringUtils.hasText(keyword);
        Page<Blog> blogPage = hasFilters
                ? blogService.getList(status, tagIds, keyword, pageable)
                : blogService.findAll(pageable);
        MessageResult ms = MessageResult.success("success");
        ms.setData(blogPage);
        return ms;
    }

    @GetMapping("/latest")
    @ApiOperation("Get latest published blogs")
    public MessageResult latest(@RequestParam(defaultValue = "4") Integer limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<Blog> blogPage = blogService.findByStatus(1, pageable);
        MessageResult ms = MessageResult.success("success");
        ms.setData(blogPage);
        return ms;
    }
}
