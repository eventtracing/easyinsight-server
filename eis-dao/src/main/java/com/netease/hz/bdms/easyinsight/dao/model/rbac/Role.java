package com.netease.hz.bdms.easyinsight.dao.model.rbac;

import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleLevelEnum;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleTypeEnum;
import lombok.Data;

import java.sql.Timestamp;

/**
 * 角色表 eis_role 对应的实体类
 *
 * @author wangliangyuan
 * @date 2021-08-02 下午 02:21
 */
@Data
public class Role {
    /**
     * 自增id
     */
    private Long id;

    /**
     * 角色名
     */
    private String roleName;

    /**
     * 角色等级
     * {@link RoleLevelEnum}
     */
    private Integer roleLevel;

    /**
     * 角色类型
     * {@link RoleTypeEnum}
     */
    private Integer roleType;

    /**
     * 类型ID
     * 如果type为app，则为appId，为domain则为domainId，为platform则默认为-1
     */
    private Long typeId;

    /**
     * 是否是内置角色
     * true-内置角色，false-自定义角色(非内置角色)
     */
    private Boolean builtin;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 更新时间
     */
    private Timestamp updateTime;
}