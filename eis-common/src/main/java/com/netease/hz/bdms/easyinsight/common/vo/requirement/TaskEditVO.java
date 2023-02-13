package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import lombok.Data;

@Data
public class TaskEditVO {

    Long id;

    String taskIssueKey;

    String reqIssueKey;

    String name;

    Long terminalId;

    UserDTO owner;

    UserDTO verifier;

}
