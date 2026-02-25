package com.wikex.wikex.user.feign;


import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.user.entity.Country;
import com.wikex.wikex.user.service.CountryService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Api(tags = "nation")
@RestController
@RequestMapping("/countryFeign")
public class CountryFeignController extends BaseController {

    @Autowired
    private CountryService countryService;

    @GetMapping("findByZhName")
    public Country findByZhName(@RequestParam("zhName")String zhName) {
        Country country = countryService.findOne(zhName);
        return country;
    }


}

