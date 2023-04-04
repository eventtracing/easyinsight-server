package com.netease.hz.bdms.easyinsight.common.dto.param.parambind;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 对象空值率设置
 * @author: yangyichun
 * @date: 2023/03/21 17:28
 */
@Data
@Accessors(chain = true)
public class ParamEmptyRateDTO {
    /**
     * id
     */
    private Long id;
    /**
     * 对象Id
     */
    private Long objId;
    /**
     * 参数id
     */
    private Long paramId;
    /**
     * 非空率
     */
    private Integer setRate;
    /*
     * 稽查非空率
     */
    private Integer auditRate;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 最近更新时间
     */
    private Long updateTime;
}
