package com.netease.hz.bdms.easyinsight.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * overmind鉴权请求header生成工具
 */
@Slf4j
public class OvermindUtil {

    private static final String SIGNATURE_HEADER_NAME = "Overmind-Auth-Sign";
    private static final String CLIENT_ID_HEADER_NAME = "Overmind-Auth-Client";
    private static final String TIMESTAMP_HEADER_NAME = "Overmind-Auth-Timestamp";

    public static Map<String,String> genRequestHeaderParam(String clientId, String secret){
        Map<String,String> headerParams = new HashMap<>();
        Long timestamp = System.currentTimeMillis();
        headerParams.put(CLIENT_ID_HEADER_NAME, encodeClientId(clientId));
        headerParams.put(TIMESTAMP_HEADER_NAME, timestamp.toString());
        headerParams.put(SIGNATURE_HEADER_NAME, calSign(clientId, timestamp, secret));
        return headerParams;
    }

    private static String encodeClientId(String plainClientId) {
        String encodedClientId = Base64.encodeBase64String(plainClientId.getBytes());
        return encodedClientId;
    }
    private static String calSign(String clientId, Long timestamp, String secret) {
        try {
            String algorithm = "HmacSHA256";
            Mac sha256HMAC = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), algorithm);
            sha256HMAC.init(secretKeySpec);
            byte[] bytes = sha256HMAC.doFinal(new StringBuilder(clientId).append(timestamp).toString().getBytes());
            return new String(Base64.encodeBase64(bytes));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            //never goes here
            log.error("", e);
            return null;
        }
    }
}
