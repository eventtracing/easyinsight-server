package com.netease.hz.bdms.easyinsight.dao.rbac;


import com.netease.hz.bdms.easyinsight.dao.model.rbac.Role;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoleMapper {

    List<Role> select(Role role);

    int deleteByPrimaryKey(Long id);

    int insert(Role record);

    int insertSelective(Role record);

    Role selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Role record);

    int updateByPrimaryKey(Role record);

    int insertBatch(@Param("defaultRoleList") List<Role> defaultRoleList);

    /**
     * 根据条件查询角色集合(包含内置角色),不区分用户
     *
     * @param roleLevelList  角色等级集合
     * @param roleType       角色类型
     * @param typeIdList     类型 ID 集合
     * @param roleName       角色名称
     * @param containedBuilt true - 查询结果包含内置角色; false - 不包含内置角色
     * @return 角色集合
     */
    List<Role> selectByExample(@Param("roleLevelList") List<Integer> roleLevelList,
                               @Param("roleType") Integer roleType,
                               @Param("typeIdList") List<Long> typeIdList,
                               @Param("roleName") String roleName,
                               @Param("containedBuilt") boolean containedBuilt);
}