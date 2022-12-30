package com.netease.hz.bdms.easyinsight.common.dto.tag;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * @author: xumengqiang
 * @date: 2021/11/10 15:24
 */

@Data
@Accessors(chain = true)
public class SpmTagSimpleDTO {
    /**
     * SPM ID
     */
    private Long spmId;

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 创建者
     */
    private UserSimpleDTO creator;

    /**
     * 更新人
     */
    private UserSimpleDTO updater;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 更新时间
     */
    private Timestamp updateTime;
}
