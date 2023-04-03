package com.netease.hz.bdms.easyinsight.dao.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class EisBuildRuleResource {
    /**
     * 自增ID
     */
    private Long id;
    /*
     * builduuid
     */
    private String buildUUid;
    /*
     * 埋点发布版本
     */
    private Long ruleVerion;
    /**
     * 执行时间，日期格式为yyyy-MM-dd HH:mm:ss
     */
    private Date createTime;
    /**
     * 更新时间，日期格式为yyyy-MM-dd HH:mm:ss
     */
    private Date updateTime;

}
