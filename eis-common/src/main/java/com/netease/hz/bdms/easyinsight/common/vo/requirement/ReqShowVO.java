package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import lombok.Data;

@Data
public class ReqShowVO {

    private Long reqId;

    private String reqIssueKey;

    private Integer from;

    private String name;

    private String team;

    private String views;

    private String businessArea;

    private String priority;

    private UserDTO creator;

    private String description;

}
