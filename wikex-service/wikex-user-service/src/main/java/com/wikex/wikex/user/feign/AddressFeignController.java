package com.wikex.wikex.user.feign;

import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.user.entity.Addressext;
import com.wikex.wikex.user.entity.Coinprotocol;
import com.wikex.wikex.user.service.AddressextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @Description: coin
 * @date 2021/4/214:20
 */

@RestController
@RequestMapping("/addressFeign")
public class AddressFeignController extends BaseController {

    @Autowired
    private AddressextService addressextService;

    @PostMapping(value = "/findByAddress")
    public Addressext findByAddress(@RequestParam("address")String address){
        return addressextService.findByAddress(address);
    }

    @PostMapping(value = "/save")
    public Addressext save(@RequestBody Addressext addressext){
       return addressextService.saveAndFlush(addressext);
    }

}
