package com.wikex.wikex.p2p.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.p2p.entity.Appeal;
import com.wikex.wikex.p2p.vo.AppealVo;
import com.wikex.wikex.screen.AppealScreen;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-p2p",contextId = "appealFeign")
public interface AppealFeign {

    @PostMapping("/appealFeign/appealQuery")
    Page appealQuery(@RequestBody AppealScreen screen);

    @PostMapping("/appealFeign/findOneAppealVO")
    AppealVo findOneAppealVO(@RequestParam("id") Long id);

    @PostMapping("/appealFeign/findOne")
    Appeal findOne(@RequestParam("id") Long id);

    @PostMapping("/appealFeign/updateById")
    MessageResult updateById(@RequestBody Appeal appeal);

    @PostMapping("/appealFeign/countAuditing")
    Integer countAuditing();
}
