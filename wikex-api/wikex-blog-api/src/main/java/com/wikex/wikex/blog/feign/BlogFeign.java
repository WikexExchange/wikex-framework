package com.wikex.wikex.blog.feign;

import com.wikex.wikex.blog.dto.BlogDTO;
import com.wikex.wikex.util.MessageResult;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Blog Feign Client
 */
@FeignClient(value = "wikex-blog", contextId = "postFeign")
public interface BlogFeign {

        /**
         * Get blog by id
         */
        @GetMapping("/postFeign/{id}")
        MessageResult getBlogById(@PathVariable("id") String id);

        /**
         * Get blog list with pagination and filters
         */
        @GetMapping("/postFeign/list")
        MessageResult getBlogListByTag(@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
                        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                        @RequestParam(value = "tagId", defaultValue = "") String tagId,
                        @RequestParam(value = "keyword", defaultValue = "") String keyword);

        /**
         * Get 4 latest blogs
         */
        @GetMapping("/postFeign/admin/list")
        MessageResult getAllBlogsAdmin(@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
                        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                        @RequestParam(value = "tagIds", required = false) List<String> tagIds,
                        @RequestParam(value = "status", required = false) Integer status,
                        @RequestParam(value = "keyword", required = false) String keyword);

        @GetMapping("/postFeign/latest")
        MessageResult getLatestBlogs(@RequestParam(value = "limit", defaultValue = "4") Integer limit);

        /**
         * Create blog
         */
        @PostMapping("/postFeign")
        MessageResult createBlog(@RequestBody BlogDTO blog);

        /**
         * Update blog
         */
        @PutMapping("/postFeign/{id}")
        MessageResult updateBlog(@PathVariable("id") String id, @RequestBody BlogDTO blog);

        /**
         * Delete blog
         */
        @DeleteMapping("/postFeign/{id}")
        MessageResult deleteBlog(@PathVariable("id") String id);
}
