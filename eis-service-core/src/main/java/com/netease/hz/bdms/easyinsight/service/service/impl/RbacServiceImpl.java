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
import com.netease.hz.bdms.easyinsight.common.dto.rbac.RoleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.AuthType;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleLevelEnum;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleTypeEnum;
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
import com.netease.hz.bdms.easyinsight.common.util.TreeUtil;
import com.netease.hz.bdms.easyinsight.common.vo.auth.RoleVO;
import com.netease.hz.bdms.easyinsight.common.vo.auth.UserVO;
import com.netease.hz.bdms.easyinsight.dao.AppMapper;
import com.netease.hz.bdms.easyinsight.dao.model.App;
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
 * @date 2021-08-02 ?????? 04:18
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

        // ??????????????????
        PageHelper.startPage(currentPage, pageSize);
        // ??????????????????
        List<User> userList = userMapper.selectByRange(roleType, typeId, roleId, orderBy, orderRule, search);
        if (CollectionUtils.isNotEmpty(userList)) {
            PageSerializable<User> userPageSerializable = PageSerializable.of(userList);
            resultDTO.setTotalNum(Long.valueOf(userPageSerializable.getTotal()).intValue());

            // ?????????????????????????????????
            List<UserVO> userVOList = Lists.newArrayListWithCapacity(pageSize);

            // ?????????????????? ??? ??? ?????? ??????????????????
            if (roleId == null) {
                // ?????? ID ??????
                List<Long> userIdList = userList.stream().map(User::getId).collect(Collectors.toList());

                // ??????????????????????????????
                List<UserRole> relationBetweenUserAndRole = userRoleMapper.selectRelationBetweenUserAndRole(
                        null, roleType, Lists.newArrayList(typeId), null, userIdList);
                // ???????????????????????? ?????? map
                Map<Long, List<UserRole>> userRoleMap = relationBetweenUserAndRole.stream()
                        .collect(Collectors.groupingBy(UserRole::getUserId));

                Map<Long, App> appMap = null;
                // ??????????????? app ??? map
                Map<Long, List<UserRole>> userAppMap = null;

                // ?????????????????? ???????????????
                if (RoleTypeEnum.DOMAIN.equals(roleTypeEnum)) {
                    // ???????????????????????????????????????
                    List<App> appListOfCurrentDomain = appMapper.selectByDomainId(typeId);
                    if (CollectionUtils.isNotEmpty(appListOfCurrentDomain)) {
                        appMap = appListOfCurrentDomain.stream()
                                .collect(Collectors.toMap(App::getId, Function.identity()));

                        List<Long> appIdList = Lists.newArrayList(appMap.keySet());
                        // ??????????????? app ????????????????????????
                        List<UserRole> userAppList = userRoleMapper.selectRelationBetweenUserAndRole(
                                null, RoleTypeEnum.APP.getCode(), appIdList, null, userIdList);
                        // ????????? app ???????????????????????? ?????? map
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
                    // ???????????? "????????????"
                    userVO.setCreateTime(user.getCreateTime());

                    List<UserRole> userRoles = userRoleMap.get(userId);
                    if (CollectionUtils.isNotEmpty(userRoles)) {
                        List<RoleVO> roleVOList = Lists.newArrayListWithCapacity(userRoles.size());
                        for (UserRole userRole : userRoles) {
                            RoleVO roleVO = new RoleVO();
                            roleVO.setId(userRole.getRoleId());
                            roleVO.setRoleName(userRole.getRoleName());

                            roleVOList.add(roleVO);

                            // userRole.getUpdateTime() ??????????????????????????????????????????????????????
                            userVO.setUpdateTime(userRole.getUpdateTime());
                        }
                        // ???????????????????????????
                        userVO.setRoles(roleVOList);
                    }

                    if (userAppMapNotEmpty) {
                        List<UserRole> list = userAppMap.get(userId);
                        if (CollectionUtils.isNotEmpty(list)) {
                            // ???????????????????????????????????????
                            Set<String> appNameSet = Sets.newHashSet();

                            for (UserRole userRole : list) {
                                App app = appMap.get(userRole.getTypeId());
                                appNameSet.add(app.getName());
                            }
                            // ????????????????????? app ??????
                            userVO.setApps(appNameSet);
                        }
                    }

                    userVOList.add(userVO);
                }

                // ?????????????????? ?????? ??????????????????
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

        // ?????????????????????????????????
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

        // ?????? ???/??????/?????? ???????????????????????????
        List<User> existedUserList = userMapper.selectByRange(roleTypeEnum.getCode(), typeId, createParam.getCurrentRole(),
                "u.id", "ASC", null);
        // ?????? map, key = email ,value = ??????
        Map<String, User> existedUserMap = existedUserList.stream()
                .collect(Collectors.toMap(User::getEmail, Function.identity()));

        // ??????????????????????????? ID ??????
        List<Long> userIdListToAddRole = new ArrayList<>();
        // ???????????????????????????
        List<User> usersToInsert = new ArrayList<>();

        List<UserSimpleDTO> submittedUserList = createParam.getUsers();
        for (UserSimpleDTO submittedUser : submittedUserList) {
            String key = submittedUser.getEmail();
            boolean containsKey = existedUserMap.containsKey(key);
            if (containsKey) {
                User user = existedUserMap.get(key);
                String errorMessage = MessageFormat.format("?????????[??????:{0}, ??????:{1}]?????????,??????????????????",
                        user.getUserName(), user.getEmail());
                throw new IllegalArgumentException(errorMessage);
            }

            User newUser = BeanConvertUtils.convert(submittedUser, User.class);
            // ??????????????????????????????????????????
            usersToInsert.add(newUser);
        }

        log.info("{} users will be added to current domain or app", usersToInsert.size());

        if (CollectionUtils.isNotEmpty(usersToInsert)) {
            List<String> newEmailList = usersToInsert.stream().map(User::getEmail).collect(Collectors.toList());
            // ???????????????????????????????????????????????????,???????????????????????????
            List<User> users = userMapper.selectByEmails(newEmailList);
            // users ?????? map
            Map<String, User> userMap = users.stream().collect(Collectors.toMap(User::getEmail, Function.identity()));

            Iterator<User> iterator = usersToInsert.iterator();
            while (iterator.hasNext()) {
                User userToAdd = iterator.next();
                String key = userToAdd.getEmail();
                boolean containsKey = userMap.containsKey(key);
                // ?????? eis_user ????????????????????????,??????????????????
                if (containsKey) {
                    // ?????? userId
                    userIdListToAddRole.add(userMap.get(key).getId());
                    // ???????????????????????????,??????
                    iterator.remove();

                    // ??????????????????????????? eis_user ???
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
                // ??????????????????
                userMapper.insertBatch(usersToInsert);

                // ????????????????????????ID??????
                List<Long> newUserIdList = usersToInsert.stream().map(User::getId).collect(Collectors.toList());
                userIdListToAddRole.addAll(newUserIdList);
            }
        }

        log.info("{} users will be assigned role", userIdListToAddRole.size());

        // ????????????????????????????????????
        if (CollectionUtils.isEmpty(userIdListToAddRole)) {
            // ????????????
            return;
        }

        List<Long> roleIds = createParam.getRoleIds();

        switch (roleTypeEnum) {
            case APP: {
                if (CollectionUtils.isEmpty(roleIds)) {
                    throw new IllegalArgumentException("???????????????");
                }

                // ?????? ??????????????? ????????????
                List<Role> roleList = this.searchRole(EnumSet.of(RoleLevelEnum.DOMAIN_NORMAL_USER),
                        RoleTypeEnum.DOMAIN, null, null, true);
                // ???????????? ??????????????? ID
                Long roleIdOfDomainNormalUser = roleList.get(0).getId();

                Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);
                // ????????????????????????????????????, ????????????????????? ??????????????? ?????????
                this.setNewUserListForRole(roleIdOfDomainNormalUser, userIdListToAddRole,
                        RoleTypeEnum.DOMAIN, domainId, true);

                break;
            }
            case DOMAIN: {
                // ?????? ??????????????? ????????????
                List<Role> roleList = this.searchRole(EnumSet.of(RoleLevelEnum.DOMAIN_NORMAL_USER),
                        RoleTypeEnum.DOMAIN, null, null, true);
                // ?????????????????? ??????????????? ?????????
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
            String tip = MessageFormat.format("??????[{0}]???????????????", newEmail);
            throw new IllegalArgumentException(tip);
        }

        if (user == null) {
            user = userMapper.selectByPrimaryKey(userId);
            if (user == null) {
                throw new IllegalArgumentException("???????????????ID:" + userId);
            }
        }

        user.setId(userId);

        String newUserName = updateParam.getUserName();
        if (StringUtils.isBlank(newUserName)) {
            newUserName = StringUtils.EMPTY;
        }
        // ?????????????????????,??????????????????
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
     * ???????????????????????????, ????????????????????????????????? {@link RoleLevelEnum#DOMAIN_PRINCIPAL}
     *
     * @param newRoleIdList
     * @param roleTypeEnum
     * @param typeId
     */
    private void notAllowedToUpdateDomainPrincipalAtMemberPageOfDomain(List<Long> newRoleIdList,
                                                                       RoleTypeEnum roleTypeEnum,
                                                                       Long typeId) {
        if (RoleTypeEnum.DOMAIN.equals(roleTypeEnum)) {
            // ?????? ???????????? ????????????
            List<Role> roleOfDomainPrincipal = this.searchRole(EnumSet.of(RoleLevelEnum.DOMAIN_PRINCIPAL),
                    roleTypeEnum, Collections.singletonList(typeId), null, true);
            // ???????????? ??????????????? ID
            Long roleIdOfDomainPrincipal = roleOfDomainPrincipal.get(0).getId();
            if (newRoleIdList.contains(roleIdOfDomainPrincipal)) {
                String errorMessage = MessageFormat.format("????????????????????????[{0}]", RoleLevelEnum.DOMAIN_PRINCIPAL.getRoleName());
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

        // ????????????????????????????????????
        userRoleMapper.deleteRelationBatchBetweenUserAndRole(roleType, Collections.singletonList(typeId), userIds, roleId);

        // ???????????? ?????????-????????????-???????????? ???????????????????????????
        if (RoleTypeEnum.DOMAIN.equals(roleTypeEnum)) {
            // ?????????????????????????????????????????????????????????, ?????????????????????????????????????????????
            List<App> allApps = appMapper.selectByDomainId(typeId);

            if (CollectionUtils.isNotEmpty(allApps)) {
                List<Long> appIdListOfCurrentDomain = allApps.stream().map(App::getId).collect(Collectors.toList());
                // ?????????????????????????????????????????????
                userRoleMapper.deleteRelationBatchBetweenUserAndRole(RoleTypeEnum.APP.getCode(),
                        appIdListOfCurrentDomain, userIds, roleId);
            }
        }

    }

    /**
     * ??????????????????????????????,????????????????????????
     *
     * @param existedRelationList ???????????????????????????????????????
     * @param newRelationList     ?????????????????????????????????
     */
    private void deleteExistedRelationAndSaveNewRelationBetweenUserAndRole(List<UserRole> existedRelationList,
                                                                           List<UserRole> newRelationList) {
        List<UserRole> tmp = new ArrayList<>(existedRelationList);
        // ?????????
        tmp.retainAll(newRelationList);

        if (CollectionUtils.isNotEmpty(tmp)) {
            // removeAll ???????????????????????? ?????? ?????????
            existedRelationList.removeAll(tmp);

            // removeAll ???????????????????????? ?????? ?????????
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
            throw new IllegalArgumentException("????????????????????????:" + roleName);
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
            throw new IllegalArgumentException("???????????????ID:" + appId);
        }
        Long currentDomainId = EtContext.get(ContextConstant.DOMAIN_ID);
        if (!app.getDomainId().equals(currentDomainId)) {
            throw new IllegalArgumentException("???????????????ID:" + appId);
        }
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public void assignFunctionToRole(RoleAuthParam roleAssignParam) {
        Long roleId = roleAssignParam.getRoleId();
        requireRoleNotBuiltin(roleId);

        // ????????????????????????????????????
        List<RoleAuth> existedAuthList = roleAuthMapper.selectByRoleId(roleId);

        // ???????????? ID ??????
        List<Long> newAuthIdList = roleAssignParam.getFunctions().stream()
                .map(MenuFunctionParam::getFunctionId).collect(Collectors.toList());

        List<RoleAuth> roleAuthListToInsert = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(newAuthIdList)) {
            List<Auth> authList = authMapper.selectByPrimaryKeyList(newAuthIdList);
            Set<Auth> authSetToInsert = Sets.newHashSet(authList);
            // ???????????????
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
        // ?????????
        tmp.retainAll(roleAuthListToInsert);

        if (CollectionUtils.isNotEmpty(tmp)) {
            // removeAll ???????????????????????? ?????? ?????????
            existedAuthList.removeAll(tmp);

            // removeAll ???????????????????????? ?????? ?????????
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
     * ???????????????????????????
     *
     * @param authSetToInsert ???????????????????????????
     * @param subAuthList     ???????????????
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
        // ????????????
        roleMapper.deleteByPrimaryKey(roleId);

        Integer roleType = roleToDelete.getRoleType();
        Long typeId = decideTypeIdByRoleType(RoleTypeEnum.match(roleType));

        // ???????????????????????????????????????
        userRoleMapper.deleteRelationBatchBetweenUserAndRole(roleType, Collections.singletonList(typeId), null, roleId);
        // ???????????????????????????????????????
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
                throw new IllegalArgumentException("????????????????????????:" + newRoleName);
            }
        }

        role.setRoleName(newRoleName);
        role.setDescription(updateParam.getDescription());
        roleMapper.updateByPrimaryKeySelective(role);
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param roleId ??????ID
     * @return
     */
    private Role requireRoleNotBuiltin(Long roleId) {
        Role role = roleMapper.selectByPrimaryKey(roleId);
        if (role == null) {
            throw new IllegalArgumentException("???????????????ID:" + roleId);
        }
        if (role.getBuiltin()) {
            throw new IllegalArgumentException("??????????????????????????????");
        }
        return role;
    }

    @Override
    public List<MenuNodeDTO> getMenuTree(Long roleId) {
        Role currentRole = roleMapper.selectByPrimaryKey(roleId);
        if (currentRole == null) {
            throw new IllegalArgumentException("???????????????ID:" + roleId);
        }

        RoleTypeEnum roleTypeEnum = RoleTypeEnum.match(currentRole.getRoleType());

        List<Long> typeIdList = null;
        Long typeId = currentRole.getTypeId();
        if (typeId != null) {
            typeIdList = new ArrayList<>(1);
            typeIdList.add(typeId);
        }

        // ??????????????????(??????????????????)
        List<Role> roleList = this.searchRole(null, roleTypeEnum, typeIdList, null, true);
        if (CollectionUtils.isEmpty(roleList)) {
            return Collections.emptyList();
        }

        List<Long> roleIdList = roleList.stream().map(Role::getId).collect(Collectors.toList());
        // ???????????????(??????????????????)
        List<Auth> authList = authMapper.selectByRoleIdList(roleIdList);
        // ??? Set ????????????????????????
        Set<Auth> allAuthSet = Sets.newLinkedHashSet(authList);

        // ???????????????????????????
        List<Auth> authListOfCurrentRole = authMapper.selectByRoleIdList(Lists.newArrayList(roleId));

        Map<Long, Auth> authMapOfCurrentRole = authListOfCurrentRole.stream()
                // ?????? map, key = authId
                .collect(Collectors.toMap(Auth::getId, Function.identity()));

        List<MenuNodeDTO> authorizedAuthList = allAuthSet.stream().map(auth -> {
            MenuNodeDTO menuNodeDTO = convertToMenuNodeDTO(auth);

            // ?????????????????????, ???????????????????????????,????????????????????????????????????, ???????????????????????????????????????
            if (AuthType.BUTTON.getCode().equals(auth.getAuthType())) {
                menuNodeDTO.setAssigned(authMapOfCurrentRole.containsKey(auth.getId()));
            }

            return menuNodeDTO;
        }).collect(Collectors.toList());

        // ??????????????????????????????
        List<MenuNodeDTO> menuNodeTreeOfCurrentRole = TreeUtil.build(authorizedAuthList);

        // ?????? {@link AuthType.MENU} ??????????????????????????????
        setAssignedForMenu(menuNodeTreeOfCurrentRole, authMapOfCurrentRole);

        return menuNodeTreeOfCurrentRole;
    }

    /**
     * ?????? {@link AuthType#MENU} ??????????????????????????????
     *
     * @param menuTree
     * @param authMapOfCurrentRole
     */
    private void setAssignedForMenu(List<MenuNodeDTO> menuTree, Map<Long, Auth> authMapOfCurrentRole) {

        for (MenuNodeDTO menuNodeDTO : menuTree) {
            // ?????????????????????
            if (AuthType.MENU.getCode().equals(menuNodeDTO.getMenuType())) {
                // ?????????????????????????????????????????????
                boolean containsKey = authMapOfCurrentRole.containsKey(menuNodeDTO.getId());
                // ?????????
                if (containsKey) {
                    // ????????????????????????
                    List<MenuNodeDTO> children = menuNodeDTO.getChildren();
                    // ?????????????????????
                    if (CollectionUtils.isEmpty(children)) {
                        // ?????????????????????????????????????????????
                        menuNodeDTO.setAssigned(true);

                        // ??????????????????
                    } else {
                        // ??????
                        setAssignedForMenu(children, authMapOfCurrentRole);
                    }

                    // ????????????
                } else {
                    // ???????????????????????????
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

        // ?????????????????????????????????????????????????????????
        List<UserRole> userRoleList = getRelationBetweenUserAndRoleInTargetRanges(userId, domainId, appId);
        log.info("user(name={}, email={}) has roles:{}",
                currentLoginUser.getUserName(), currentLoginUser.getEmail(), userRoleList);

        // ??????????????????????????? ID
        List<Long> roleIdListOfCurrentUser = userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        // ???????????????????????????(?????????????????????)
        List<Auth> authorizedAuthList = authMapper.selectByRoleIdList(roleIdListOfCurrentUser);
        if (CollectionUtils.isEmpty(authorizedAuthList)) {
            return Collections.emptyList();
        }
        // ?????????,???????????????????????????
        Set<Auth> authorizedAuthSet = Sets.newLinkedHashSet(authorizedAuthList);
        List<MenuNodeDTO> menuNodeDTOList = authorizedAuthSet.stream()
                .map(this::convertToMenuNodeDTO).collect(Collectors.toList());
        // ??????????????????????????????????????????
        List<MenuNodeDTO> menuTreeOfUser = TreeUtil.build(menuNodeDTOList);
        return menuTreeOfUser;
    }

    @Override
    public List<UserRole> getRelationBetweenUserAndRoleInTargetRanges(Long userId, Long domainId, Long appId) {
        // ????????????
        List<UserRole> ranges = new ArrayList<>(3);

        // ??????
        UserRole rangeInPlatform = new UserRole();
        rangeInPlatform.setUserId(userId);
        rangeInPlatform.setRoleType(RoleTypeEnum.PLATFORM.getCode());
        rangeInPlatform.setTypeId(GlobalConst.DEFAULT_PLATFORM_ID);
        ranges.add(rangeInPlatform);

        // ???
        if (domainId != null) {
            UserRole rangeInDomain = new UserRole();
            rangeInDomain.setUserId(userId);
            rangeInDomain.setRoleType(RoleTypeEnum.DOMAIN.getCode());
            rangeInDomain.setTypeId(domainId);
            ranges.add(rangeInDomain);
        }

        // ??????
        if (appId != null) {
            UserRole rangeInApp = new UserRole();
            rangeInApp.setUserId(userId);
            rangeInApp.setRoleType(RoleTypeEnum.APP.getCode());
            rangeInApp.setTypeId(appId);
            ranges.add(rangeInApp);
        }

        // ????????????????????????????????????????????????????????????
        List<UserRole> allUserRoleList = userRoleMapper.selectRelationBetweenUserAndRoleInTargetRange(ranges);
        if (CollectionUtils.isEmpty(allUserRoleList)) {
            throw new UserManagementException("????????????????????????");
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
        // ???????????????????????????(??????????????????)
        List<Auth> authListOfCurrentUser = authMapper.selectByRoleIdList(roleIdList);
        // ???????????????????????????????????????
        if (CollectionUtils.isEmpty(authListOfCurrentUser)) {
            // ????????????
            return false;
        }
        // ??? Set ??????
        Set<Auth> authSetOfCurrentUser = Sets.newHashSet(authListOfCurrentUser);
        // ?????????
        authSetOfCurrentUser.retainAll(requiredAuthList);
        // ?????? authSetOfCurrentUser ????????????, ??????????????????; ????????????
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

        // ?????????????????????????????????
        List<UserRole> newRelationList = newRoleIdList.stream()
                .map(newRoleId -> UserRole.of(userId, newRoleId, roleType, typeId)).collect(Collectors.toList());

        if (addOnly) {
            userRoleMapper.insertBatch(newRelationList);

        } else {

            // ?????????????????????,???????????????????????????????????????
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
        // ???????????? ??? ?????? ??????????????????
        List<UserRole> newRelationList = userIdList.stream()
                .map(userId -> UserRole.of(userId, roleId, roleType, typeId)).collect(Collectors.toList());

        if (addOnly) {
            userRoleMapper.insertBatch(newRelationList);
        } else {

            // ????????? ??? ?????? ??????????????????
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
            // ID ??????
            List<Long> idList = list.stream().map(BaseUserListHolderDTO::getId).collect(Collectors.toList());
            List<UserRole> userRoleList = getRelationBetweenUserAndRole(null, roleTypeEnum, idList, roleLevelEnum);
            // ?????? typeId ??????
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
                            // ??????????????? ???????????? ???????????????
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
     * ?????????????????? typeId ??????
     *
     * @param roleTypeEnum ??????
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
