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
        // 验证参数
        Preconditions.checkArgument(null != param, "产品对象不能为空");

        Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);
        Preconditions.checkArgument(null != domainId, "未指定域信息");
        DomainSimpleDTO domainSimpleDTO = domainService.getDomainById(domainId);
        Preconditions.checkArgument(null != domainSimpleDTO, "该域不存在");

        AppSimpleDTO appSimpleDTO = BeanConvertUtils.convert(param, AppSimpleDTO.class);
        appSimpleDTO.setDomainId(domainId);

        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
        appSimpleDTO.setCreator(currentUser)
                .setUpdater(currentUser);

        Long appId;
        try {
            // 插入记录
            appId = appService.createApp(appSimpleDTO);
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("该产品已存在，创建失败");
        }

        RoleTypeEnum range = RoleTypeEnum.APP;

        List<Role> roleList = rbacService.searchRole(EnumSet.of(RoleLevelEnum.PRODUCT_ADMIN), range,
                null, null, true);
        // 创建 产品管理员 这个角色和用户的关系
        Long roleIdOfProductAdmin = roleList.get(0).getId();
        List<Long> userIdList = param.getAdmins().stream().map(UserSimpleDTO::getId).collect(Collectors.toList());
        rbacService.setNewUserListForRole(roleIdOfProductAdmin, userIdList, range, appId, true);

        // 插入预置的终端：Android, iPhone, Web
        List<TerminalSimpleDTO> presentedTerminals = terminalService.getPresented(appId, currentUser);
        for (TerminalSimpleDTO terminal : presentedTerminals) {
            Long terminalId = terminalService.create(terminal);
            // 新建预置版本
            versionService.presetVersion(appId, terminalId, EntityTypeEnum.TERMINAL.getType(), currentUser);
        }
        return appId;
    }

    public Integer updateApp(AppUpdateParam param) {
        // 验证参数
        Preconditions.checkArgument(null != param, "产品对象不能为空");
        Preconditions.checkArgument(null != param.getId(), "产品标识ID不能为空");

        Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);
        Preconditions.checkArgument(null != domainId, "未指定域信息");

        // 验证当前产品是否已存在
        AppSimpleDTO existsApp = appService.getAppById(param.getId());
        Preconditions.checkArgument(null != existsApp, "该产品不存在，修改失败");
        Preconditions.checkArgument(domainId == existsApp.getDomainId(), "未指定域信息或该产品不在该域下，修改失败");

        // 插入记录
        AppSimpleDTO appSimpleDTO = BeanConvertUtils.convert(param, AppSimpleDTO.class);
        appSimpleDTO.setDomainId(domainId);

        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
        appSimpleDTO.setUpdater(currentUser);

        RoleTypeEnum range = RoleTypeEnum.APP;

        try {
            // 更新产品信息
            Integer updateResult = appService.updateApp(appSimpleDTO);

            List<Long> userIdList = param.getAdmins().stream().map(UserSimpleDTO::getId).collect(Collectors.toList());
            List<Role> roleList = rbacService.searchRole(EnumSet.of(RoleLevelEnum.PRODUCT_ADMIN), range,
                    null, null, true);
            Long roleIdOfProductAdmin = roleList.get(0).getId();
            // 更新 产品管理员 这个角色和用户的关系
            rbacService.setNewUserListForRole(roleIdOfProductAdmin, userIdList, range, appSimpleDTO.getId(), false);

            return updateResult;
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("该产品已存在，修改失败");
        }
    }

    public Integer deleteApp(Long appId) {
        Preconditions.checkArgument(null != appId, "产品ID不能为空");

        Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);
        Preconditions.checkArgument(null != domainId, "未指定域信息");

        // 判断是否存在成员
        boolean hasMember = rbacService.checkIsHaveMember(RoleTypeEnum.APP, appId);
        if (hasMember) {
            throw new UserManagementException("请先清空该产品下的成员");
        }

        return appService.deleteApp(appId);
    }

    public PagingResultDTO<AppSimpleDTO> listApps(String search, PagingSortDTO pagingSortDTO) {
        // 验证参数
        Preconditions.checkArgument(null != pagingSortDTO, "分页不能为空");

        Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);
        Preconditions.checkArgument(null != domainId, "未指定域信息");

        // 获取大小
        Integer totalNum = appService.searchAppSize(search, domainId);
        // 获取分页明细
        List<AppSimpleDTO> apps = appService.searchApp(search, domainId, pagingSortDTO.getOrderBy(),
                pagingSortDTO.getOrderRule(), pagingSortDTO.getOffset(), pagingSortDTO.getPageSize());

        // 为产品设置管理员集合
        rbacService.setUserDTOListOfRole(apps, RoleTypeEnum.APP, RoleLevelEnum.PRODUCT_ADMIN);

        PagingResultDTO<AppSimpleDTO> result = new PagingResultDTO<>();
        result.setTotalNum(totalNum)
                .setPageNum(pagingSortDTO.getCurrentPage())
                .setList(apps);
        return result;
    }

    public AppDTO getApp(Long appId) {
        // 验证参数
        Preconditions.checkArgument(null != appId, "产品ID不能为空");

        Long domainId = EtContext.get(ContextConstant.DOMAIN_ID);
        Preconditions.checkArgument(null != domainId, "未指定域信息");
        // 获取数据
        AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
        Preconditions.checkArgument(null != appSimpleDTO, "产品不存在");

        AppDTO appDTO = BeanConvertUtils.convert(appSimpleDTO, AppDTO.class);

        // 为产品设置管理员集合
        rbacService.setUserDTOListOfRole(Collections.singletonList(appDTO),RoleTypeEnum.APP, RoleLevelEnum.PRODUCT_ADMIN);

        DomainSimpleDTO domainSimpleDTO = domainService.getDomainById(appSimpleDTO.getDomainId());
        appDTO.setDomain(domainSimpleDTO);

        return appDTO;
    }

    public AppSimpleDTO getSimpleApp(Long appId) {
        // 验证参数
        Preconditions.checkArgument(null != appId, "产品ID不能为空");
        AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
        return appSimpleDTO;
    }
}
