package com.netease.hz.bdms.easyinsight.common.dto.compass;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author wangyongliang
 * @version 1.0.0
 * @ClassName PrivilegeListDTO.java
 * @Description 鉴权
 * @createTime 2023/3/28 11:23
 */
@Data
public class PrivilegeListDTO {
    /**
     * spm
     */
    private Map<String,String> spmNo;
    /**
     * 是否全选
     */
    private Boolean isAll;
    /**
     * 资源类型
     */
    private String resourceType;
    /**
     * 资源列表
     */
    private List<Map<String,String>> resourceList;
    /**
     * 业务线code
     */
    private String bizCode;


}
