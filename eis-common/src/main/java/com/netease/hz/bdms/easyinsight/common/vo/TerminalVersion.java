package com.netease.hz.bdms.easyinsight.common.vo;

import com.netease.hz.bdms.easyinsight.common.enums.VersionSourceEnum;
import com.netease.hz.bdms.easyinsight.common.enums.VersionSourceStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class TerminalVersion {
    /**
     * 版本名称
     */
    private String name;

}
