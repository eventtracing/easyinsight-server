package com.netease.hz.bdms.easyinsight.common.vo.obj;

import com.netease.hz.bdms.easyinsight.common.dto.common.CommonAggregateDTO;
import lombok.Data;

import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2022/1/17 19:27
 */
@Data
public class ObjCascadeAggregateVO {
    /**
     * 终端信息
     */
    List<CommonAggregateDTO> terminals;
}
