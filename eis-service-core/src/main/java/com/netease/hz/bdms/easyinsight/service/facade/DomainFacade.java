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
        // ????????????
        Preconditions.checkArgument(null != param, "?????????????????????");
        Preconditions.checkArgument(StringUtils.isNotBlank(param.getCode()), "???ID????????????");

        // ??????????????????????????????
        DomainSimpleDTO existsDomain = domainService.getDomainByCode(param.getCode());
        Preconditions.checkArgument(null == existsDomain, "??????????????????????????????");

        // ????????????
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
            throw new DomainException("??????ID????????????????????????");
        }

        List<Role> roleList = rbacService.searchRole(
                // ?????? ???????????? ??? ???????????? ???????????????
                EnumSet.of(RoleLevelEnum.DOMAIN_PRINCIPAL, RoleLevelEnum.DOMAIN_ADMIN), RoleTypeEnum.DOMAIN,
                null, null, true);
        Map<Integer, Role> roleLevelMap = roleList.stream()
                // ?????? ???????????? ?????? map
                .collect(Collectors.toMap(Role::getRoleLevel, Function.identity()));
        // ?????????????????????????????????
        initRelationBetweenUserAndRole(param, roleLevelMap, domainId);

        return domainId;
    }

    /**
     * ?????? ??? ????????????????????????????????????
     *
     * @param param
     * @param levelMap
     * @param domainId
     */
    private void initRelationBetweenUserAndRole(DomainCreateParam param, Map<Integer, Role> levelMap, Long domainId) {
        RoleTypeEnum range = RoleTypeEnum.DOMAIN;

        Long userId = param.getOwner().getId();
        Long roleId = levelMap.get(RoleLevelEnum.DOMAIN_PRINCIPAL.getLevel()).getId();
        // ?????? ???????????? ??????????????????????????????
        rbacService.setNewRolesForUser(userId, Lists.newArrayList(roleId), range, domainId, true);

        List<UserSimpleDTO> admins = param.getAdmins();
        if (CollectionUtils.isNotEmpty(admins)) {
            List<String> adminEmailList = admins.stream().map(UserSimpleDTO::getEmail).collect(Collectors.toList());
            // ???????????????????????????????????????
            List<User> adminUserList = userService.getByEmails(adminEmailList);
            if (CollectionUtils.isNotEmpty(adminUserList)) {
                List<Long> adminUserIdList = adminUserList.stream().map(User::getId).collect(Collectors.toList());
                roleId = levelMap.get(RoleLevelEnum.DOMAIN_ADMIN.getLevel()).getId();
                // ?????? ???????????? ??????????????????????????????
                rbacService.setNewUserListForRole(roleId, adminUserIdList, range, domainId, true);
            }
        }
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public Integer updateDomain(DomainUpdateParam param) {
        // ????????????
        Preconditions.checkArgument(null != param, "?????????????????????");
        Preconditions.checkArgument(StringUtils.isNotBlank(param.getCode()) && null != param.getId(), "???ID????????????");

        // ??????????????????????????????
        DomainSimpleDTO existsDomain = domainService.getDomainById(param.getId());
        Preconditions.checkArgument(null != existsDomain, "??????????????????????????????");

        // ????????????
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
                // ???????????????????????? ????????????
                List<UserRole> existedDomainPrincipalList = rbacService.getRelationBetweenUserAndRole(null, range, Collections.singletonList(domainId),
                        RoleLevelEnum.DOMAIN_PRINCIPAL);
                if (CollectionUtils.isNotEmpty(existedDomainPrincipalList)) {
                    // ??????????????????????????? ????????????
                    UserRole userRole = existedDomainPrincipalList.get(0);
                    // ???????????? ???????????? ?????????ID
                    Long userIdOfExistedDomainPrincipal = userRole.getUserId();
                    // ??????????????? ??????????????? ID
                    Long roleIdOfDomainNormalUser = levelMap.get(RoleLevelEnum.DOMAIN_NORMAL_USER.getLevel()).getId();
                    // ???????????? ???????????? ?????????????????? ???????????????
                    rbacService.setNewRolesForUser(userIdOfExistedDomainPrincipal,
                            Lists.newArrayList(roleIdOfDomainNormalUser), range, domainId, false);
                }

                // ??? owner ?????????
                String emailOfNewOwner = param.getOwner().getEmail();

                // ??? owner ?????????ID
                Long newOwnerId;
                User newOwner = userService.getByEmail(emailOfNewOwner);
                if (newOwner == null) {
                    newOwnerId = userService.create(emailOfNewOwner, StringUtils.EMPTY);
                } else {
                    newOwnerId = newOwner.getId();
                }

                // ???????????? ??????????????? ID
                Long roleIdOfDomainPrincipal = levelMap.get(RoleLevelEnum.DOMAIN_PRINCIPAL.getLevel()).getId();
                // ??? owner ??????????????? ????????????
                rbacService.setNewRolesForUser(newOwnerId, Lists.newArrayList(roleIdOfDomainPrincipal), range, domainId, false);
            }

            // ?????????????????????????????????
            List<UserSimpleDTO> admins = param.getAdmins();
            if (CollectionUtils.isNotEmpty(admins)) {
                List<String> adminEmailList = admins.stream().map(UserSimpleDTO::getEmail).collect(Collectors.toList());
                List<User> adminUserList = userService.getByEmails(adminEmailList);
                if (CollectionUtils.isNotEmpty(adminUserList)) {
                    List<Long> adminUserIdList = adminUserList.stream().map(User::getId).collect(Collectors.toList());
                    // ???????????? ??????????????? ID
                    Long roleIdOfDomainAdmin = levelMap.get(RoleLevelEnum.DOMAIN_ADMIN.getLevel()).getId();
                    rbacService.setNewUserListForRole(roleIdOfDomainAdmin, adminUserIdList, range, domainId, false);
                }
            }

            return domainService.updateDomain(domainSimpleDTO);
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("???????????????????????????????????????");
        }
    }

    public Integer deleteDomain(Long domainId) {
        // ????????????
        Preconditions.checkArgument(null != domainId, "?????????ID????????????");

        Integer appSize = appService.getAppSizeByDomainId(domainId);
        Preconditions.checkArgument(appSize <= 0, "?????????????????????????????????????????????");

        return domainService.deleteDomain(domainId);
    }

    public PagingResultDTO<DomainSimpleDTO> listDomains(String search, PagingSortDTO pagingSortDTO) {
        // ????????????
        Preconditions.checkArgument(null != pagingSortDTO, "??????????????????");

        Integer currentPage = pagingSortDTO.getCurrentPage();
        Integer pageSize = pagingSortDTO.getPageSize();

        PagingResultDTO<DomainSimpleDTO> resultDTO = new PagingResultDTO<>();
        resultDTO.setPageNum(currentPage);
        resultDTO.setTotalNum(0);

        // ??????????????????
        List<DomainSimpleDTO> domains = domainService.searchDomain(search, pagingSortDTO.getOrderBy(),
                pagingSortDTO.getOrderRule(), currentPage, pageSize);

        // ?????????????????? ????????????
        rbacService.setUserDTOListOfRole(domains, RoleTypeEnum.DOMAIN, RoleLevelEnum.DOMAIN_PRINCIPAL);

        PageSerializable<DomainSimpleDTO> pageSerializable = PageSerializable.of(domains);

        resultDTO.setTotalNum(Long.valueOf(pageSerializable.getTotal()).intValue());
        resultDTO.setList(domains);
        return resultDTO;
    }

    public DomainDTO getDomain(Long domainId) {
        // ????????????
        Preconditions.checkArgument(null != domainId, "???ID????????????");
        // ????????????
        DomainSimpleDTO domainSimpleDTO = domainService.getDomainById(domainId);
        DomainDTO domainDTO = BeanConvertUtils.convert(domainSimpleDTO, DomainDTO.class);

        List<DomainDTO> domainDTOList = Collections.singletonList(domainDTO);
        // ?????? ????????????
        rbacService.setUserDTOListOfRole(domainDTOList, RoleTypeEnum.DOMAIN, RoleLevelEnum.DOMAIN_PRINCIPAL);
        // ?????? ???????????? ??????
        rbacService.setUserDTOListOfRole(domainDTOList, RoleTypeEnum.DOMAIN, RoleLevelEnum.DOMAIN_ADMIN);

        List<AppSimpleDTO> appSimpleDTOList = appService.getAppsByDomainId(domainId);
        domainDTO.setApps(appSimpleDTOList);
        return domainDTO;
    }

    public List<AppSimpleDTO> getAppOfCurrentUser() {
        Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);
        // ?????? ??? ????????????????????????
        List<AppSimpleDTO> allApps = appService.getAppsByDomainId(domainId);
        if (CollectionUtils.isEmpty(allApps)) {
            return Collections.emptyList();
        }

        UserDTO user = EtContext.get(ContextConstant.USER);
        Long appId = EtContext.get(ContextConstant.APP_ID);

        Long userId = user.getId();

        // ?????????????????????????????????????????????????????????
        List<UserRole> userRoleList = rbacService.getRelationBetweenUserAndRoleInTargetRanges(userId, domainId, appId);

        // ???????????????????????????????????????
        boolean containsPrivilegedRoles = checkIsContainsPrivilegedRoles(userRoleList);
        if (containsPrivilegedRoles) {
            return allApps;
        }

        List<Long> allAppIds = allApps.stream().map(AppSimpleDTO::getId).collect(Collectors.toList());

        // ?????????????????? ?????? ??? ???????????? ???????????????
        userRoleList = rbacService.getRelationBetweenUserAndRole(userId, RoleTypeEnum.APP, allAppIds, null);
        if (CollectionUtils.isEmpty(userRoleList)) {
            // ???????????????,???????????????????????????????????????????????????
            return Collections.emptyList();
        }

        Map<Long, AppSimpleDTO> appMap = allApps.stream()
                .collect(Collectors.toMap(AppSimpleDTO::getId, Function.identity()));

        // ??????????????????????????????
        return userRoleList.stream().map(userRole -> appMap.get(userRole.getTypeId())).collect(Collectors.toList());
    }

    /**
     * ???????????????????????????????????????????????????????????????
     *
     * @param userRoleList ?????????????????????
     * @return true-??????; false - ?????????
     */
    private boolean checkIsContainsPrivilegedRoles(List<UserRole> userRoleList) {
        Set<Long> roleIdsOfCurrentUser = userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
        log.info("roleIdsOfCurrentUser:{}", roleIdsOfCurrentUser);

        List<Role> privilegedRoleList = rbacService.searchRole(PermissionVisibleConstant.PRIVILEGED_ROLES_OF_VIEW_ALL_APPS,
                null, null, null, true);
        Set<Long> privilegedRoleIds = privilegedRoleList.stream().map(Role::getId).collect(Collectors.toSet());
        log.info("privilegedRoleIds:{}", privilegedRoleIds);

        // ?????????
        roleIdsOfCurrentUser.retainAll(privilegedRoleIds);

        boolean contained = CollectionUtils.isNotEmpty(roleIdsOfCurrentUser);
        log.info("current user could view all apps of current domain:{}", contained);

        return contained;
    }
}
