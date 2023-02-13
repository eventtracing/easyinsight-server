package com.netease.hz.bdms.easyinsight.service.facade;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.service.service.impl.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@Slf4j
@Component
public class ImageFacade {

    @Autowired
    private FileUploadService fileUploadService;

    public String uploadImage(MultipartFile multipartFile, HttpServletRequest request) throws IOException {
        Preconditions.checkArgument(null != multipartFile, "文件参数不能为空");
        long contentLength = multipartFile.getSize();
        String contentType = multipartFile.getContentType();
        String originalFilename = multipartFile.getOriginalFilename();
        checkFile(originalFilename);
        return fileUploadService.putFile(originalFilename, multipartFile.getInputStream(), contentLength, contentType);
    }

    public void checkFile(String fileName) {
        /* 严格意义上讲，图片上传需要进行安全检测, 但是作为域内服务暂不处理. */
        if (Strings.isNullOrEmpty(fileName)) {
            throw new CommonException("文件名称为空, 请设置文件名称");
        }
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            throw new CommonException("文件名称不符合规则, 请设置文件名后缀");
        }
    }

}
