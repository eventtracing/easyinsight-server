package com.netease.hz.bdms.easyinsight.web.demo.adapters;

import com.netease.eis.adapters.FileUploadAdapter;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class DemoFileUploadAdapter implements FileUploadAdapter {

    @Override
    public String put(String key, InputStream inputStream, Long contentLength, String contentType) {
        return "http://mdgl.nos-jd.163yun.com/test_20221218160733d2b5ca33456.png";
    }
}
