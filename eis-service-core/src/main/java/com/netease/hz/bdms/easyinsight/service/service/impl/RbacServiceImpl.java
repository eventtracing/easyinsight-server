package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageSerializable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.constant.DemoConst;
import com.netease.hz.bdms.easyinsight.common.constant.GlobalConst;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.BaseUserListHolderDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingResultDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.rbac.MenuNodeDTO;
import com.netease.hz.bdms.easyinsight.common.dto.rbac.RoleApplyDTO;
import com.netease.hz.bdms.easyinsight.common.dto.rbac.RoleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.PermissionAuditEnum;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.AuthType;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleLevelEnum;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleTypeEnum;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.exception.UserManagementException;
import com.netease.hz.bdms.easyinsight.common.param.auth.MemberListPageableParam;
import com.netease.hz.bdms.easyinsight.common.param.auth.MemberListParam;
import com.netease.hz.bdms.easyinsight.common.param.auth.MenuFunctionParam;
import com.netease.hz.bdms.easyinsight.common.param.auth.RoleAuthParam;
import com.netease.hz.bdms.easyinsight.common.param.auth.RoleCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.auth.RoleUpdateParam;
import com.netease.hz.bdms.easyinsight.common.param.auth.UserRoleRelationCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.auth.UserRoleRelationDeleteParam;
import com.netease.hz.bdms.easyinsight.common.param.auth.UserRoleRelationUpdateParam;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.common.util.TreeUtil;
import com.netease.hz.bdms.easyinsight.common.vo.auth.RoleVO;
import com.netease.hz.bdms.easyinsight.common.vo.auth.UserVO;
import com.netease.hz.bdms.easyinsight.dao.AppMapper;
import com.netease.hz.bdms.easyinsight.dao.EisPermissionApplyRecordMapper;
import com.netease.hz.bdms.easyinsight.dao.model.App;
import com.netease.hz.bdms.easyinsight.dao.model.EisPermissionApplyRecord;
import com.netease.hz.bdms.easyinsight.dao.model.rbac.Auth;
import com.netease.hz.bdms.easyinsight.dao.model.rbac.Role;
import com.netease.hz.bdms.easyinsight.dao.model.rbac.RoleAuth;
import com.netease.hz.bdms.easyinsight.dao.model.rbac.User;
import com.netease.hz.bdms.easyinsight.dao.model.rbac.UserRole;
import com.netease.hz.bdms.easyinsight.dao.rbac.AuthMapper;
import com.netease.hz.bdms.easyinsight.dao.rbac.RoleAuthMapper;
import com.netease.hz.bdms.easyinsight.dao.rbac.RoleMapper;
import com.netease.hz.bdms.easyinsight.dao.rbac.UserMapper;
import com.netease.hz.bdms.easyinsight.dao.rbac.UserRoleMapper;
import com.netease.hz.bdms.easyinsight.service.service.RbacService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author wangliangyuan
 * @date 2021-08-02 下午 04:18
 */
@Slf4j
@Service
public class RbacServiceImpl implements RbacService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private AppMapper appMapper;

    @Autowired
    private RoleAuthMapper roleAuthMapper;

    @Autowired
    private AuthMapper authMapper;

    @Autowired
    private EisPermissionApplyRecordMapper eisPermissionApplyRecordMapper;

    @Override
    public PagingResultDTO<UserVO> getUserByPage(MemberListPageableParam pageableParam) {
        Integer currentPage = pageableParam.getCurrentPage();
        Integer pageSize = pageableParam.getPageSize();

        PagingResultDTO<UserVO> resultDTO = new PagingResultDTO<>();
        resultDTO.setTotalNum(0);
        resultDTO.setPageNum(currentPage);
        resultDTO.setList(Lists.newArrayList());

        RoleTypeEnum roleTypeEnum = RoleTypeEnum.match(pageableParam.getRange());
        Integer roleType = roleTypeEnum.getCode();
        Long typeId = decideTypeIdByRoleType(roleTypeEnum);

        Long roleId = pageableParam.getRoleId();
        String orderBy = pageableParam.getOrderBy();
        String orderRule = pageableParam.getOrderRule();
        String search = pageableParam.getSearch();

        // 设置分页参数
        PageHelper.startPage(currentPage, pageSize);
        // 查询用户列表
        List<User> userList = userMapper.selectByRange(roleType, typeId, roleId, orderBy, orderRule, search);
        if (CollectionUtils.isNotEmpty(userList)) {
            PageSerializable<User> userPageSerializable = PageSerializable.of(userList);
            resultDTO.setTotalNum(Long.valueOf(userPageSerializable.getTotal()).intValue());

            // 最终返回的用户列表数据
            List<UserVO> userVOList = Lists.newArrayListWithCapacity(pageSize);

            // 如果查询的是 域 或 产品 下的成员列表
            if (roleId == null) {
                // 用户 ID 集合
                List<Long> userIdList = userList.stream().map(User::getId).collect(Collectors.toList());

                // 查询用户和角色的关系
                List<UserRole> relationBetweenUserAndRole = userRoleMapper.selectRelationBetweenUserAndRole(
                        null, roleType, Lists.newArrayList(typeId), null, userIdList);
                // 用户和角色的关系 转成 map
                Map<Long, List<UserRole>> userRoleMap = relationBetweenUserAndRole.stream()
                        .collect(Collectors.groupingBy(UserRole::getUserId));

                Map<Long, App> appMap = null;
                // 用户所属的 app 的 map
                Map<Long, List<UserRole>> userAppMap = null;

                // 如果查询的是 域成员列表
                if (RoleTypeEnum.DOMAIN.equals(roleTypeEnum)) {
                    // 查询当前域下所有的产品集合
                    List<App> appListOfCurrentDomain = appMapper.selectByDomainId(typeId);
                    if (CollectionUtils.isNotEmpty(appListOfCurrentDomain)) {
                        appMap = appListOfCurrentDomain.stream()
                                .collect(Collectors.toMap(App::getId, Function.identity()));

                        List<Long> appIdList = Lists.newArrayList(appMap.keySet());
                        // 查询用户在 app 下拥有的角色集合
                        List<UserRole> userAppList = userRoleMapper.selectRelationBetweenUserAndRole(
                                null, RoleTypeEnum.APP.getCode(), appIdList, null, userIdList);
                        // 用户在 app 下拥有的角色集合 转成 map
                        userAppMap = userAppList.stream().collect(Collectors.groupingBy(UserRole::getUserId));
                    }
                }

                boolean userAppMapNotEmpty = MapUtils.isNotEmpty(userAppMap);

                for (User user : userList) {
                    Long userId = user.getId();

                    UserVO userVO = new UserVO();
                    userVO.setUserId(userId);
                    userVO.setEmail(user.getEmail());
                    userVO.setUserName(user.getUserName());
                    // 列表页的 "添加时间"
                    userVO.setCreateTime(user.getCreateTime());

                    List<UserRole> userRoles = userRoleMap.get(userId);
                    if (CollectionUtils.isNotEmpty(userRoles)) {
                        List<RoleVO> roleVOList = Lists.newArrayListWithCapacity(userRoles.size());
                        for (UserRole userRole : userRoles) {
                            RoleVO roleVO = new RoleVO();
                            roleVO.setId(userRole.getRoleId());
                            roleVO.setRoleName(userRole.getRoleName());

                            roleVOList.add(roleVO);

                            // userRole.getUpdateTime() 就是用户被添加到当前域或产品下的时间
                            userVO.setUpdateTime(userRole.getUpdateTime());
                        }
                        // 设置用户的角色集合
                        userVO.setRoles(roleVOList);
                    }

                    if (userAppMapNotEmpty) {
                        List<UserRole> list = userAppMap.get(userId);
                        if (CollectionUtils.isNotEmpty(list)) {
                            // 单个用户所属的产品名称集合
                            Set<String> appNameSet = Sets.newHashSet();

                            for (UserRole userRole : list) {
                                App app = appMap.get(userRole.getTypeId());
                                appNameSet.add(app.getName());
                            }
                            // 设置用户所属的 app 集合
                            userVO.setApps(appNameSet);
                        }
                    }

                    userVOList.add(userVO);
                }

                // 如果查询的是 角色 下的成员列表
            } else {

                for (User user : userList) {
                    UserVO userVO = new UserVO();
                    userVO.setUserId(user.getId());
                    userVO.setEmail(user.getEmail());
                    userVO.setUserName(user.getUserName());
                    userVO.setCreateTime(user.getCreateTime());
                    userVO.setUpdateTime(user.getUpdateTime());

                    userVOList.add(userVO);
                }
            }

            resultDTO.setList(userVOList);
        }

        return resultDTO;
    }

    @Override
    public List<UserVO> getUserList(MemberListParam param) {
        Long typeId = decideTypeIdByRoleType(RoleTypeEnum.match(param.getRange()));
        List<User> userList = userMapper.selectByRange(
                param.getRange(), typeId, null, null, null, null);
        if (CollectionUtils.isEmpty(userList)) {
            return Lists.newArrayList();
        }

        // 最终返回的用户列表数据
        List<UserVO> userVOList = Lists.newArrayListWithCapacity(userList.size());

        for (User user : userList) {
            UserVO userVO = new UserVO();
            userVO.setUserId(user.getId());
            userVO.setEmail(user.getEmail());
            userVO.setUserName(user.getUserName());

            userVOList.add(userVO);
        }

        return userVOList;
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public void addUser(UserRoleRelationCreateParam createParam) {
        RoleTypeEnum roleTypeEnum = RoleTypeEnum.match(createParam.getRange());
        Long typeId = decideTypeIdByRoleType(roleTypeEnum);

        // 当前 域/产品/角色 下已存在的成员集合
        List<User> existedUserList = userMapper.selectByRange(roleTypeEnum.getCode(), typeId, createParam.getCurrentRole(),
                "u.id", "ASC", null);
        // 转成 map, key = email ,value = 用户
        Map<String, User> existedUserMap = existedUserList.stream()
                .collect(Collectors.toMap(User::getEmail, Function.identity()));

        // 需要添加角色的用户 ID 集合
        List<Long> userIdListToAddRole = new ArrayList<>();
        // 需要新增的用户集合
        List<User> usersToInsert = new ArrayList<>();

        List<UserSimpleDTO> submittedUserList = createParam.getUsers();
        for (UserSimpleDTO submittedUser : submittedUserList) {
            String key = submittedUser.getEmail();
            boolean containsKey = existedUserMap.containsKey(key);
            if (containsKey) {
                User user = existedUserMap.get(key);
                String errorMessage = MessageFormat.format("该成员[名称:{0}, 邮箱:{1}]已添加,不能重复添加",
                        user.getUserName(), user.getEmail());
                throw new IllegalArgumentException(errorMessage);
            }

            User newUser = BeanConvertUtils.convert(submittedUser, User.class);
            // 不存在的用户需要新增到数据库
            usersToInsert.add(newUser);
        }

        log.info("{} users will be added to current domain or app", usersToInsert.size());

        if (CollectionUtils.isNotEmpty(usersToInsert)) {
            List<String> newEmailList = usersToInsert.stream().map(User::getEmail).collect(Collectors.toList());
            // 前端提交过来的这些邮箱可能已经存在,所以需要先查一次库
            List<User> users = userMapper.selectByEmails(newEmailList);
            // users 转成 map
            Map<String, User> userMap = users.stream().collect(Collectors.toMap(User::getEmail, Function.identity()));

            Iterator<User> iterator = usersToInsert.iterator();
            while (iterator.hasNext()) {
                User userToAdd = iterator.next();
                String key = userToAdd.getEmail();
                boolean containsKey = userMap.containsKey(key);
                // 如果 eis_user 表已经存在该邮箱,即存在该用户
                if (containsKey) {
                    // 添加 userId
                    userIdListToAddRole.add(userMap.get(key).getId());
                    // 这个用户不需要新增,移除
                    iterator.remove();

                    // 如果这个用户不存在 eis_user 表
                } else {
                    String userName = userToAdd.getUserName();
                    if (StringUtils.isBlank(userName)) {
                        userName = StringUtils.substringBefore(key, GlobalConst.AT);
                        userToAdd.setUserName(userName);
                    }
                }
            }

            log.info("{} users will be created", usersToInsert.size());

            if (CollectionUtils.isNotEmpty(usersToInsert)) {
                // 批量新增用户
                userMapper.insertBatch(usersToInsert);

                // 获取新增后的用户ID集合
                List<Long> newUserIdList = usersToInsert.stream().map(User::getId).collect(Collectors.toList());
                userIdListToAddRole.addAll(newUserIdList);
            }
        }

        log.info("{} users will be assigned role", userIdListToAddRole.size());

        // 如果没有用户需要分配角色
        if (CollectionUtils.isEmpty(userIdListToAddRole)) {
            // 结束逻辑
            return;
        }

        List<Long> roleIds = createParam.getRoleIds();

        switch (roleTypeEnum) {
            case APP: {
                if (CollectionUtils.isEmpty(roleIds)) {
                    throw new IllegalArgumentException("未指定角色");
                }

                // 查询 域普通用户 这个角色
                List<Role> roleList = this.searchRole(EnumSet.of(RoleLevelEnum.DOMAIN_NORMAL_USER),
                        RoleTypeEnum.DOMAIN, null, null, true);
                // 普通用户 这个角色的 ID
                Long roleIdOfDomainNormalUser = roleList.get(0).getId();

                Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);
                // 为产品里的角色添加用户后, 同时给用户添加 域普通用户 的角色
                this.setNewUserListForRole(roleIdOfDomainNormalUser, userIdListToAddRole,
                        RoleTypeEnum.DOMAIN, domainId, true);

                break;
            }
            case DOMAIN: {
                // 查询 域普通用户 这个角色
                List<Role> roleList = this.searchRole(EnumSet.of(RoleLevelEnum.DOMAIN_NORMAL_USER),
                        RoleTypeEnum.DOMAIN, null, null, true);
                // 域成员默认为 域普通用户 的角色
                roleIds = Lists.newArrayList(roleList.get(0).getId());
                break;
            }
            default: {
                // Do nothing
            }
        }

        for (int i = 0; i < roleIds.size(); i++) {
            Long roleId = roleIds.get(i);
            this.setNewUserListForRole(roleId, userIdListToAddRole, roleTypeEnum, typeId, true);
        }
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public void updateUser(UserRoleRelationUpdateParam updateParam) {
        Long userId = updateParam.getUserId();
        String newEmail = updateParam.getEmail();
        User user = userMapper.selectByEmail(newEmail);
        if (user != null && !user.getId().equals(userId)) {
            String tip = MessageFormat.format("邮箱[{0}]已经被占用", newEmail);
            throw new IllegalArgumentException(tip);
        }

        if (user == null) {
            user = userMapper.selectByPrimaryKey(userId);
            if (user == null) {
                throw new IllegalArgumentException("无效的用户ID:" + userId);
            }
        }

        user.setId(userId);

        String newUserName = updateParam.getUserName();
        if (StringUtils.isBlank(newUserName)) {
            newUserName = StringUtils.EMPTY;
        }
        // 只能更新用户名,邮箱不能更新
        user.setUserName(newUserName);
        userMapper.update(user);

        List<Long> newRoleIds = updateParam.getRoleIds();
        if (CollectionUtils.isNotEmpty(newRoleIds)) {
            RoleTypeEnum roleTypeEnum = RoleTypeEnum.match(updateParam.getRange());
            Long typeId = decideTypeIdByRoleType(roleTypeEnum);

            notAllowedToUpdateDomainPrincipalAtMemberPageOfDomain(newRoleIds, roleTypeEnum, typeId);

            this.setNewRolesForUser(userId, newRoleIds, roleTypeEnum, typeId, false);
        }

    }

    /**
     * 在域的成员列表页面, 不允许更新成员的身份为 {@link RoleLevelEnum#DOMAIN_PRINCIPAL}
     *
     * @param newRoleIdList
     * @param roleTypeEnum
     * @param typeId
     */
    private void notAllowedToUpdateDomainPrincipalAtMemberPageOfDomain(List<Long> newRoleIdList,
                                                                       RoleTypeEnum roleTypeEnum,
                                                                       Long typeId) {
        if (RoleTypeEnum.DOMAIN.equals(roleTypeEnum)) {
            // 查询 域负责人 这个角色
            List<Role> roleOfDomainPrincipal = this.searchRole(EnumSet.of(RoleLevelEnum.DOMAIN_PRINCIPAL),
                    roleTypeEnum, Collections.singletonList(typeId), null, true);
            // 域负责人 这个角色的 ID
            Long roleIdOfDomainPrincipal = roleOfDomainPrincipal.get(0).getId();
            if (newRoleIdList.contains(roleIdOfDomainPrincipal)) {
                String errorMessage = MessageFormat.format("不支持修改身份为[{0}]", RoleLevelEnum.DOMAIN_PRINCIPAL.getRoleName());
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public void removeUser(UserRoleRelationDeleteParam param) {
        RoleTypeEnum roleTypeEnum = RoleTypeEnum.match(param.getRange());
        Integer roleType = roleTypeEnum.getCode();

        Long typeId = decideTypeIdByRoleType(roleTypeEnum);

        List<Long> userIds = param.getUserIds();
        Long roleId = param.getRoleId();

        // 删除用户和角色的关联关系
        userRoleMapper.deleteRelationBatchBetweenUserAndRole(roleType, Collections.singletonList(typeId), userIds, roleId);

        // 如果是从 域管理-成员管理-移除成员 这个入口来移除成员
        if (RoleTypeEnum.DOMAIN.equals(roleTypeEnum)) {
            // 需要同时删除该域下所有产品和用户的关联, 因此需要先查询该域下的所有产品
            List<App> allApps = appMapper.selectByDomainId(typeId);

            if (CollectionUtils.isNotEmpty(allApps)) {
                List<Long> appIdListOfCurrentDomain = allApps.stream().map(App::getId).collect(Collectors.toList());
                // 删除该域下所有产品和用户的关联
                userRoleMapper.deleteRelationBatchBetweenUserAndRole(RoleTypeEnum.APP.getCode(),
                        appIdListOfCurrentDomain, userIds, roleId);
            }
        }

    }

    /**
     * 删除已存在的关联关系,保存新的关联关系
     *
     * @param existedRelationList 用户和角色已存在的关联关系
     * @param newRelationList     用户和角色新的关联关系
     */
    private void deleteExistedRelationAndSaveNewRelationBetweenUserAndRole(List<UserRole> existedRelationList,
                                                                           List<UserRole> newRelationList) {
        List<UserRole> tmp = new ArrayList<>(existedRelationList);
        // 求交集
        tmp.retainAll(newRelationList);

        if (CollectionUtils.isNotEmpty(tmp)) {
            // removeAll 后剩下的就是需要 删除 的数据
            existedRelationList.removeAll(tmp);

            // removeAll 后剩下的就是需要 新增 的数据
            newRelationList.removeAll(tmp);
        }

        if (CollectionUtils.isNotEmpty(existedRelationList)) {
            log.info("relation between user and role, delete:{}", existedRelationList.size());
            userRoleMapper.deleteBatch(existedRelationList);
        }

        if (CollectionUtils.isNotEmpty(newRelationList)) {
            log.info("relation between user and role, insert:{}", newRelationList.size());
            userRoleMapper.insertBatch(newRelationList);
        }

    }

    @Override
    public List<RoleDTO> getRoleList(Integer range) {
        RoleTypeEnum roleTypeEnum = RoleTypeEnum.match(range);
        Long typeId = decideTypeIdByRoleType(roleTypeEnum);

        List<Role> roleList = this.searchRole(null, roleTypeEnum, Lists.newArrayList(typeId),
                null, true);
        if (CollectionUtils.isEmpty(roleList)) {
            return Collections.emptyList();
        }

        return roleList.stream()
                .map(role -> BeanConvertUtils.convert(role, RoleDTO.class))
                .sorted(Comparator.comparingInt(RoleDTO::getRoleLevel))
                .collect(Collectors.toList());
    }

    @Override
    public Long addRole(RoleCreateParam createParam) {
        Long appId = createParam.getAppId();
        validateAppId(appId);

        RoleTypeEnum range = RoleTypeEnum.APP;
        String roleName = createParam.getRoleName();

        List<Role> roleList = this.searchRole(null, range, Lists.newArrayList(appId), roleName, true);
        if (CollectionUtils.isNotEmpty(roleList)) {
            throw new IllegalArgumentException("已存在的角色名称:" + roleName);
        }

        Role role = new Role();
        role.setRoleLevel(GlobalConst.DEFAULT_ROLE_LEVEL_OF_NOT_BUILTIN);
        role.setRoleName(roleName);
        role.setRoleType(range.getCode());
        role.setTypeId(appId);
        role.setBuiltin(false);
        role.setDescription(createParam.getDescription());
        roleMapper.insertSelective(role);
        return role.getId();
    }

    private void validateAppId(Long appId) {
        App app = appMapper.selectByPrimaryKey(appId);
        if (app == null) {
            throw new IllegalArgumentException("无效的产品ID:" + appId);
        }
        Long currentDomainId = EtContext.get(ContextConstant.DOMAIN_ID);
        if (!app.getDomainId().equals(currentDomainId)) {
            throw new IllegalArgumentException("无效的产品ID:" + appId);
        }
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public void assignFunctionToRole(RoleAuthParam roleAssignParam) {
        Long roleId = roleAssignParam.getRoleId();
        requireRoleNotBuiltin(roleId);

        // 当前角色已存在的权限集合
        List<RoleAuth> existedAuthList = roleAuthMapper.selectByRoleId(roleId);

        // 新的权限 ID 集合
        List<Long> newAuthIdList = roleAssignParam.getFunctions().stream()
                .map(MenuFunctionParam::getFunctionId).collect(Collectors.toList());

        List<RoleAuth> roleAuthListToInsert = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(newAuthIdList)) {
            List<Auth> authList = authMapper.selectByPrimaryKeyList(newAuthIdList);
            Set<Auth> authSetToInsert = Sets.newHashSet(authList);
            // 添加父节点
            addParentAuth(authSetToInsert, authList);

            roleAuthListToInsert = authSetToInsert.stream()
                    .map(auth -> {
                        RoleAuth roleAuth = new RoleAuth();
                        roleAuth.setRoleId(roleId);
                        roleAuth.setAuthId(auth.getId());
                        return roleAuth;
                    }).collect(Collectors.toList());
        }

        List<RoleAuth> tmp = new ArrayList<>(existedAuthList);
        // 求交集
        tmp.retainAll(roleAuthListToInsert);

        if (CollectionUtils.isNotEmpty(tmp)) {
            // removeAll 后剩下的就是需要 删除 的数据
            existedAuthList.removeAll(tmp);

            // removeAll 后剩下的就是需要 新增 的数据
            roleAuthListToInsert.removeAll(tmp);
        }

        log.info("{} roleAuths to delete", existedAuthList.size());
        if (CollectionUtils.isNotEmpty(existedAuthList)) {
            List<Long> authIdListToDelete = existedAuthList.stream().map(RoleAuth::getAuthId).collect(Collectors.toList());
            roleAuthMapper.deleteByRoleIdAndAuthId(roleId, authIdListToDelete);
        }

        log.info("{} roleAuths to insert", roleAuthListToInsert.size());
        if (CollectionUtils.isNotEmpty(roleAuthListToInsert)) {
            roleAuthMapper.insertBatch(roleAuthListToInsert);
        }
    }

    /**
     * 添加子权限的父权限
     *
     * @param authSetToInsert 需要新增的权限集合
     * @param subAuthList     子权限集合
     */
    private void addParentAuth(Set<Auth> authSetToInsert, List<Auth> subAuthList) {
        List<Integer> parentCodeList = subAuthList.stream()
                .map(Auth::getAuthParentCode)
                .filter(parentCode -> !GlobalConst.DEFAULT_PARENT_CODE_OF_ROOT_AUTH.equals(parentCode))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(parentCodeList)) {
            return;
        }
        List<Auth> parentAuthList = authMapper.selectByCodes(parentCodeList);
        authSetToInsert.addAll(parentAuthList);
        addParentAuth(authSetToInsert, parentAuthList);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public void deleteRole(Long roleId) {
        Role roleToDelete = requireRoleNotBuiltin(roleId);
        // 删除角色
        roleMapper.deleteByPrimaryKey(roleId);

        Integer roleType = roleToDelete.getRoleType();
        Long typeId = decideTypeIdByRoleType(RoleTypeEnum.match(roleType));

        // 删除该角色和用户的关联关系
        userRoleMapper.deleteRelationBatchBetweenUserAndRole(roleType, Collections.singletonList(typeId), null, roleId);
        // 删除该角色和权限的关联关系
        roleAuthMapper.deleteByRoleIdAndAuthId(roleId, null);
    }

    @Override
    public void updateRole(RoleUpdateParam updateParam) {
        Long roleId = updateParam.getId();
        Role role = requireRoleNotBuiltin(roleId);

        String newRoleName = updateParam.getRoleName();

        List<Role> roleList = this.searchRole(null, RoleTypeEnum.APP,
                Lists.newArrayList(role.getTypeId()), newRoleName, true);
        if (CollectionUtils.isNotEmpty(roleList)) {
            boolean match = roleList.stream().anyMatch(otherRole -> !otherRole.getId().equals(roleId));
            if (match) {
                throw new IllegalArgumentException("已存在的角色名称:" + newRoleName);
            }
        }

        role.setRoleName(newRoleName);
        role.setDescription(updateParam.getDescription());
        roleMapper.updateByPrimaryKeySelective(role);
    }

    /**
     * 要求正在操作的角色不是内置角色
     *
     * @param roleId 角色ID
     * @return
     */
    private Role requireRoleNotBuiltin(Long roleId) {
        Role role = roleMapper.selectByPrimaryKey(roleId);
        if (role == null) {
            throw new IllegalArgumentException("无效的角色ID:" + roleId);
        }
        if (role.getBuiltin()) {
            throw new IllegalArgumentException("内置角色不支持该操作");
        }
        return role;
    }

    @Override
    public List<MenuNodeDTO> getMenuTree(Long roleId) {
        Role currentRole = roleMapper.selectByPrimaryKey(roleId);
        if (currentRole == null) {
            throw new IllegalArgumentException("无效的角色ID:" + roleId);
        }

        RoleTypeEnum roleTypeEnum = RoleTypeEnum.match(currentRole.getRoleType());

        List<Long> typeIdList = null;
        Long typeId = currentRole.getTypeId();
        if (typeId != null) {
            typeIdList = new ArrayList<>(1);
            typeIdList.add(typeId);
        }

        // 查询所有角色(包含内置角色)
        List<Role> roleList = this.searchRole(null, roleTypeEnum, typeIdList, null, true);
        if (CollectionUtils.isEmpty(roleList)) {
            return Collections.emptyList();
        }

        List<Long> roleIdList = roleList.stream().map(Role::getId).collect(Collectors.toList());
        // 全量的权限(包含重复数据)
        List<Auth> authList = authMapper.selectByRoleIdList(roleIdList);
        // 用 Set 去重后的全量权限
        Set<Auth> allAuthSet = Sets.newLinkedHashSet(authList);

        // 当前角色已有的权限
        List<Auth> authListOfCurrentRole = authMapper.selectByRoleIdList(Lists.newArrayList(roleId));

        Map<Long, Auth> authMapOfCurrentRole = authListOfCurrentRole.stream()
                // 转成 map, key = authId
                .collect(Collectors.toMap(Auth::getId, Function.identity()));

        List<MenuNodeDTO> authorizedAuthList = allAuthSet.stream().map(auth -> {
            MenuNodeDTO menuNodeDTO = convertToMenuNodeDTO(auth);

            // 前端的树形组件, 父节点如果有子节点,那么选中状态由子节点决定, 最底层的子节点一般都是按钮
            if (AuthType.BUTTON.getCode().equals(auth.getAuthType())) {
                menuNodeDTO.setAssigned(authMapOfCurrentRole.containsKey(auth.getId()));
            }

            return menuNodeDTO;
        }).collect(Collectors.toList());

        // 构建当前角色的菜单树
        List<MenuNodeDTO> menuNodeTreeOfCurrentRole = TreeUtil.build(authorizedAuthList);

        // 设置 {@link AuthType.MENU} 类型的节点的选中状态
        setAssignedForMenu(menuNodeTreeOfCurrentRole, authMapOfCurrentRole);

        return menuNodeTreeOfCurrentRole;
    }

    /**
     * 设置 {@link AuthType#MENU} 类型的节点的选中状态
     *
     * @param menuTree
     * @param authMapOfCurrentRole
     */
    private void setAssignedForMenu(List<MenuNodeDTO> menuTree, Map<Long, Auth> authMapOfCurrentRole) {

        for (MenuNodeDTO menuNodeDTO : menuTree) {
            // 如果节点是菜单
            if (AuthType.MENU.getCode().equals(menuNodeDTO.getMenuType())) {
                // 判断当前角色是否有这个菜单权限
                boolean containsKey = authMapOfCurrentRole.containsKey(menuNodeDTO.getId());
                // 如果有
                if (containsKey) {
                    // 获取菜单的子节点
                    List<MenuNodeDTO> children = menuNodeDTO.getChildren();
                    // 如果子节点为空
                    if (CollectionUtils.isEmpty(children)) {
                        // 那么设置这个菜单节点为选中状态
                        menuNodeDTO.setAssigned(true);

                        // 如果有子节点
                    } else {
                        // 递归
                        setAssignedForMenu(children, authMapOfCurrentRole);
                    }

                    // 如果没有
                } else {
                    // 这个菜单节点不选中
                    menuNodeDTO.setAssigned(false);
                }
            }
        }
    }

    @Override
    public List<MenuNodeDTO> getMenuTreeOfUser(Long appId) {
        UserDTO currentLoginUser = EtContext.get(ContextConstant.USER);
        Long userId = currentLoginUser.getId();
        Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);

        // 查询用户在当前域或产品下拥有的角色集合
        List<UserRole> userRoleList = getRelationBetweenUserAndRoleInTargetRanges(userId, domainId, appId);
        log.info("user(name={}, email={}) has roles:{}",
                currentLoginUser.getUserName(), currentLoginUser.getEmail(), userRoleList);

        // 用户拥有的所有角色 ID
        List<Long> roleIdListOfCurrentUser = userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        // 用户拥有的所有权限(保护重复的权限)
        List<Auth> authorizedAuthList = authMapper.selectByRoleIdList(roleIdListOfCurrentUser);
        if (CollectionUtils.isEmpty(authorizedAuthList)) {
            return Collections.emptyList();
        }
        // 去重后,用户拥有的所有权限
        Set<Auth> authorizedAuthSet = Sets.newLinkedHashSet(authorizedAuthList);
        List<MenuNodeDTO> menuNodeDTOList = authorizedAuthSet.stream()
                .map(this::convertToMenuNodeDTO).collect(Collectors.toList());
        // 用户在当前域或产品下的权限树
        List<MenuNodeDTO> menuTreeOfUser = TreeUtil.build(menuNodeDTOList);
        return menuTreeOfUser;
    }

    @Override
    public boolean applyRolePermission(Long appId, Long roleId, String desc) {
        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        if(currentUserDTO == null){
            throw new CommonException("用户信息不能为空");
        }
        //查询申请角色信息
        Role role = roleMapper.selectByPrimaryKey(roleId);
        if(role == null){
            throw new CommonException("申请的角色不存在");
        }
        EisPermissionApplyRecord eisPermissionApplyRecord = new EisPermissionApplyRecord();
        eisPermissionApplyRecord.setAppId(appId);
        eisPermissionApplyRecord.setApplyUserName(currentUserDTO.getUserName());
        eisPermissionApplyRecord.setApplyUser(currentUserDTO.getEmail());
        eisPermissionApplyRecord.setAuditUser("");
        eisPermissionApplyRecord.setDescription(desc);
        eisPermissionApplyRecord.setRoleId(roleId);
        eisPermissionApplyRecord.setRoleName(role.getRoleName());
        eisPermissionApplyRecord.setStatus(PermissionAuditEnum.INIT.getChangeType());
        eisPermissionApplyRecord.setCreateTime(new Timestamp(System.currentTimeMillis()));
        eisPermissionApplyRecord.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        int ret = eisPermissionApplyRecordMapper.insert(eisPermissionApplyRecord);
        return ret > 0;
    }

    @Override
    public boolean auditRolePermission(Long applyId, Integer type) {
        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        if(currentUserDTO == null){
            throw new CommonException("用户信息不能为空");
        }
        EisPermissionApplyRecord record = eisPermissionApplyRecordMapper.getById(applyId);
        if(record == null){
            throw new CommonException("未发现申请记录");
        }
        if(type.equals(PermissionAuditEnum.PASS.getChangeType())){
            //审核通过，开通权限
            UserRoleRelationCreateParam createParam = new UserRoleRelationCreateParam();
            createParam.setRoleIds(Collections.singletonList(record.getRoleId()));
            UserSimpleDTO userSimpleDTO = new UserSimpleDTO();
            userSimpleDTO.setEmail(record.getApplyUser());
            createParam.setUsers(Collections.singletonList(userSimpleDTO));
            createParam.setRange(RoleTypeEnum.APP.getCode());
            addUser(createParam);
        }
        eisPermissionApplyRecordMapper.updateRecordStatus(applyId, type, currentUserDTO.getEmail());
        return true;
    }

    @Override
    public List<RoleApplyDTO> getApplyList(Long appId, Integer status) {
        List<EisPermissionApplyRecord> records = eisPermissionApplyRecordMapper.listApplyRecords(appId, status);
        List<RoleApplyDTO> roleApplyDTOS = new ArrayList<>();
        records.forEach(record -> {
            RoleApplyDTO roleApplyDTO = BeanConvertUtils.convert(record, RoleApplyDTO.class);
            roleApplyDTOS.add(roleApplyDTO);
        });
        return roleApplyDTOS;
    }

    @Override
    public List<UserRole> getRelationBetweenUserAndRoleInTargetRanges(Long userId, Long domainId, Long appId) {
        // 范围集合
        List<UserRole> ranges = new ArrayList<>(3);

        // 平台
        UserRole rangeInPlatform = new UserRole();
        rangeInPlatform.setUserId(userId);
        rangeInPlatform.setRoleType(RoleTypeEnum.PLATFORM.getCode());
        rangeInPlatform.setTypeId(GlobalConst.DEFAULT_PLATFORM_ID);
        ranges.add(rangeInPlatform);

        // 域
        if (domainId != null) {
            UserRole rangeInDomain = new UserRole();
            rangeInDomain.setUserId(userId);
            rangeInDomain.setRoleType(RoleTypeEnum.DOMAIN.getCode());
            rangeInDomain.setTypeId(domainId);
            ranges.add(rangeInDomain);
        }

        // 产品
        if (appId != null) {
            UserRole rangeInApp = new UserRole();
            rangeInApp.setUserId(userId);
            rangeInApp.setRoleType(RoleTypeEnum.APP.getCode());
            rangeInApp.setTypeId(appId);
            ranges.add(rangeInApp);
        }

        // 查询当前用户在这三个范围下拥有的所有角色
        List<UserRole> allUserRoleList = userRoleMapper.selectRelationBetweenUserAndRoleInTargetRange(ranges);
        if (CollectionUtils.isEmpty(allUserRoleList)) {
            throw new UserManagementException("该用户未分配角色");
        }

        return allUserRoleList;
    }

    private MenuNodeDTO convertToMenuNodeDTO(Auth auth) {
        MenuNodeDTO menuNodeDTO = new MenuNodeDTO();
        menuNodeDTO.setId(auth.getId());
        menuNodeDTO.setMenuType(auth.getAuthType());
        menuNodeDTO.setMenuName(auth.getAuthName());
        menuNodeDTO.setCode(auth.getAuthCode());
        menuNodeDTO.setParentCode(auth.getAuthParentCode());
        return menuNodeDTO;
    }

    @Override
    public boolean authorize(Set<Auth> requiredAuthList, Long userId, Long domainId, Long appId) {
        List<UserRole> userRoleList = getRelationBetweenUserAndRoleInTargetRanges(userId, domainId, appId);

        List<Long> roleIdList = userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        // 当前用户的所有权限(包含重复数据)
        List<Auth> authListOfCurrentUser = authMapper.selectByRoleIdList(roleIdList);
        // 如果当前用户的所有权限为空
        if (CollectionUtils.isEmpty(authListOfCurrentUser)) {
            // 直接拒绝
            return false;
        }
        // 用 Set 去重
        Set<Auth> authSetOfCurrentUser = Sets.newHashSet(authListOfCurrentUser);
        // 求交集
        authSetOfCurrentUser.retainAll(requiredAuthList);
        // 此时 authSetOfCurrentUser 就是交集, 不为空则放行; 否则拒绝
        return CollectionUtils.isNotEmpty(authSetOfCurrentUser);
    }

    @Override
    public List<UserRole> getRelationBetweenUserAndRole(Long userId,
                                                        RoleTypeEnum roleTypeEnum,
                                                        List<Long> typeIdList,
                                                        RoleLevelEnum roleLevelEnum) {
        Integer roleType = null;
        if (roleTypeEnum != null) {
            roleType = roleTypeEnum.getCode();
        }

        Integer roleLevel = null;
        if (roleLevelEnum != null) {
            roleLevel = roleLevelEnum.getLevel();
        }

        List<Long> userIdList = null;
        if (userId != null) {
            userIdList = Collections.singletonList(userId);
        }

        return userRoleMapper.selectRelationBetweenUserAndRole(null, roleType, typeIdList, roleLevel, userIdList);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public void setNewRolesForUser(Long userId,
                                   List<Long> newRoleIdList,
                                   RoleTypeEnum roleTypeEnum,
                                   Long typeId,
                                   boolean addOnly) {

        Integer roleType = roleTypeEnum.getCode();

        // 用户和角色新的关联关系
        List<UserRole> newRelationList = newRoleIdList.stream()
                .map(newRoleId -> UserRole.of(userId, newRoleId, roleType, typeId)).collect(Collectors.toList());

        if (addOnly) {
            userRoleMapper.insertBatch(newRelationList);

        } else {

            // 查询域或产品下,用户和角色已存在的关联关系
            List<UserRole> existedRelationList = userRoleMapper.selectRelationBetweenUserAndRole(null,
                    roleType, Lists.newArrayList(typeId), null, Lists.newArrayList(userId));

            deleteExistedRelationAndSaveNewRelationBetweenUserAndRole(existedRelationList, newRelationList);
        }
    }

    @Override
    public void setNewUserListForRole(Long roleId,
                                      List<Long> userIdList,
                                      RoleTypeEnum roleTypeEnum,
                                      Long typeId,
                                      boolean addOnly) {

        Integer roleType = roleTypeEnum.getCode();
        // 角色等级 和 用户 新的关联关系
        List<UserRole> newRelationList = userIdList.stream()
                .map(userId -> UserRole.of(userId, roleId, roleType, typeId)).collect(Collectors.toList());

        if (addOnly) {
            userRoleMapper.insertBatch(newRelationList);
        } else {

            // 该角色 和 用户 旧的关联关系
            List<UserRole> existedRelationList = userRoleMapper.selectRelationBetweenUserAndRole(roleId,
                    roleType, Lists.newArrayList(typeId), null, null);

            deleteExistedRelationAndSaveNewRelationBetweenUserAndRole(existedRelationList, newRelationList);
        }
    }

    @Override
    public List<Role> searchRole(EnumSet<RoleLevelEnum> roleLevelEnums,
                                 RoleTypeEnum roleTypeEnum,
                                 List<Long> typeIdList,
                                 String roleName,
                                 boolean containedBuilt) {

        List<Integer> roleLevelList = null;
        if (CollectionUtils.isNotEmpty(roleLevelEnums)) {
            roleLevelList = roleLevelEnums.stream().map(RoleLevelEnum::getLevel).collect(Collectors.toList());
        }

        Integer roleType = null;
        if (roleTypeEnum != null) {

            roleType = roleTypeEnum.getCode();

            if (RoleTypeEnum.PLATFORM.equals(roleTypeEnum)) {
                typeIdList = Collections.singletonList(GlobalConst.DEFAULT_PLATFORM_ID);
            }
        }

        return roleMapper.selectByExample(roleLevelList, roleType, typeIdList, roleName, containedBuilt);
    }


    @Override
    public <D extends BaseUserListHolderDTO> List<D> setUserDTOListOfRole(List<D> list,
                                                                          RoleTypeEnum roleTypeEnum,
                                                                          RoleLevelEnum roleLevelEnum) {
        if (CollectionUtils.isNotEmpty(list)) {
            // ID 集合
            List<Long> idList = list.stream().map(BaseUserListHolderDTO::getId).collect(Collectors.toList());
            List<UserRole> userRoleList = getRelationBetweenUserAndRole(null, roleTypeEnum, idList, roleLevelEnum);
            // 根据 typeId 分组
            Map<Long, List<UserRole>> map = userRoleList.stream().collect(Collectors.groupingBy(UserRole::getTypeId));

            for (int i = 0; i < list.size(); i++) {
                BaseUserListHolderDTO baseUserListHolderDTO = list.get(i);

                List<UserRole> userRoles = map.get(baseUserListHolderDTO.getId());
                if (CollectionUtils.isNotEmpty(userRoles)) {
                    List<UserSimpleDTO> userSimpleDTOList = userRoles.stream()
                            .map(userRole -> new UserSimpleDTO(userRole.getUserId(), userRole.getEmail(), userRole.getUserName()))
                            .collect(Collectors.toList());

                    switch (roleLevelEnum) {
                        case PRODUCT_ADMIN:
                        case DOMAIN_ADMIN: {
                            baseUserListHolderDTO.setAdmins(userSimpleDTOList);
                            break;
                        }
                        case DOMAIN_PRINCIPAL: {
                            // 业务上规定 域负责人 只能有一个
                            UserSimpleDTO owner = userSimpleDTOList.get(0);
                            baseUserListHolderDTO.setOwner(owner);
                            break;
                        }
                        default: {
                            // do nothing
                        }
                    }

                }
            }
        }
        return list;
    }

    /**
     * 根据范围决定 typeId 的值
     *
     * @param roleTypeEnum 范围
     * @return typeId
     */
    private Long decideTypeIdByRoleType(RoleTypeEnum roleTypeEnum) {
        Long typeId = null;
        switch (roleTypeEnum) {
            case APP: {
                typeId = EtContext.get(ContextConstant.APP_ID);
                break;
            }
            case DOMAIN: {
                typeId = EtContext.get(ContextConstant.DOMAIN_ID);
                break;
            }
            case PLATFORM: {
                typeId = GlobalConst.DEFAULT_PLATFORM_ID;
                break;
            }
            default: {
                // do nothing
            }
        }
        return typeId;
    }

    @Override
    public boolean checkIsHaveMember(RoleTypeEnum range, Long typeId) {
        int memberCount = userRoleMapper.selectCount(range.getCode(), typeId);
        log.info("{}:{} has {} members", range.name(), typeId, memberCount);
        return memberCount != 0;
    }
}
