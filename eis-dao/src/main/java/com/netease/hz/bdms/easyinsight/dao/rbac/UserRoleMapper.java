package com.netease.hz.bdms.easyinsight.dao.rbac;


import com.netease.hz.bdms.easyinsight.dao.model.rbac.UserRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserRoleMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UserRole record);

    int insertSelective(UserRole record);

    UserRole selectByPrimaryKey(Long id);

    UserRole selectByUserIdAndRoleId(Long userId, Long roleId);

    int updateByPrimaryKeySelective(UserRole record);

    int updateByPrimaryKey(UserRole record);

    int insertBatch(@Param("userRoleListToInsert") List<UserRole> userRoleListToInsert);

    int deleteBatch(@Param("existedRelationList") List<UserRole> existedRelationList);


    /**
     * 查询用户在指定的范围下拥有的所有角色
     *
     * @param ranges 指定的范围
     * @return 用户和角色的关联关系(包含角色ID)
     */
    List<UserRole> selectRelationBetweenUserAndRoleInTargetRange(@Param("ranges") List<UserRole> ranges);

    /**
     * 查询用户和角色的关联关系,并携带角色信息
     *
     * @param roleType   角色类型
     * @param typeIdList 类型 ID 集合
     * @param roleLevel  角色等级
     * @param userIdList 用户 ID 集合
     * @return 用户和角色的关联关系
     */
    List<UserRole> selectRelationBetweenUserAndRole(
            @Param("roleId") Long roleId,
            @Param("roleType") Integer roleType,
            @Param("typeIdList") List<Long> typeIdList,
            @Param("roleLevel") Integer roleLevel,
            @Param("userIdList") List<Long> userIdList);


    int deleteRelationBatchBetweenUserAndRole(@Param("roleType") Integer roleType,
                                              @Param("typeIdList") List<Long> typeIdList,
                                              @Param("userIdList") List<Long> userIdList,
                                              @Param("roleId") Long roleId);

    int selectCount(@Param("roleType") Integer roleType, @Param("typeId") Long typeId);
}