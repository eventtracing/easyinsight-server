package com.netease.hz.bdms.easyinsight.dao.model;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import java.util.Date;

@Data
@Accessors(chain = true)
public class EisAuditPackageInfo {
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
    /**
     * 执行时间，日期格式为yyyy-MM-dd HH:mm:ss
     */
    private Date createTime;
    /**
     * 更新时间，日期格式为yyyy-MM-dd HH:mm:ss
     */
    private Date updateTime;

    /**
     * 扩展字段
     */
    private String ext;

}
