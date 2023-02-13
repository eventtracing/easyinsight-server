package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class TaskEntityVO {

    private Long id;

    private String taskIssueKey;

    private String name;

    private String reqIssueKey;

    private Long terminalId;

    private UserSimpleDTO owner;

    private UserSimpleDTO verifier;

    private String versionName;

}
