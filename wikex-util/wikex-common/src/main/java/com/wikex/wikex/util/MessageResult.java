package com.wikex.wikex.util;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Response Result")
public class MessageResult<T> {
    @ApiModelProperty(value = "Data")
    private T data;

    public MessageResult(int code , String msg){
        this.code = code;
        this.message = msg;
    }

    public MessageResult(int code , String msg, T object){
        this.code = code;
        this.message = msg;
        this.data = object;
    }

    public MessageResult() {
        // TODO Auto-generated constructor stub
    }

    public static MessageResult success(){
        return new MessageResult(0,"SUCCESS");
    }

    public static MessageResult success(String msg){
        return new MessageResult(0,msg);
    }

    public static <T> MessageResult success(String msg, T data){
        return new MessageResult(0,msg,data);
    }

    public static MessageResult error(int code, String msg){
        return new MessageResult(code,msg);
    }

    public static MessageResult error(String msg){
        return new MessageResult(500,msg);
    }

    @ApiModelProperty(value = "Code")
    private int code;

    @ApiModelProperty(value = "Message")
    private String message;

    @ApiModelProperty(value = "Data")
    private T Data;

    @ApiModelProperty(value = "Total Pages")
    private String totalPage;

    @ApiModelProperty(value = "Total Elements")
    private String totalElement;

    public String getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(String totalPage) {
        this.totalPage = totalPage;
    }

    public String getTotalElement() {
        return totalElement;
    }

    public void setTotalElement(String totalElement) {
        this.totalElement = totalElement;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString(){
        return JSONObject.toJSONString(this);
        //return "{\"code\":"+code+",\"message\":\""+message+"\"}";
    }

    public T getData() {
        return Data;
    }

    public void setData(T data) {
        Data = data;
    }

    public static MessageResult getSuccessInstance(String message , Object data){
        MessageResult result = success(message);
        result.setData(data);
        return result ;
    }
}
