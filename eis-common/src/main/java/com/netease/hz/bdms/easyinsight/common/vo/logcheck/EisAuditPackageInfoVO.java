package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import lombok.Data;

@Data
public class EisAuditPackageInfoVO {

    /**
     * 自增ID
     */
    private Long id;
    /*
     * 终端类型
     */
    private Long terminalId;
    /*
     * 应用id
     */
    private Long appId;
    /*
     * 版本号
     */
    private String versionId;
    /*
     * 包类型,1-全量/灰度 2-测试
     */
    private Long packageType;
    /*
     * 打包buildid
     */
    private String buildUUID;
    /*
     * 关联的需求列表
     */
    private String relatedReq;
    /*
     * 基准id
     */
    private Long auditId;
    /*
     * 责任人
     */
    private String userInfo;
}
