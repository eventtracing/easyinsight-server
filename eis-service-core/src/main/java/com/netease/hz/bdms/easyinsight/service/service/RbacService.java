package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.common.BaseUserListHolderDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingResultDTO;
import com.netease.hz.bdms.easyinsight.common.dto.rbac.MenuNodeDTO;
import com.netease.hz.bdms.easyinsight.common.dto.rbac.RoleApplyDTO;
import com.netease.hz.bdms.easyinsight.common.dto.rbac.RoleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleLevelEnum;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleTypeEnum;
import com.netease.hz.bdms.easyinsight.common.param.auth.MemberListPageableParam;
import com.netease.hz.bdms.easyinsight.common.param.auth.MemberListParam;
import com.netease.hz.bdms.easyinsight.common.param.auth.RoleAuthParam;
import com.netease.hz.bdms.easyinsight.common.param.auth.RoleCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.auth.RoleUpdateParam;
import com.netease.hz.bdms.easyinsight.common.param.auth.UserRoleRelationCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.auth.UserRoleRelationDeleteParam;
import com.netease.hz.bdms.easyinsight.common.param.auth.UserRoleRelationUpdateParam;
import com.netease.hz.bdms.easyinsight.common.vo.auth.UserVO;
import com.netease.hz.bdms.easyinsight.dao.model.rbac.Auth;
import com.netease.hz.bdms.easyinsight.dao.model.rbac.Role;
import com.netease.hz.bdms.easyinsight.dao.model.rbac.UserRole;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author wangliangyuan
 * @date 2021-08-02 下午 04:18
 */
public interface RbacService {

    /*************************************** 成员管理 start ***********************************/

    /**
     * 分页获取成员列表
     *
     * @param pageableParam
     * @return
     */
    PagingResultDTO<UserVO> getUserByPage(MemberListPageableParam pageableParam);

    /**
     * 不分页的成员列表
     *
     * @param param
     * @return
     */
    List<UserVO> getUserList(MemberListParam param);

    /**
     * 添加成员
     *
     * @param createParam
     */
    void addUser(UserRoleRelationCreateParam createParam);

    /**
     * 更新成员
     *
     * @param updateParam
     */
    void updateUser(UserRoleRelationUpdateParam updateParam);

    /**
     * 移除成员
     *
     * @param param
     */
    void removeUser(UserRoleRelationDeleteParam param);

    /*************************************** 成员管理 end ***********************************/


    /*************************************** 角色管理 start ***********************************/

    /**
     * 获取域或产品下的角色列表
     *
     * @param range
     * @return
     */
    List<RoleDTO> getRoleList(Integer range);

    /**
     * 添加角色
     *
     * @param createParam
     */
    Long addRole(RoleCreateParam createParam);

    /**
     * 给角色分配权限
     *
     * @param roleAssignParam
     */
    void assignFunctionToRole(RoleAuthParam roleAssignParam);

    /**
     * 删除角色
     *
     * @param roleId
     */
    void deleteRole(Long roleId);

    /**
     * 更新角色
     *
     * @param updateParam
     */
    void updateRole(RoleUpdateParam updateParam);

    /*************************************** 角色管理 end ***********************************/


    /*************************************** 权限管理 start ***********************************/

    /**
     * 获取权限树(包含选中状态)
     *
     * @param roleId
     * @return
     */
    List<MenuNodeDTO> getMenuTree(Long roleId);

    /**
     * 获取用户在当前产品下的权限集合
     *
     * @param appId
     * @return
     */
    List<MenuNodeDTO> getMenuTreeOfUser(Long appId);

    /**
     * 用户申请产品角色权限
     * @param appId    产品ID
     * @param roleId   角色ID
     * @param desc     原因说明
     * @return
     */
    boolean applyRolePermission(Long appId, Long roleId, String desc);

    /**
     * 审批产品角色权限申请
     * @param applyId   申请ID
     * @param type      操作类型
     * @return
     */
    boolean auditRolePermission(Long applyId, Integer type);

    /**
     * 获取产品权限申请列表
     * @param appId    产品ID
     * @return
     */
    List<RoleApplyDTO> getApplyList(Long appId, Integer status);

    /**
     * 判断用户在当前域或产品下是否有指定的权限
     *
     * @param requiredAuthList 需要的权限
     * @param userId           用户ID
     * @param domainId         域ID
     * @param appId            产品ID
     */
    boolean authorize(Set<Auth> requiredAuthList, Long userId, Long domainId, Long appId);

    /*************************************** 权限管理 end ***********************************/


    /*************************************** 其它方法 start ***********************************/

    /**
     * 查询用户拥有的角色集合
     *
     * @param userId   用户ID
     * @param domainId 域ID
     * @param appId    产品ID
     * @return 用户和角色的关联关系(包含角色ID)
     */
    List<UserRole> getRelationBetweenUserAndRoleInTargetRanges(Long userId, Long domainId, Long appId);

    /**
     * 根据条件获取用户和角色的关联关系
     *
     * @param userId        用户 ID
     * @param roleTypeEnum  角色类型
     * @param typeIdList    类型 ID 集合
     * @param roleLevelEnum 角色等级
     * @return 用户和角色的关联关系
     */
    List<UserRole> getRelationBetweenUserAndRole(Long userId,
                                                 RoleTypeEnum roleTypeEnum,
                                                 List<Long> typeIdList,
                                                 RoleLevelEnum roleLevelEnum);

    /**
     * 给用户设置新的角色集合
     *
     * @param userId        用户 ID
     * @param roleTypeEnum  角色类型
     * @param typeId        类型 ID
     * @param newRoleIdList 新的角色集合
     * @param addOnly       true - 只是在旧数据的基础上新增; false - 删除旧数据, 新增新数据, 但保留新旧数据的交集
     */
    void setNewRolesForUser(Long userId, List<Long> newRoleIdList, RoleTypeEnum roleTypeEnum, Long typeId, boolean addOnly);

    /**
     * 给角色设置新的用户集合
     * (例如将某个域下的管理员换一批人)
     *
     * @param userIdList   用户 ID 集合
     * @param roleId       角色 ID
     * @param roleTypeEnum 角色类型
     * @param typeId       类型 ID
     * @param addOnly      true - 只是在旧数据的基础上新增; false - 删除旧数据, 新增新数据, 但保留新旧数据的交集
     */
    void setNewUserListForRole(Long roleId, List<Long> userIdList, RoleTypeEnum roleTypeEnum, Long typeId, boolean addOnly);

    /**
     * 根据条件查询角色(包含内置角色)
     *
     * @param roleLevelEnums 角色类型集合
     * @param roleTypeEnum   角色类型
     * @param typeIdList     类型 ID 集合
     * @param roleName       角色名称
     * @param containedBuilt true - 查询结果包含内置角色; false - 不包含内置角色
     * @return 角色集合
     */
    List<Role> searchRole(EnumSet<RoleLevelEnum> roleLevelEnums,
                          RoleTypeEnum roleTypeEnum,
                          List<Long> typeIdList,
                          String roleName,
                          boolean containedBuilt);

    /**
     * 为 对象集合 设置指定 角色等级 下的用户集合,
     * 例如为产品设置拥有 产品管理员 这个角色的用户集合
     *
     * @param list 对象集合, 必须继承 {@link BaseUserListHolderDTO}
     * @param roleTypeEnum 角色范围
     * @param roleLevelEnum 角色等级
     * @param <D> 继承 {@link BaseUserListHolderDTO} 的 DTO 对象
     * @return
     */
    <D extends BaseUserListHolderDTO> List<D> setUserDTOListOfRole(List<D> list,
                                                                   RoleTypeEnum roleTypeEnum,
                                                                   RoleLevelEnum roleLevelEnum);

    /**
     * 判断指定范围内是否有成员
     *
     * @param range  范围
     * @param typeId 范围ID(平台ID/域ID/产品ID)
     * @return true-有; false-无
     */
    boolean checkIsHaveMember(RoleTypeEnum range, Long typeId);

    /*************************************** 其它方法 end ***********************************/

}
