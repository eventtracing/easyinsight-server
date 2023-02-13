package com.netease.hz.bdms.easyinsight.common.dto.obj.overmind;

import lombok.Data;

@Data
public class OvermindVersionDTO {

    private Integer id;
    private String name;
    private String creatorName;
    private long releasePlanTime;
    private String releaseActualTime;
    private String releaseSubmitTime;
    private String moduleName;
    private Integer status;
    private Long createdAt;
    private Long updatedAt;
}
