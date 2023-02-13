package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OmReqVO {

    private String reqIssueKey;

    private String name;

    private String priority;

    private String team;

    private String businessArea;

    private String views;

    private Integer omState;

    private UserDTO creator;

    private List<UserDTO> dataOwners;

    private UserDTO reporter;

    private String description;

    private String issueUrl;

}
