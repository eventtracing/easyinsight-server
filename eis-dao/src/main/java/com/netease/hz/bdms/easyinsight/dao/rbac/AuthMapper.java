package com.netease.hz.bdms.easyinsight.dao.rbac;


import com.netease.hz.bdms.easyinsight.dao.model.rbac.Auth;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AuthMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Auth record);

    int insertSelective(Auth record);

    Auth selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Auth record);

    int updateByPrimaryKey(Auth record);

    List<Auth> selectByCodes(@Param("authCodeList") List<Integer> authCodeList);

    /**
     * 查询角色拥有的权限
     *
     * @param roleIdList 角色 ID 集合
     * @return
     */
    List<Auth> selectByRoleIdList(@Param("roleIdList") List<Long> roleIdList);

    /**
     * 根据主键集合查询权限
     * @param authIdList 主键集合
     * @return 权限集合
     */
    List<Auth> selectByPrimaryKeyList(@Param("authIdList")List<Long> authIdList);
}