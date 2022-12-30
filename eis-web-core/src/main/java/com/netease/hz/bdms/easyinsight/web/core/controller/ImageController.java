package com.netease.hz.bdms.easyinsight.web.core.controller;

import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.service.facade.ImageFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@RequestMapping("/eis/v1/image")
@RestController
public class ImageController {

    @Resource
    private ImageFacade imageFacade;

    @PostMapping("upload")
    public HttpResult<String> uploadImage(@RequestParam("file") MultipartFile multipartFile, HttpServletRequest request)
            throws IOException {
        String url = imageFacade.uploadImage(multipartFile, request);
        return HttpResult.success(url);
    }
}
