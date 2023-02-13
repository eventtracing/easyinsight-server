package com.netease.hz.bdms.easyinsight.common.vo.event;

import com.netease.hz.bdms.easyinsight.common.dto.common.CommonAggregateDTO;
import lombok.Data;

import java.util.List;

/**
 * 需求管理模块——埋点事件池—新建事件埋点页面
 *
 * @author: xumengqiang
 * @date: 2022/1/20 15:57
 */
@Data
public class EventAggregateInfoVO {
    /**
     * 终端信息
     */
    List<CommonAggregateDTO> terminals;
}
