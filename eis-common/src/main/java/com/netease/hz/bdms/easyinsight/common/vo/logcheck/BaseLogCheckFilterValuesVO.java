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

    /**
     * 版本号及对应包信息
     */
    private List<VersionBuildUUIDVO> versions;
    /**
     * 包责任人
     */
    private List<UserVO> packageOwners;
    /**
     * 包规则版本
     */
    private List<RuleVersionVO> ruleVersions;

}