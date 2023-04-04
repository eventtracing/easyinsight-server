package com.netease.hz.bdms.easyinsight.common.dto.compass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author wangyongliang
 * @version 1.0.0
 * @ClassName BatchCheckPrivilegeDTO.java
 * @Description 批量鉴权接口
 * @createTime 2023/3/28 11:23
 */
@Data
public class BatchCheckPrivilegeDTO {
    /**
     * 资源类型
     */
    private String resourceType;
    /**
     * 资源列表
     */
    private String[] resources;

    /**
     * 业务线code
     */
    private String bizCode;

}
