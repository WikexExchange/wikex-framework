package com.wikex.wikex.admin.controller.p2p;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.AdvertiseControlStatus;
import com.wikex.wikex.constant.AdvertiseType;
import com.wikex.wikex.p2p.entity.Advertise;
import com.wikex.wikex.p2p.feign.AdvertiseFeign;
import com.wikex.wikex.screen.AdvertiseScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Country;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.feign.CountryFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.util.FileUtil;
import com.wikex.wikex.util.MessageResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


 // Backend advertisement web layer
@RestController
@RequestMapping("/otc/advertise")
public class AdminAdvertiseController extends BaseAdminController {

    @Autowired
    private AdvertiseFeign advertiseService;
    @Autowired
    private LocaleMessageSourceService messageSource;
    @Autowired
    private MemberFeign memberFeign;
    /**
     * Local currency abbreviation
     */
    @Autowired
    private CountryFeign countryFeign;

    @RequiresPermissions("otc:advertise:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.OTC, operation = "Backend advertisement Advertise details")
    public MessageResult detail(Long id) {
        if (id == null) {
            return error(messageSource.getMessage("ID_REQUIRED"));
        }
        Advertise one = advertiseService.findOne(id);
        if (one == null) {
            return error(messageSource.getMessage("NO_ADVERTISEMENT_WITH_THIS_ID"));
        }
        if(StringUtils.isNotEmpty(one.getCountry())) {
          Country country = countryFeign.findByZhName(one.getCountry());
          if(country!=null){
              one.setLocalCurrency(country.getLocalCurrency());
          }
        }
        if(StringUtils.isEmpty(one.getUsername())){
            Member member = memberFeign.findMemberById(one.getMemberId());
            one.setUsername(member.getUsername());
        }
        return success(messageSource.getMessage("SUCCESS"), one);
    }

    @RequiresPermissions("otc:advertise:alter-status")
    @PostMapping("alter-status")
    @AccessLog(module = AdminModule.OTC, operation = "Modify backend advertisement Advertise status")
    public MessageResult statue(
            @RequestParam(value = "ids") Long[] ids,
            @RequestParam(value = "status") AdvertiseControlStatus status) {
        advertiseService.turnOffBatch(status,ids);
        return success(messageSource.getMessage("SUCCESS"));
    }

    @RequiresPermissions("otc:advertise:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.OTC, operation = "Paginated query backend advertisement Advertise")
    public MessageResult page(AdvertiseScreen screen) {
        Page<Advertise> all = advertiseService.findAll(screen);
        List<Advertise> records = all.getRecords();
        all.setRecords(
                records.stream().map(record->{
                    Member member = memberFeign.findMemberById(record.getMemberId());
                    record.setRealName(member.getRealName());
                    return record;
                }).collect(Collectors.toList())
        );

        return success(IPage2Page(all));
    }

    @RequiresPermissions("otc:advertise:out-excel")
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.OTC, operation = "Export backend advertisement Advertise Excel")
    public MessageResult outExcel(
            @RequestParam(value = "startTime", required = false) Date startTime,
            @RequestParam(value = "endTime", required = false) Date endTime,
            @RequestParam(value = "advertiseType", required = false) AdvertiseType advertiseType,
            @RequestParam(value = "realName", required = false) String realName,
            HttpServletRequest request, HttpServletResponse response) throws Exception {

        List<Advertise> list = advertiseService.queryAdvertise(startTime, endTime, advertiseType, realName);
        return new FileUtil().exportExcel(request, response, list, "order");
    }

}
