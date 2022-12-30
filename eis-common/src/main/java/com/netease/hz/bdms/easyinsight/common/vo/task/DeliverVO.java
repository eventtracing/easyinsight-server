package com.netease.hz.bdms.easyinsight.common.vo.task;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import lombok.Data;

import java.util.Set;

@Data
public class DeliverVO {

    /**
     * @see com.netease.hz.bdms.easyinsight.common.enums.DeliverTypeEnum
     */
    String deliverType;

    Set<Long> taskIds;

    Set<Long> processIds;

    UserDTO userDTO;

}
