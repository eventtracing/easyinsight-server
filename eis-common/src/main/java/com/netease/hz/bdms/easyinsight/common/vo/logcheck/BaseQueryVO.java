package com.netease.hz.bdms.easyinsight.common.vo.logcheck;

import com.netease.hz.bdms.easyinsight.common.enums.logcheck.LogCheckPackageTypeEum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;

@Accessors(chain = true)
@Data
public class BaseQueryVO {

    /**
     * 开始时间
     */
    private long timeStart;
    /**
     * 结束时间
     */
    private long timeEnd;
    /**
     * 按打包时间过滤，开始时间
     */
    private long packageTimeStart;
    /**
     * 按打包时间过滤，结束时间
     */
    private long packageTimeEnd;
    /**
     * 包类型
     * {@link LogCheckPackageTypeEum}
     */
    private Integer packageType;
    /**
     * 包类型
     * {@link LogCheckPackageTypeEum}
     */
    private Set<Integer> packageTypes;

    /**
     * 大版本号
     */
    private String version;

    /**
     * build号
     */
    private List<String> buildUUIDs;

    /**
     * 规则版本
     */
    private Long ruleVersion;

    /**
     * 负责人
     */
    private String owner;

}
