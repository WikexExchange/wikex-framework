package com.wikex.wikex.user.controller;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.PaymentType;
import com.wikex.wikex.user.entity.PaymentTypeRecord;
import com.wikex.wikex.user.service.PaymentTypeRecordService;
import com.wikex.wikex.user.service.PaymentTypeService;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.user.vo.PaymentTypeConfig;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Api(tags = "Payment")
@RestController
@RequestMapping("payment")
public class PaymentController extends BaseController {
    @Autowired
    private PaymentTypeService paymentTypeService;
    @Autowired
    private PaymentTypeRecordService paymentTypeRecordService;
    @Autowired
    private LocaleMessageSourceService msService;

    private String[] colors = { "#f0a70a", "#e5dc2a", "#4fbe51", "#d07e3b", "#0a4bf0", "#810af0", "#2b9f76" };

    // Query all payment methods
    @ApiOperation(value = "Query all payment methods")
    @GetMapping("list")
    public MessageResult list() {
        List<PaymentType> list = paymentTypeService.findAll();
        return success(list);
    }

    // Query payment method configuration
    @ApiOperation(value = "Query payment method configuration")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id"),
    })
    @GetMapping("findPaymentTypeConfigById")
    public MessageResult list(@RequestParam(value = "id") Long id) {
        List<PaymentTypeConfig> list = paymentTypeService.findPaymentTypeConfigById(id);
        return success(list);
    }

    /**
     * Bind payment method
     */
    @ApiOperation(value = "Bind payment method")
    @PermissionOperation
    @RequestMapping("saveOrUpdate")
    public MessageResult saveOrUpdate(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "type", required = false) Long type,
            @RequestParam(value = "field_1", required = false) String field_1,
            @RequestParam(value = "field_2", required = false) String field_2,
            @RequestParam(value = "field_3", required = false) String field_3,
            @RequestParam(value = "field_4", required = false) String field_4,
            @RequestParam(value = "field_5", required = false) String field_5,
            @RequestParam(value = "field_6", required = false) String field_6,
            @RequestParam(value = "field_7", required = false) String field_7) {

        PaymentTypeRecord record = new PaymentTypeRecord();
        record.setType(type);
        if (id != null) {
            record.setId(id);
        }
        record.setField_1(field_1);
        record.setField_2(field_2);
        record.setField_3(field_3);
        record.setField_4(field_4);
        record.setField_5(field_5);
        record.setField_6(field_6);
        record.setField_7(field_7);
        AuthMember user = AuthMember.toAuthMember(authMember);
        record.setMemberId(user.getId());
        paymentTypeRecordService.saveOrUpdate(record);
        return success();
    }

    /**
     * Get records
     */
    @ApiOperation(value = "Get records")
    @PermissionOperation
    @RequestMapping("getRecords")
    public MessageResult getRecords(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        List<PaymentTypeRecord> list = paymentTypeRecordService.getRecordsByUserId(user.getId());
        if (list != null && list.size() > 0) {
            int index = 0;
            for (PaymentTypeRecord record : list) {
                PaymentType type = paymentTypeService.findPaymentTypeById(record.getType());
                record.setTypeName(type.getCode());
                List<PaymentTypeConfig> cList = JSON.parseArray(type.getConfigJson(), PaymentTypeConfig.class);

                Map<String, String> fieldType = cList.stream()
                        .collect(Collectors.toMap(PaymentTypeConfig::getFieldName, PaymentTypeConfig::getType));
                Map<String, String> fieldName = cList.stream()
                        .collect(Collectors.toMap(PaymentTypeConfig::getFieldName, o -> {
                            String message = msService.getMessage(o.getShowText());
                            if (message.indexOf("(") > 0) {
                                String substring = message.substring(message.indexOf("("), message.indexOf(")") + 1);
                                message = message.replace(substring, "");
                            }
                            return message.trim();
                        }));
                record.setFieldType(fieldType);
                record.setFieldName(fieldName);
                record.setColor(colors[index % 6]);
                index++;
            }
        }
        return success(list);
    }

    public static void main(String[] args) {
        String s = "Payment details (optional)";
        if (s.indexOf("(") > 0) {
            String substring = s.substring(s.indexOf("("), s.indexOf(")") + 1);
            System.out.println(s.replace(substring, ""));
        }
    }

    /**
     * Delete record by ID
     */
    @ApiOperation(value = "Delete record by ID")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id"),
    })
    @PermissionOperation
    @RequestMapping("delRecordById")
    public MessageResult delRecordById(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam("id") Long id) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        paymentTypeRecordService.delRecordById(id);
        return success();
    }

}
