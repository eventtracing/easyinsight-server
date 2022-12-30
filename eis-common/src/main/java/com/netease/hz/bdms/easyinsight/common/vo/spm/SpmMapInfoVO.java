package com.netease.hz.bdms.easyinsight.common.vo.spm;

import com.netease.hz.bdms.easyinsight.common.dto.common.CommonAggregateDTO;
import com.netease.hz.bdms.easyinsight.common.enums.ProcessStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 聚合对象 查询`eis_spm_map_relation`和`eis_spm_map_info`表聚合而来
 *
 * @author: xumengqiang
 * @date: 2021/11/9 16:35
 */

@Data
@Accessors(chain = true)
public class SpmMapInfoVO {
    /**
     * 自增ID `spm_map_info`表主键
     */
    private Long id;

    /**
     * 新spm
     */
    private String spm;

    /**
     * 由父到子的spm串, 用于排序
     */
    private String spmReverse;

    /**
     * 旧spm列表
     */
    private List<String> spmOldList;

    /**
     * spm名称
     */
    private String name;

    /**
     * spm标签
     */
    private List<CommonAggregateDTO> tags;

    /**
     * 映射状态
     * @see com.netease.hz.bdms.easyinsight.common.enums.SpmMapStatusEnum
     */
    private Integer mapStatus;

    /**
     * spm 当前最新状态
     * @see ProcessStatusEnum
     */
    private Integer spmStatus;

    /**
     * SPM 是否已上过线
     */
    private Boolean isDeployed;

    /**
     * 映射生效版本
     */
    private String version;

    /**
     * 备注
     */
    private String note;

    /**
     * 来源 0-同步 1-手动添加
     */
    private Integer source;
}
