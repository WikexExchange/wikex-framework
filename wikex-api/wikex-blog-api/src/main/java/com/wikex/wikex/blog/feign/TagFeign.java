package com.wikex.wikex.blog.feign;

import com.wikex.wikex.blog.dto.TagDTO;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "wikex-blog", contextId = "tagFeign")
public interface TagFeign {
    @GetMapping("/tag/{id}")
    MessageResult getTagById(@PathVariable("id") String id);

    @GetMapping("/tagFeign/list")
    MessageResult getTagList(@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize);

    @GetMapping("/tagFeign/all")
    MessageResult getAllTags();

    @PostMapping("/tagFeign")
    MessageResult createTag(@RequestBody TagDTO tag);

    @PutMapping("/tagFeign/{id}")
    MessageResult updateTag(@PathVariable("id") String id, @RequestBody TagDTO tag);

    @DeleteMapping("/tagFeign/{id}")
    MessageResult deleteTag(@PathVariable("id") String id);
}