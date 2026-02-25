package com.wikex.wikex.admin.feign;

import com.wikex.wikex.admin.entity.DataDictionary;
import com.wikex.wikex.admin.service.DataDictionaryService;
import com.wikex.wikex.controller.BaseController;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Entrusted Order Processing Class
 */
@Api(tags = "Entrusted Order Processing Class")
@Slf4j
@RestController
@RequestMapping("/dataDictionaryFeign")
public class DataDictionaryFeignController extends BaseController {

    @Autowired
    private DataDictionaryService dataDictionaryService;

    @PostMapping("findByBond")
    public DataDictionary findByBond(@RequestParam("bond") String bond) {
       return dataDictionaryService.findByBond(bond);
    }
}
