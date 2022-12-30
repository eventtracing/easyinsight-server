package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
public class RequirementInfoVO {

    private Long reqId;

    private String reqIssueKey;

    private Integer from;

    private String name;

    private String priority;

    private String team;

    private String businessArea;

    private String views;

    private Integer omState;

    private UserDTO creator;

    private String desc;

}
