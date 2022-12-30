package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.netease.eis.adapters.FileUploadAdapter;
import com.netease.hz.bdms.easyinsight.common.SysProperties;
import com.netease.hz.bdms.easyinsight.common.util.DateTimeUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.Random;

@Component
public class FileUploadService {

    @Resource
    private SysProperties sysProperties;

    @Resource
    private FileUploadAdapter fileUploadAdapter;

    public String putImage(String imageKey, InputStream inputStream, Long contentLength, String contentType) {
        return fileUploadAdapter.put(imageKey, inputStream, contentLength, contentType);
    }

    public String putFile(String fileName, InputStream inputStream, Long contentLength, String contentType) {
        String key = generateUploadFileKey(fileName);
        return fileUploadAdapter.put(key, inputStream, contentLength, contentType);
    }

    private String generateUploadFileKey(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        String env = sysProperties.getEnv();
        String hash = DigestUtils.md5Hex(fileName).substring(0, 8);
        int random = new Random().nextInt(1000);
        return env + "_" + DateTimeUtils.getSimplifiedCurrent() + hash + random + suffix;
    }
}
