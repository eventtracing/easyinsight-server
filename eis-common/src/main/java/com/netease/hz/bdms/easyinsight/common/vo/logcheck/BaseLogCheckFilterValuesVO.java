package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import com.netease.hz.bdms.easyinsight.common.vo.auth.UserVO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 稽查统计报表统计信息
 */
@Accessors(chain = true)
@Data
public class BaseLogCheckFilterValuesVO {

    private List<VersionBuildUUIDVO> versions;
    private List<UserVO> packageOwners;
}
