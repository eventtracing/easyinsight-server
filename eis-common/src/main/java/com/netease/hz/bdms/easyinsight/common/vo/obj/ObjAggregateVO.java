package com.netease.hz.bdms.easyinsight.common.vo.obj;

import com.netease.hz.bdms.easyinsight.common.dto.common.CommonAggregateDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.CommonRelationAggregateDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 已上线对象管理模块 聚合信息
 *
 * @author: xumengqiang
 * @date: 2022/1/4 10:58
 */
@Data
@Accessors(chain = true)
public class ObjAggregateVO {
    /**
     * 终端信息
     */
    List<CommonAggregateDTO> terminals;

    /**
     * 发布信息
     */
    List<CommonRelationAggregateDTO> releases;

    /**
     * 类型
     */
    List<CommonAggregateDTO> types;

    /**
     * 标签
     */
    List<CommonAggregateDTO> tags;
}
