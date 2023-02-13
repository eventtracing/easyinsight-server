package com.netease.hz.bdms.easyinsight.common.dto.common;

import lombok.Data;

import java.util.List;

/**
 * 持有一些 UserSimpleDTO 集合的 DTO 对象
 *
 * @author wangliangyuan
 * @date 2021-08-24 下午 03:57
 */
@Data
public abstract class BaseUserListHolderDTO {
    /**
     * ID
     */
    private Long id;

    /**
     * 负责人
     */
    private UserSimpleDTO owner;

    /**
     * 管理员
     */
    private List<UserSimpleDTO> admins;

}
