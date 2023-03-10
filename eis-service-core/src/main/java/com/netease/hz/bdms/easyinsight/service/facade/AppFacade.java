package com.netease.hz.bdms.easyinsight.service.facade;

import com.google.common.base.Preconditions;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.app.AppDTO;
import com.netease.hz.bdms.easyinsight.common.dto.app.AppSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingResultDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.domain.DomainSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.EntityTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleLevelEnum;
import com.netease.hz.bdms.easyinsight.common.enums.rbac.RoleTypeEnum;
import com.netease.hz.bdms.easyinsight.common.exception.DomainException;
import com.netease.hz.bdms.easyinsight.common.exception.UserManagementException;
import com.netease.hz.bdms.easyinsight.common.param.app.AppCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.app.AppUpdateParam;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.model.rbac.Role;
import com.netease.hz.bdms.easyinsight.service.service.AppService;
import com.netease.hz.bdms.easyinsight.service.service.DomainService;
import com.netease.hz.bdms.easyinsight.service.service.ParamService;
import com.netease.hz.bdms.easyinsight.service.service.ParamValueService;
import com.netease.hz.bdms.easyinsight.service.service.RbacService;
import com.netease.hz.bdms.easyinsight.service.service.TerminalService;
import com.netease.hz.bdms.easyinsight.service.service.VersionService;
import com.netease.hz.bdms.easyinsight.service.helper.ParamBindHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AppFacade {

    @Autowired
    private AppService appService;

    @Autowired
    private DomainService domainService;

    @Autowired
    private TerminalService terminalService;

    @Autowired
    private ParamBindHelper paramBindHelper;

    @Autowired
    private ParamValueService paramValueService;

    @Autowired
    private VersionService versionService;

    @Autowired
    private ParamService paramService;

    @Autowired
    private RbacService rbacService;

    @Transactional(rollbackFor = RuntimeException.class)
    public Long createApp(AppCreateParam param) {
        // ????????????
        Preconditions.checkArgument(null != param, "????????????????????????");

        Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);
        Preconditions.checkArgument(null != domainId, "??????????????????");
        DomainSimpleDTO domainSimpleDTO = domainService.getDomainById(domainId);
        Preconditions.checkArgument(null != domainSimpleDTO, "???????????????");

        AppSimpleDTO appSimpleDTO = BeanConvertUtils.convert(param, AppSimpleDTO.class);
        appSimpleDTO.setDomainId(domainId);

        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
        appSimpleDTO.setCreator(currentUser)
                .setUpdater(currentUser);

        Long appId;
        try {
            // ????????????
            appId = appService.createApp(appSimpleDTO);
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("?????????????????????????????????");
        }

        RoleTypeEnum range = RoleTypeEnum.APP;

        List<Role> roleList = rbacService.searchRole(EnumSet.of(RoleLevelEnum.PRODUCT_ADMIN), range,
                null, null, true);
        // ?????? ??????????????? ??????????????????????????????
        Long roleIdOfProductAdmin = roleList.get(0).getId();
        List<Long> userIdList = param.getAdmins().stream().map(UserSimpleDTO::getId).collect(Collectors.toList());
        rbacService.setNewUserListForRole(roleIdOfProductAdmin, userIdList, range, appId, true);

        // ????????????????????????Android, iPhone, Web
        List<TerminalSimpleDTO> presentedTerminals = terminalService.getPresented(appId, currentUser);
        for (TerminalSimpleDTO terminal : presentedTerminals) {
            Long terminalId = terminalService.create(terminal);
            // ??????????????????
            versionService.presetVersion(appId, terminalId, EntityTypeEnum.TERMINAL.getType(), currentUser);
        }
        return appId;
    }

    public Integer updateApp(AppUpdateParam param) {
        // ????????????
        Preconditions.checkArgument(null != param, "????????????????????????");
        Preconditions.checkArgument(null != param.getId(), "????????????ID????????????");

        Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);
        Preconditions.checkArgument(null != domainId, "??????????????????");

        // ?????????????????????????????????
        AppSimpleDTO existsApp = appService.getAppById(param.getId());
        Preconditions.checkArgument(null != existsApp, "?????????????????????????????????");
        Preconditions.checkArgument(domainId == existsApp.getDomainId(), "????????????????????????????????????????????????????????????");

        // ????????????
        AppSimpleDTO appSimpleDTO = BeanConvertUtils.convert(param, AppSimpleDTO.class);
        appSimpleDTO.setDomainId(domainId);

        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
        appSimpleDTO.setUpdater(currentUser);

        RoleTypeEnum range = RoleTypeEnum.APP;

        try {
            // ??????????????????
            Integer updateResult = appService.updateApp(appSimpleDTO);

            List<Long> userIdList = param.getAdmins().stream().map(UserSimpleDTO::getId).collect(Collectors.toList());
            List<Role> roleList = rbacService.searchRole(EnumSet.of(RoleLevelEnum.PRODUCT_ADMIN), range,
                    null, null, true);
            Long roleIdOfProductAdmin = roleList.get(0).getId();
            // ?????? ??????????????? ??????????????????????????????
            rbacService.setNewUserListForRole(roleIdOfProductAdmin, userIdList, range, appSimpleDTO.getId(), false);

            return updateResult;
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("?????????????????????????????????");
        }
    }

    public Integer deleteApp(Long appId) {
        Preconditions.checkArgument(null != appId, "??????ID????????????");

        Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);
        Preconditions.checkArgument(null != domainId, "??????????????????");

        // ????????????????????????
        boolean hasMember = rbacService.checkIsHaveMember(RoleTypeEnum.APP, appId);
        if (hasMember) {
            throw new UserManagementException("?????????????????????????????????");
        }

        return appService.deleteApp(appId);
    }

    public PagingResultDTO<AppSimpleDTO> listApps(String search, PagingSortDTO pagingSortDTO) {
        // ????????????
        Preconditions.checkArgument(null != pagingSortDTO, "??????????????????");

        Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);
        Preconditions.checkArgument(null != domainId, "??????????????????");

        // ????????????
        Integer totalNum = appService.searchAppSize(search, domainId);
        // ??????????????????
        List<AppSimpleDTO> apps = appService.searchApp(search, domainId, pagingSortDTO.getOrderBy(),
                pagingSortDTO.getOrderRule(), pagingSortDTO.getOffset(), pagingSortDTO.getPageSize());

        // ??????????????????????????????
        rbacService.setUserDTOListOfRole(apps, RoleTypeEnum.APP, RoleLevelEnum.PRODUCT_ADMIN);

        PagingResultDTO<AppSimpleDTO> result = new PagingResultDTO<>();
        result.setTotalNum(totalNum)
                .setPageNum(pagingSortDTO.getCurrentPage())
                .setList(apps);
        return result;
    }

    public AppDTO getApp(Long appId) {
        // ????????????
        Preconditions.checkArgument(null != appId, "??????ID????????????");

        Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);
        Preconditions.checkArgument(null != domainId, "??????????????????");
        // ????????????
        AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
        Preconditions.checkArgument(null != appSimpleDTO, "???????????????");

        AppDTO appDTO = BeanConvertUtils.convert(appSimpleDTO, AppDTO.class);

        // ??????????????????????????????
        rbacService.setUserDTOListOfRole(Collections.singletonList(appDTO),RoleTypeEnum.APP, RoleLevelEnum.PRODUCT_ADMIN);

        DomainSimpleDTO domainSimpleDTO = domainService.getDomainById(appSimpleDTO.getDomainId());
        appDTO.setDomain(domainSimpleDTO);

        return appDTO;
    }

    public AppSimpleDTO getSimpleApp(Long appId) {
        // ????????????
        Preconditions.checkArgument(null != appId, "??????ID????????????");
        AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
        return appSimpleDTO;
    }
}
