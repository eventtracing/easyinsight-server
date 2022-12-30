package com.netease.eis.adapters;

import java.io.InputStream;

public interface FileUploadAdapter {

    /**
     * 将一个文件输入流上传，并返回一个图片地址
     * @param key 文件唯一Key
     * @param inputStream 文件流输入
     * @param contentLength 文件大小
     * @param contentType 内容类型
     * @return 文件唯一HTTP访问地址
     */
    String put(String key, InputStream inputStream, Long contentLength, String contentType);
}
