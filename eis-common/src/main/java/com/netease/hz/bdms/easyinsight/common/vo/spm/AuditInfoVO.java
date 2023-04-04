package com.netease.hz.bdms.easyinsight.common.vo.spm;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 稽查结果
 */

@Data
@Accessors(chain = true)
public class AuditInfoVO {

    /**
     * 稽查spm信息详情
     */
    private List<SpmAuditInfoVO> spmAuditInfos;

    /**
     * 稽查跳转链接
     */
    private String targetUrl;
}
