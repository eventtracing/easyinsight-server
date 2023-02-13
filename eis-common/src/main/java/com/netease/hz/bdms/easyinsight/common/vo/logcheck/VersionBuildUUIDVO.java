package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
public class VersionBuildUUIDVO {

    private String version;
    private List<String> buildUUIDs;
}
