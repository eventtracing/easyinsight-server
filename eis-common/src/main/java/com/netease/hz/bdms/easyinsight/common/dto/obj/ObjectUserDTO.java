package com.netease.hz.bdms.easyinsight.common.dto.obj;

import lombok.Data;
import lombok.experimental.Accessors;
import java.util.List;


@Data
@Accessors(chain = true)
public class ObjectUserDTO {
    /**
     * 录入列表
     */
    private List<UserPointInfoDTO> userPointInfoDTOS;

}
