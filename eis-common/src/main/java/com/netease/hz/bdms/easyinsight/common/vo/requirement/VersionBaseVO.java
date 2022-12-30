package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import com.netease.hz.bdms.easyinsight.common.enums.VersionSourceStatusEnum;
import com.netease.hz.bdms.easyinsight.common.enums.VersionSourceEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class VersionBaseVO {

    /**
     * 来源名字
     */
    private String sourceName;

    /**
     * 来源 {@link VersionSourceEnum}
     */
    private Integer source;

    /**
     * 来源状态 {@link VersionSourceStatusEnum}
     */
    private Integer sourceStatus;

    /**
     * 来源状态的描述 {@link VersionSourceStatusEnum}
     */
    private String sourceStatusDesc;

    /**
     * 来源创建时间
     */
    private Date sourceCreateTime;

    /**
     * 计划发布时间
     */
    private String planReleaseTime;
}
