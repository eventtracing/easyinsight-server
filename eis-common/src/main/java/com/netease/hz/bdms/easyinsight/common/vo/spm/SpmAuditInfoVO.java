package com.netease.hz.bdms.easyinsight.common.vo.spm;

import com.netease.hz.bdms.easyinsight.common.vo.logcheck.LogCheckLogVO;
import com.netease.hz.bdms.easyinsight.common.vo.logcheck.SpmInfoVO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * spm稽查结果
 */

@Data
@Accessors(chain = true)
public class SpmAuditInfoVO {
    /**
     * spm
     */
    private String spm;

    /**
     * 稽查通过数量
     */
    private long success;

    /**
     * 稽查失败数量
     */
    private long failed;

    /**
     * 稽查失败的spm信息，最多返回1000条
     */
    private List<LogCheckLogVO> failedSpmInfo;
}
