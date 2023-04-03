package com.netease.hz.bdms.easyinsight.dao.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class ParamRuleAudit {
    /**
     * 自增ID
     */
    private Long id;
    /*
     * 对象id
     */
    private Long objId;
    /*
     * 参数id
     */
    private Long paramId;
    /*
     * 设置非空率
     */
    private Integer setRate;
    /**
     * 执行时间，日期格式为yyyy-MM-dd HH:mm:ss
     */
    private Date createTime;
    /**
     * 更新时间，日期格式为yyyy-MM-dd HH:mm:ss
     */
    private Date updateTime;

}
