package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import com.netease.hz.bdms.easyinsight.common.enums.CauseCheckResultEnum;
import com.netease.hz.bdms.easyinsight.common.enums.LogCheckResultEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
public class LogQueryVO extends BaseQueryVO {

    private String eventCode;
    private List<String> eventCodes;
    private List<String> oids;
    private List<String> spms;
    private List<String> referSpms;
    private List<String> bizRefers;
    private String objectPriority;
    private Long objectTagId;
    /**
     * {@link LogCheckResultEnum}
     */
    private Integer checkResult;
    /**
     * {@link CauseCheckResultEnum}
     */
    private Integer causeCheckResult;

    private String referType;

    private String uid;

    private String failKey;
}
