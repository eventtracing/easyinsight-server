package com.netease.hz.bdms.easyinsight.dao.rbac;

import com.netease.hz.bdms.easyinsight.dao.model.rbac.RoleAuth;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoleAuthMapper {

    int deleteByPrimaryKey(Long id);

    int insert(RoleAuth record);

    int insertSelective(RoleAuth record);

    RoleAuth selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RoleAuth record);

    int updateByPrimaryKey(RoleAuth record);

    List<RoleAuth> selectByRoleId(Long roleId);

    int deleteByRoleIdAndAuthId(@Param("roleId") Long roleId, @Param("authIdList") List<Long> authIdList);

    int insertBatch(@Param("roleAuthList") List<RoleAuth> roleAuthList);
}