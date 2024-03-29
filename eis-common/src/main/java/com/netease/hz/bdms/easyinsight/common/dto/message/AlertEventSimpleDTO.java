package com.netease.hz.bdms.easyinsight.common.dto.message;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: xumengqiang
 * @date: 2021/10/15 17:36
 * @see: http://doc.hz.netease.com/pages/viewpage.action?pageId=164375781 报警类型
 */
@Data
@Accessors(chain = true)
public class AlertEventSimpleDTO {
    // 服务名称
    private String service;

    // 组件名称
    private String component;

    // 具体事件类型
    private String type;
}
