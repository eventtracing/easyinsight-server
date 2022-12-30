package com.netease.hz.bdms.easyinsight.service.facade;

import com.github.pagehelper.PageSerializable;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.constant.PermissionVisibleConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.app.AppSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingResultDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.domain.DomainDTO;
import com.netease.hz.bdms.easyinsight.common.dto.domain.DomainSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleLevelEnum;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleTypeEnum;
import com.netease.hz.bdms.easyinsight.common.exception.DomainException;
import com.netease.hz.bdms.easyinsight.common.param.domain.DomainCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.domain.DomainUpdateParam;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.model.rbac.Role;
import com.netease.hz.bdms.easyinsight.dao.model.rbac.User;
import com.netease.hz.bdms.easyinsight.dao.model.rbac.UserRole;
import com.netease.hz.bdms.easyinsight.service.service.AppService;
import com.netease.hz.bdms.easyinsight.service.service.DomainService;
import com.netease.hz.bdms.easyinsight.service.service.RbacService;
import com.netease.hz.bdms.easyinsight.service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Component
public class DomainFacade {

    @Autowired
    private DomainService domainService;

    @Autowired
    private AppService appService;

    @Autowired
    private RbacService rbacService;

    @Autowired
    private UserService userService;

    @Transactional(rollbackFor = RuntimeException.class)
    public Long createDomain(DomainCreateParam param) {
        // 验证参数
        Preconditions.checkArgument(null != param, "域对象不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(param.getCode()), "域ID不能为空");

        // 验证当前域是否已存在
        DomainSimpleDTO existsDomain = domainService.getDomainByCode(param.getCode());
        Preconditions.checkArgument(null == existsDomain, "该域已存在，创建失败");

        // 插入记录
        DomainSimpleDTO domainSimpleDTO = BeanConvertUtils.convert(param, DomainSimpleDTO.class);
        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
        domainSimpleDTO.setCreator(currentUser)
                .setUpdater(currentUser);

        UserSimpleDTO ownerDTO = domainSimpleDTO.getOwner();
        String ownerEmail = ownerDTO.getEmail();

        Long ownerId;

        User domainOwner = userService.getByEmail(ownerEmail);
        if (domainOwner == null) {
            ownerId = userService.create(ownerEmail, null);
        } else {
            ownerId = domainOwner.getId();
            ownerDTO.setUserName(domainOwner.getUserName());
        }
        param.getOwner().setId(ownerId);

        Long domainId;
        try {
            domainId = domainService.createDomain(domainSimpleDTO);
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("该域ID已存在，创建失败");
        }

        List<Role> roleList = rbacService.searchRole(
                // 查询 域负责人 和 域管理员 这两个角色
                EnumSet.of(RoleLevelEnum.DOMAIN_PRINCIPAL, RoleLevelEnum.DOMAIN_ADMIN), RoleTypeEnum.DOMAIN,
                null, null, true);
        Map<Integer, Role> roleLevelMap = roleList.stream()
                // 根据 角色等级 转成 map
                .collect(Collectors.toMap(Role::getRoleLevel, Function.identity()));
        // 初始化用户和角色的关系
        initRelationBetweenUserAndRole(param, roleLevelMap, domainId);

        return domainId;
    }

    /**
     * 创建 域 时初始化用户和角色的关系
     *
     * @param param
     * @param levelMap
     * @param domainId
     */
    private void initRelationBetweenUserAndRole(DomainCreateParam param, Map<Integer, Role> levelMap, Long domainId) {
        RoleTypeEnum range = RoleTypeEnum.DOMAIN;

        Long userId = param.getOwner().getId();
        Long roleId = levelMap.get(RoleLevelEnum.DOMAIN_PRINCIPAL.getLevel()).getId();
        // 创建 域负责人 这个角色和用户的关系
        rbacService.setNewRolesForUser(userId, Lists.newArrayList(roleId), range, domainId, true);

        List<UserSimpleDTO> admins = param.getAdmins();
        if (CollectionUtils.isNotEmpty(admins)) {
            List<String> adminEmailList = admins.stream().map(UserSimpleDTO::getEmail).collect(Collectors.toList());
            // 根据入参的邮箱集合查询用户
            List<User> adminUserList = userService.getByEmails(adminEmailList);
            if (CollectionUtils.isNotEmpty(adminUserList)) {
                List<Long> adminUserIdList = adminUserList.stream().map(User::getId).collect(Collectors.toList());
                roleId = levelMap.get(RoleLevelEnum.DOMAIN_ADMIN.getLevel()).getId();
                // 创建 域管理员 这个角色和用户的关系
                rbacService.setNewUserListForRole(roleId, adminUserIdList, range, domainId, true);
            }
        }
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public Integer updateDomain(DomainUpdateParam param) {
        // 验证参数
        Preconditions.checkArgument(null != param, "域对象不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(param.getCode()) && null != param.getId(), "域ID不能为空");

        // 验证当前域是否已存在
        DomainSimpleDTO existsDomain = domainService.getDomainById(param.getId());
        Preconditions.checkArgument(null != existsDomain, "该域不存在，无法修改");

        // 修改记录
        DomainSimpleDTO domainSimpleDTO = BeanConvertUtils.convert(param, DomainSimpleDTO.class);
        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
        domainSimpleDTO.setUpdater(currentUser);

        try {
            RoleTypeEnum range = RoleTypeEnum.DOMAIN;
            Long domainId = existsDomain.getId();

            List<Role> roleList = rbacService.searchRole(EnumSet.of(RoleLevelEnum.DOMAIN_PRINCIPAL,
                    RoleLevelEnum.DOMAIN_ADMIN, RoleLevelEnum.DOMAIN_NORMAL_USER), range, null, null, true);
            Map<Integer, Role> levelMap = roleList.stream()
                    .collect(Collectors.toMap(Role::getRoleLevel, Function.identity()));

            if (!existsDomain.getOwner().equals(param.getOwner())) {
                // 当前域下已存在的 域负责人
                List<UserRole> existedDomainPrincipalList = rbacService.getRelationBetweenUserAndRole(null, range, Collections.singletonList(domainId),
                        RoleLevelEnum.DOMAIN_PRINCIPAL);
                if (CollectionUtils.isNotEmpty(existedDomainPrincipalList)) {
                    // 业务规定只能有一个 域负责人
                    UserRole userRole = existedDomainPrincipalList.get(0);
                    // 已存在的 域负责人 的用户ID
                    Long userIdOfExistedDomainPrincipal = userRole.getUserId();
                    // 域普通用户 这个角色的 ID
                    Long roleIdOfDomainNormalUser = levelMap.get(RoleLevelEnum.DOMAIN_NORMAL_USER.getLevel()).getId();
                    // 已存在的 域负责人 的身份降级为 域普通用户
                    rbacService.setNewRolesForUser(userIdOfExistedDomainPrincipal,
                            Lists.newArrayList(roleIdOfDomainNormalUser), range, domainId, false);
                }

                // 新 owner 的邮箱
                String emailOfNewOwner = param.getOwner().getEmail();

                // 新 owner 的用户ID
                Long newOwnerId;
                User newOwner = userService.getByEmail(emailOfNewOwner);
                if (newOwner == null) {
                    newOwnerId = userService.create(emailOfNewOwner, StringUtils.EMPTY);
                } else {
                    newOwnerId = newOwner.getId();
                }

                // 域负责人 这个角色的 ID
                Long roleIdOfDomainPrincipal = levelMap.get(RoleLevelEnum.DOMAIN_PRINCIPAL.getLevel()).getId();
                // 新 owner 的身份改为 域负责人
                rbacService.setNewRolesForUser(newOwnerId, Lists.newArrayList(roleIdOfDomainPrincipal), range, domainId, false);
            }

            // 更新这个域的管理员列表
            List<UserSimpleDTO> admins = param.getAdmins();
            if (CollectionUtils.isNotEmpty(admins)) {
                List<String> adminEmailList = admins.stream().map(UserSimpleDTO::getEmail).collect(Collectors.toList());
                List<User> adminUserList = userService.getByEmails(adminEmailList);
                if (CollectionUtils.isNotEmpty(adminUserList)) {
                    List<Long> adminUserIdList = adminUserList.stream().map(User::getId).collect(Collectors.toList());
                    // 域管理员 这个角色的 ID
                    Long roleIdOfDomainAdmin = levelMap.get(RoleLevelEnum.DOMAIN_ADMIN.getLevel()).getId();
                    rbacService.setNewUserListForRole(roleIdOfDomainAdmin, adminUserIdList, range, domainId, false);
                }
            }

            return domainService.updateDomain(domainSimpleDTO);
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("修改后的域已存在，修改失败");
        }
    }

    public Integer deleteDomain(Long domainId) {
        // 验证参数
        Preconditions.checkArgument(null != domainId, "域主键ID不能为空");

        Integer appSize = appService.getAppSizeByDomainId(domainId);
        Preconditions.checkArgument(appSize <= 0, "该域下存在绑定的产品，不能删除");

        return domainService.deleteDomain(domainId);
    }

    public PagingResultDTO<DomainSimpleDTO> listDomains(String search, PagingSortDTO pagingSortDTO) {
        // 验证参数
        Preconditions.checkArgument(null != pagingSortDTO, "分页不能为空");

        Integer currentPage = pagingSortDTO.getCurrentPage();
        Integer pageSize = pagingSortDTO.getPageSize();

        PagingResultDTO<DomainSimpleDTO> resultDTO = new PagingResultDTO<>();
        resultDTO.setPageNum(currentPage);
        resultDTO.setTotalNum(0);

        // 获取分页明细
        List<DomainSimpleDTO> domains = domainService.searchDomain(search, pagingSortDTO.getOrderBy(),
                pagingSortDTO.getOrderRule(), currentPage, pageSize);

        // 为这些域设置 域负责人
        rbacService.setUserDTOListOfRole(domains, RoleTypeEnum.DOMAIN, RoleLevelEnum.DOMAIN_PRINCIPAL);

        PageSerializable<DomainSimpleDTO> pageSerializable = PageSerializable.of(domains);

        resultDTO.setTotalNum(Long.valueOf(pageSerializable.getTotal()).intValue());
        resultDTO.setList(domains);
        return resultDTO;
    }

    public DomainDTO getDomain(Long domainId) {
        // 验证参数
        Preconditions.checkArgument(null != domainId, "域ID不能为空");
        // 获取数据
        DomainSimpleDTO domainSimpleDTO = domainService.getDomainById(domainId);
        DomainDTO domainDTO = BeanConvertUtils.convert(domainSimpleDTO, DomainDTO.class);

        List<DomainDTO> domainDTOList = Collections.singletonList(domainDTO);
        // 设置 域负责人
        rbacService.setUserDTOListOfRole(domainDTOList, RoleTypeEnum.DOMAIN, RoleLevelEnum.DOMAIN_PRINCIPAL);
        // 设置 域管理员 集合
        rbacService.setUserDTOListOfRole(domainDTOList, RoleTypeEnum.DOMAIN, RoleLevelEnum.DOMAIN_ADMIN);

        List<AppSimpleDTO> appSimpleDTOList = appService.getAppsByDomainId(domainId);
        domainDTO.setApps(appSimpleDTOList);
        return domainDTO;
    }

    public List<AppSimpleDTO> getAppOfCurrentUser() {
        Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);
        // 当前 域 下所有的产品集合
        List<AppSimpleDTO> allApps = appService.getAppsByDomainId(domainId);
        if (CollectionUtils.isEmpty(allApps)) {
            return Collections.emptyList();
        }

        UserDTO user = EtContext.get(ContextConstant.USER);
        Long appId = EtContext.get(ContextConstant.APP_ID);

        Long userId = user.getId();

        // 查询用户在当前域或产品下拥有的角色集合
        List<UserRole> userRoleList = rbacService.getRelationBetweenUserAndRoleInTargetRanges(userId, domainId, appId);

        // 检查是否包含拥有特权的角色
        boolean containsPrivilegedRoles = checkIsContainsPrivilegedRoles(userRoleList);
        if (containsPrivilegedRoles) {
            return allApps;
        }

        List<Long> allAppIds = allApps.stream().map(AppSimpleDTO::getId).collect(Collectors.toList());

        // 查询当前域下 产品 和 登录用户 的关联关系
        userRoleList = rbacService.getRelationBetweenUserAndRole(userId, RoleTypeEnum.APP, allAppIds, null);
        if (CollectionUtils.isEmpty(userRoleList)) {
            // 返回空集合,这时登录用户看不到该域下的任何产品
            return Collections.emptyList();
        }

        Map<Long, AppSimpleDTO> appMap = allApps.stream()
                .collect(Collectors.toMap(AppSimpleDTO::getId, Function.identity()));

        // 用户能看到的产品集合
        return userRoleList.stream().map(userRole -> appMap.get(userRole.getTypeId())).collect(Collectors.toList());
    }

    /**
     * 检查用户的角色集合中是否包含拥有特权的角色
     *
     * @param userRoleList 用户的角色集合
     * @return true-包含; false - 不包含
     */
    private boolean checkIsContainsPrivilegedRoles(List<UserRole> userRoleList) {
        Set<Long> roleIdsOfCurrentUser = userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
        log.info("roleIdsOfCurrentUser:{}", roleIdsOfCurrentUser);

        List<Role> privilegedRoleList = rbacService.searchRole(PermissionVisibleConstant.PRIVILEGED_ROLES_OF_VIEW_ALL_APPS,
                null, null, null, true);
        Set<Long> privilegedRoleIds = privilegedRoleList.stream().map(Role::getId).collect(Collectors.toSet());
        log.info("privilegedRoleIds:{}", privilegedRoleIds);

        // 求交集
        roleIdsOfCurrentUser.retainAll(privilegedRoleIds);

        boolean contained = CollectionUtils.isNotEmpty(roleIdsOfCurrentUser);
        log.info("current user could view all apps of current domain:{}", contained);

        return contained;
    }
}
