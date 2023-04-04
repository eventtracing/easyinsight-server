package com.netease.hz.bdms.easyinsight.common.dto.compass;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单次鉴权请求dto
 * @date 2022/06/16
 * @since 2022-06-16 14:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifySingleDTO {
    private String user;
    private String application;
    private String tenant;
    private String product;
    private String action;
    private String resourceType;
    private String resourceCode;
}
