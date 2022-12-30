package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OmTaskVO {

    private String taskIssueKey;

    private String name;

    private String versionName;

    private String reqIssueKey;

    private UserDTO owner;

    private UserDTO verifier;

}
