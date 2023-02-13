package com.netease.hz.bdms.easyinsight.common.vo.param;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamSimpleDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ParamPoolItemVO {
    /**
     * 参数池中候选参数ID
     */
    private Long id;

    /**
     * 参数名
     */
    private String code;
    /**
     * 参数类型
     *
     * @see com.netease.hz.bdms.easyinsight.common.enums.ParamTypeEnum
     */
    private Integer paramType;
    /**
     * appId
     */
    private Long appId;
    /**
     * 创建人
     */
    private UserSimpleDTO creator;
    /**
     * 最近更新人
     */
    private UserSimpleDTO updater;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 最近更新时间
     */
    private Long updateTime;

    /**
     * 参数池下参数列表
     */
    private List<ParamSimpleDTO> params;
}
