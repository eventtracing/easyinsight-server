package com.netease.hz.bdms.easyinsight.common.vo.spm;

import com.netease.hz.bdms.easyinsight.common.enums.ProcessStatusEnum;
import com.netease.hz.bdms.easyinsight.common.vo.PageBaseReqVO;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/11/11 15:11
 */

@Data
@Accessors(chain = true)
public class SpmMapInfoListQueryVO extends PageBaseReqVO {
    /**
     * 终端名称
     */
    @NotBlank(message = "终端不能为空")
    private Long terminalId;

    /**
     * 是否配置老埋点映射
     */
    private Boolean isMapped;

    /**
     * 映射状态
     * @see com.netease.hz.bdms.easyinsight.common.enums.SpmMapStatusEnum
     */
    private List<Integer> mapStatus;

    /**
     * spm状态
     * @see ProcessStatusEnum
     */
    private Integer spmStatus;

    /**
     * spm标签
     */
    private Long tagId;

    /**
     * 老SPM/映射生效版本
     */
    private String spmOldOrMapVersion;

    /**
     * spm或中文名称
     */
    private String spmOrName;

}
