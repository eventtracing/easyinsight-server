package com.netease.hz.bdms.easyinsight.common.vo.requirement;

import lombok.Data;
import com.netease.hz.bdms.easyinsight.common.enums.ReqPoolTypeEnum;

@Data
public class AssignEntityVO {

    Long id;

    /**
     * @see ReqPoolTypeEnum
     */
    Integer poolType;

}
