package com.netease.hz.bdms.easyinsight.common.dto.message;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: xumengqiang
 * @date: 2021/10/18 9:46
 */
@Data
@Accessors(chain = true)
public class ChannelSimpleDTO {
    // 通道ID
    private Integer channel_id;

    // 标题
    private String title;

    // 内容
    private String content;

}
