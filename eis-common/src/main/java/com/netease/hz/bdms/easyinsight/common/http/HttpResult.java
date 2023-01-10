package com.netease.hz.bdms.easyinsight.common.http;

import com.netease.hz.bdms.easyinsight.common.constant.ResponseCodeConstant;
import lombok.Data;

import java.io.Serializable;

/**
 * HTTP 响应格式
 */
@Data
public class HttpResult<T> implements Serializable {
    /**
     * 响应状态码
     */
    private int code;
    /**
     * 携带的数据
     */
    private T result;
    /**
     * 携带的摘要信息 (可选)
     */
    private String msg;
    /**
     * 本次请求 id (可选)
     */
    private String reqId;
    /**
     * 请求耗时，单位毫秒 (可选)
     */
    private Long cost;
    /**
     * 携带的详细数据  (可选)
     */
    private Object desc;

    public HttpResult() {
    }

    public HttpResult(int code) {
        this.code = code;
    }

    public HttpResult(int code, String msg, T result, String reqId, Long cost) {
        this.code = code;
        this.msg = msg;
        this.result = result;
        this.reqId = reqId;
        this.cost = cost;
    }

    public static HttpResult error(int code, String message) {
        HttpResult httpResult = new HttpResult(code);
        httpResult.setMsg(message);
        return httpResult;
    }
    public static HttpResult newFailure(int code, String msg, String reqId) {
        return build(code, msg, null, reqId, null);
    }

    public static HttpResult success() {
        return new HttpResult(ResponseCodeConstant.OK);
    }

    public static <T> HttpResult<T> success(T obj) {
        HttpResult<T> httpResult = new HttpResult<>(ResponseCodeConstant.OK);
        httpResult.setResult(obj);
        return httpResult;
    }

    public static <T> HttpResult<T> build(int code, String message, T obj) {
        HttpResult<T> httpResult = new HttpResult<>(code);
        httpResult.setMsg(message);
        httpResult.setResult(obj);
        return httpResult;
    }

    public static <T> HttpResult<T> build(int code, String msg, T result,
        String reqId, Long cost) {
        return new HttpResult<>(code, msg, result, reqId, cost);
    }


}
