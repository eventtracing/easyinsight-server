package com.netease.hz.bdms.easyinsight.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netease.hz.bdms.easyinsight.common.constant.ResponseCodeConstant;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

@Slf4j
public class HttpUtils {

    private static final int DEFAULT_POST_TIMEOUT = 900000;
    private static final int DEFAULT_GET_TIMEOUT = 20000;

    private HttpUtils() {
    }

    public static HttpResult post(String url, String body, Map<String, String> headers) {
        return post(url, body, headers, DEFAULT_POST_TIMEOUT, false, null, null);
    }

    /**
     * 由于 new TypeReference<HttpResult<T>>() {} 无法识别里面的 T 类型，所以只能手动传 TypeReference 实现.
     */
    public static HttpResult post(String url, String body, Map<String, String> headers, TypeReference reference) {
        return post(url, body, headers, DEFAULT_POST_TIMEOUT, false, null, reference);
    }

    public static HttpResult post(String url, String body, Map<String, String> headers,
                    boolean printLog, String callbackLogMsgPrefix) {
        return post(url, body, headers, DEFAULT_POST_TIMEOUT, printLog, callbackLogMsgPrefix, null);
    }

    @SuppressWarnings("unchecked")
    private static HttpResult post(String url, String body, Map<String, String> headers,
                                   int timeout, boolean printLog, String callbackLogMsgPrefix, TypeReference reference) {
        HttpResult httpResult = null;
        HttpPost post = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(timeout)
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .build();
        post.setConfig(requestConfig);
        post.addHeader("Content-Type", "application/json;charset=UTF-8");
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                post.addHeader(e.getKey(), e.getValue());
            }
        }
        if (!Strings.isNullOrEmpty(body)) {
            BasicHttpEntity requestBody = new BasicHttpEntity();
            requestBody.setContent(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
            requestBody.setContentLength(body.getBytes(StandardCharsets.UTF_8).length);
            post.setEntity(requestBody);
        }
        String tempRecoder = null;
        try {
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
            HttpResponse res = httpClient.execute(post);
            int statusCode = res.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity entity = res.getEntity();
                String resultBody = entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
                tempRecoder = resultBody;
                if (printLog) {
                    String finalResultBody = (resultBody == null ? "(null)" :
                            (resultBody.isEmpty() ? "(empty)" : resultBody));
                    log.info(callbackLogMsgPrefix + finalResultBody);
                }
                if (Strings.isNullOrEmpty(resultBody)) {
                    httpResult = HttpResult.build(statusCode, null, null);
                } else if (reference != null) {
                    httpResult = (HttpResult) JsonUtils.toObjectWithTypeRef(resultBody, reference);
                } else {
                    httpResult = JsonUtils.toObjectWithTypeRef(resultBody, new TypeReference<HttpResult>() {});
                }
            } else {
                httpResult = HttpResult.build(statusCode, "http call return fail! code: " + statusCode, null);
            }
        } catch (Exception e) {
            log.error("http post failed or parse failed: {}, responseBody: {}", url, tempRecoder, e);
            httpResult = HttpResult.build(ResponseCodeConstant.REMOTE_API_EXCEPTION,
                    "http post failed or parse failed" + e.getMessage(), null);
        } finally {
            try {
                post.releaseConnection();
            } catch (Exception e) {
                log.warn("post release connection failed.", e);
            }
        }
        return httpResult;
    }

    public static String get(String url, Map<String, String> headers, Map<String, String> params) {
        return get(url, headers, params, DEFAULT_GET_TIMEOUT, true);
    }

    public static String get(String url, Map<String, String> headers, Map<String, String> params, boolean printErrorLog) {
        return get(url, headers, params, DEFAULT_GET_TIMEOUT, printErrorLog);
    }

    public static String get(String url, Map<String, String> headers, Map<String, String> params, int timeout, boolean printErrorLog) {
        HttpGet get = new HttpGet(url);
        String response = null;
        try {
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    get.addHeader(entry.getKey(), entry.getValue());
                }
            }
            if (params != null && !params.isEmpty()) {
                URIBuilder uriBuilder = new URIBuilder(get.getURI());
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    uriBuilder.addParameter(entry.getKey(), entry.getValue());
                }
                get.setURI(uriBuilder.build());
            }
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(timeout)
                    .setConnectTimeout(timeout)
                    .setConnectionRequestTimeout(timeout)
                    .build();
            get.setConfig(requestConfig);

            HttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(new SSLConnectionSocketFactory(
                            new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(),
                            NoopHostnameVerifier.INSTANCE))
                    .build();
            HttpEntity entity = httpClient.execute(get).getEntity();
            response = EntityUtils.toString(entity, "UTF-8");
            EntityUtils.consume(entity);
        } catch (Exception e) {
            if (printErrorLog) {
                log.error("http get failed, url: {}", url, e);
            }
            throw new RuntimeException("http get failed: " + url + "\n" + e.getMessage(), e);
        } finally {
            get.releaseConnection();
        }
        return response;
    }

    /**
     * 获取客户端IP
     *
     * @param request 本次请求
     * @return 客户端IP
     */
    public static String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        String realIP = Lists.newArrayList(Splitter.on(',').omitEmptyStrings().trimResults().split(ip))
            .get(0);
        return realIP.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : realIP;
    }

    /**
     * 根据Cookie名获取Cookie
     *
     * @param request 本次请求
     * @param name    Cookie名
     * @return Cookie
     */
    public static Cookie getCookieByName(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (null == cookies) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (StringUtils.equalsIgnoreCase(name, cookie.getName())) {
                return cookie;
            }
        }

        return null;
    }

    /**
     * get请求
     *
     * @param url 请求地址
     * @return 响应信息
     * @throws IOException io异常
     */
    public static String getMethod(String url) throws IOException {
        HttpClient client = HttpPoolManager.getHttpClient();
        HttpGet get = new HttpGet(url);
        HttpResponse response = client.execute(get);
        StatusLine sl = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        String content = EntityUtils.toString(entity, "utf-8");
        if (sl.getStatusCode() == HttpStatus.SC_OK) {
            return content;
        } else {
            log.warn("req url failed, url: {}, retcode: {}, content: {}", url, sl.getStatusCode(),
                content);
        }
        return null;
    }

    /**
     * get请求
     *
     * @param url 请求地址
     * @return 响应信息
     * @throws IOException io异常
     */
    public static Map<String, Object> getMethodWithSc(String url, Map<String, String> headers) throws IOException {
        HttpClient client = HttpPoolManager.getHttpClient();
        HttpGet get = new HttpGet(url);
        for (String key : headers.keySet()) {
            get.addHeader(key, headers.get(key));
        }
        HttpResponse response = client.execute(get);
        StatusLine sl = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        String content = EntityUtils.toString(entity, "utf-8");

        Map<String, Object> result = Maps.newHashMap();
        result.put("code", sl.getStatusCode());
        result.put("content", content);
        if (sl.getStatusCode() != HttpStatus.SC_OK) {
            log.warn("req url failed, url: {}, retcode: {}, content: {}", url, sl.getStatusCode(), content);
        }
        return result;
    }

    /**
     * get请求
     *
     * @param url 请求地址
     * @return 响应信息
     * @throws IOException io异常
     */
    public static String getMethod(String url, Map<String, String> headers) throws IOException {
        HttpClient client = HttpPoolManager.getHttpClient();
        HttpGet get = new HttpGet(url);
        for (String key : headers.keySet()) {
            get.addHeader(key, headers.get(key));
        }
        HttpResponse response = client.execute(get);
        StatusLine sl = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        String content = EntityUtils.toString(entity, "utf-8");
        if (sl.getStatusCode() == HttpStatus.SC_OK) {
            return content;
        } else {
            log.warn("req url failed, url: {}, retcode: {}, content: {}", url, sl.getStatusCode(),
                content);
        }
        return null;
    }

    /**
     * post请求
     *
     * @param url     请求地址
     * @param content 请求内容
     * @return 响应信息
     * @throws IOException io异常
     */
    public static String postMethod(String url, String content) throws IOException {
        HttpClient client = HttpPoolManager.getHttpClient();
        HttpPost post = new HttpPost(url);
        StringEntity se = new StringEntity(content, "utf-8");
        se.setContentType("application/json");
        post.setEntity(se);
        HttpResponse response = client.execute(post);
        StatusLine sl = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        String contentRep = EntityUtils.toString(entity, "utf-8");
        if (sl.getStatusCode() == HttpStatus.SC_OK) {
            return contentRep;
        } else {
            log.warn("req url failed, url: {}, retcode: {}, content: {}", url, sl.getStatusCode(),
                contentRep);
        }
        return null;
    }

    /**
     * post请求，携带参数
     *
     * @param url  请求地址
     * @param nvps 携带的参数
     * @return 响应信息
     * @throws IOException io异常
     */
    public static String postMethod(String url, List<NameValuePair> nvps) throws IOException {
        HttpClient client = HttpPoolManager.getHttpClient();
        HttpPost post = new HttpPost(url);
        post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        HttpResponse response = client.execute(post);
        StatusLine sl = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        String content = EntityUtils.toString(entity, "utf-8");
        if (sl.getStatusCode() == HttpStatus.SC_OK) {
            return content;
        } else {
            log.warn("req url failed, url: {}, retcode: {}, content: {}", url, sl.getStatusCode(),
                content);
        }
        return null;
    }

    /**
     * post请求
     *
     * @param url 请求地址
     * @param content 请求内容
     * @return 响应信息
     * @throws IOException io异常
     */
    public static String postMethod(String url, String content,
        Map<String, String> headers) throws IOException {
        HttpClient client = HttpPoolManager.getHttpClient();
        HttpPost post = new HttpPost(url);
        for (String key : headers.keySet()) {
            post.addHeader(key, headers.get(key));
        }
        if (null != content) {
            StringEntity se = new StringEntity(content, "utf-8");
            se.setContentType("application/json");
            post.setEntity(se);
        }
        HttpResponse response = client.execute(post);
        StatusLine sl = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        String contentRep = EntityUtils.toString(entity, "utf-8");

//        Map<String, Object> result = Maps.newHashMap();
//        result.put("code", sl.getStatusCode());
//        result.put("content", contentRep);
        if (sl.getStatusCode() != HttpStatus.SC_OK) {
            log.warn("req url failed, url: {}, retcode: {}, content: {}", url, sl.getStatusCode(), contentRep);
        }
        return contentRep;
    }

    /**
     * 对字符串进行UTF-8编码
     *
     * @param str 要编码的字符串
     * @return 编码后的字符串
     */
    public static String encodeStr(String str) {
        Preconditions.checkArgument(StringUtils.isNotBlank(str));

        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.warn("encode url error, str={}", str, e);
            throw new RuntimeException("unable to encode " + str);
        }
    }

    /**
     * 对字符串进行UTF-8解码
     *
     * @param str 要解码的字符串
     * @return 解码后的字符串
     */
    public static String decodeStr(String str) {
        Preconditions.checkArgument(StringUtils.isNotBlank(str));

        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.warn("encode url error, str={}", str, e);
            throw new RuntimeException("unable to encode " + str);
        }
    }

}
