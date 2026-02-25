package com.wikex.wikex.blog.controller;

import com.wikex.wikex.admin.entity.Admin;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.blog.dto.TagDTO;
import com.wikex.wikex.blog.entity.Tag;
import com.wikex.wikex.blog.service.TagService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Tag Controller
 */
@RestController
@RequestMapping("/tag")
@Api(tags = "Tag Management")
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private LocaleMessageSourceService msService;

    /**
     * Create tag
     */
    @PostMapping
    @AccessLog(module = AdminModule.CMS, operation = "Create tag")
    public MessageResult createTag(@SessionAttribute(value = SysConstant.SESSION_ADMIN, required = false) Admin admin,
            @RequestBody TagDTO dto) {
        // if (admin == null) {
        // return MessageResult.error(msService.getMessage("UNAUTHORIZED"));
        // }
        try {
            Tag tag = new Tag();
            tag.setName(dto.getName());

            Tag savedTag = tagService.save(tag);
            MessageResult ms = MessageResult.success("success");
            ms.setData(savedTag);
            return ms;
        } catch (IllegalArgumentException | IllegalStateException e) {
            return MessageResult.error(e.getMessage());
        }
    }

    /**
     * Update tag
     */
    @PutMapping("/{id}")
    @AccessLog(module = AdminModule.CMS, operation = "Update tag")
    public MessageResult updateTag(@SessionAttribute(value = SysConstant.SESSION_ADMIN, required = false) Admin admin,
            @PathVariable String id,
            @RequestBody TagDTO dto) {
        // if (admin == null) {
        // return MessageResult.error(msService.getMessage("UNAUTHORIZED"));
        // }
        try {
            Tag tag = new Tag();
            tag.setId(id);
            tag.setName(dto.getName());

            Tag savedTag = tagService.save(tag);
            MessageResult ms = MessageResult.success("success");
            ms.setData(savedTag);
            return ms;
        } catch (IllegalArgumentException | IllegalStateException e) {
            return MessageResult.error(e.getMessage());
        }
    }

    /**
     * Delete tag
     */
    @DeleteMapping("/{id}")
    @AccessLog(module = AdminModule.CMS, operation = "Delete tag")
    public MessageResult deleteTag(@SessionAttribute(value = SysConstant.SESSION_ADMIN, required = false) Admin admin,
            @PathVariable String id) {
        // if (admin == null) {
        //     return MessageResult.error(msService.getMessage("UNAUTHORIZED"));
        // }
        try {
            tagService.deleteById(id);
            return MessageResult.success("success");
        } catch (IllegalStateException e) {
            return MessageResult.error(e.getMessage());
        }
    }

    /**
     * Get tag by id
     */
    @GetMapping("/{id}")
    @ApiOperation("Get tag by id")
    public MessageResult getTagById(@PathVariable String id) {
        Optional<Tag> tagOpt = tagService.findById(id);

        if (!tagOpt.isPresent()) {
            return MessageResult.error(msService.getMessage("TAG_NOT_FOUND"));
        }

        MessageResult ms = MessageResult.success("success");
        ms.setData(tagOpt.get());
        return ms;
    }

    /**
     * Get tag list with pagination
     */
    @GetMapping("/list")
    @ApiOperation("Get tag list with pagination")
    public MessageResult getTagList(
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Tag> tagPage = tagService.findAll(pageable);

        MessageResult ms = MessageResult.success("success");
        ms.setData(tagPage);
        return ms;
    }

    /**
     * Get all tags
     */
    @GetMapping("/all")
    @ApiOperation("Get all tags")
    public MessageResult getAllTags() {
        List<Tag> tags = tagService.findAll();
        MessageResult ms = MessageResult.success("success");
        ms.setData(tags);
        return ms;
    }
}