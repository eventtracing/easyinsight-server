package com.netease.hz.bdms.easyinsight.service.facade;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.app.AppSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingResultDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamValueSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.terminal.TerminalSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.version.VersionSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.EntityTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ParamTypeEnum;
import com.netease.hz.bdms.easyinsight.common.exception.DomainException;
import com.netease.hz.bdms.easyinsight.common.exception.TerminalException;
import com.netease.hz.bdms.easyinsight.common.param.param.paramBind.ParamBindItermParam;
import com.netease.hz.bdms.easyinsight.common.param.terminal.TerminalCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.terminal.TerminalUpdateParam;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.service.helper.ParamBindHelper;
import com.netease.hz.bdms.easyinsight.service.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Component
public class TerminalFacade {

    @Autowired
    private TerminalService terminalService;
    @Autowired
    private AppService appService;
    @Autowired
    private VersionService versionService;
    @Autowired
    private ParamBindHelper paramBindHelper;
    @Autowired
    private ParamService paramService;
    @Autowired
    private ParamValueService paramValueService;

    public Long createTerminal(TerminalCreateParam param, Boolean presetVersion) {
        // 验证参数
        Preconditions.checkArgument(null != param, "终端对象不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(param.getName()), "终端名称不能为空");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");
        AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
        Preconditions.checkArgument(null != appSimpleDTO, "该产品不存在");

        // 验证当前终端是否已存在
        List<TerminalSimpleDTO> existsTerminals = terminalService
                .getByName(param.getName(), appId);
        Preconditions.checkArgument(CollectionUtils.isEmpty(existsTerminals), "该终端已存在，创建失败");

        // 存储终端记录
        TerminalSimpleDTO terminalSimpleDTO = BeanConvertUtils.convert(param, TerminalSimpleDTO.class);
        terminalSimpleDTO.setAppId(appId);

        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
        terminalSimpleDTO.setCreator(currentUser)
                .setUpdater(currentUser);

        Long terminalId = null;
        try {
            terminalId = terminalService.create(terminalSimpleDTO);
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("该终端已存在，创建失败");
        }

        // 新建预置版本
        if (Boolean.TRUE.equals(presetVersion)) {
            Long versionId = versionService
                    .presetVersion(appId, terminalId, EntityTypeEnum.TERMINAL.getType(), currentUser);

            // 查询全局公参，预置绑定
            List<ParamSimpleDTO> paramSimpleDTOS = paramService
                    .searchParam(null, Collections.singletonList(ParamTypeEnum.GLOBAL_PUBLIC_PARAM
                            .getType()), null, null, null, appId, null, null, null, null, null);
            if (CollectionUtils.isNotEmpty(paramSimpleDTOS)) {
                Set<Long> paramIds = paramSimpleDTOS.stream().map(ParamSimpleDTO::getId)
                        .collect(Collectors.toSet());
                List<ParamValueSimpleDTO> paramValues = paramValueService.getByParamIds(paramIds);
                Map<Long, List<Long>> paramId2ParamValueIdMap = Maps.newHashMap();// key表示参数ID, value表示参数值ID
                if (CollectionUtils.isNotEmpty(paramValues)) {
                    for (ParamValueSimpleDTO paramValue : paramValues) {
                        List<Long> paramValueIds = paramId2ParamValueIdMap
                                .computeIfAbsent(paramValue.getParamId(), k -> Lists.newArrayList());
                        paramValueIds.add(paramValue.getId());
                    }
                }

                List<ParamBindItermParam> paramBindItems = Lists.newArrayList();
                for (ParamSimpleDTO paramSimpleDTO : paramSimpleDTOS) {
                    Long paramId = paramSimpleDTO.getId();
                    ParamBindItermParam paramBindIterm = new ParamBindItermParam();

                    paramBindIterm.setParamId(paramId)
                            .setValues(paramId2ParamValueIdMap.get(paramId))
                            .setMust(true)
                            .setNotEmpty(true);
                    paramBindItems.add(paramBindIterm);
                }

                paramBindHelper.createParamBind(paramBindItems, appId, terminalId, EntityTypeEnum.TERMINAL
                        .getType(), versionId, currentUser, currentUser);
            }
        }
        return terminalId;
    }

    public Integer updateTerminal(TerminalUpdateParam param) {
        // 验证参数
        Preconditions.checkArgument(null != param, "终端对象不能为空");
        Preconditions.checkArgument(null != param.getId(), "终端ID不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(param.getName()), "终端名称不能为空");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");

        TerminalSimpleDTO existsTerminal = terminalService.getById(param.getId());
        Preconditions.checkArgument(null != existsTerminal, "该终端不存在，修改失败");
        Preconditions.checkArgument(appId == existsTerminal.getAppId(), "未指定产品信息或该终端不在该产品下，，修改失败");

        // 插入记录
        TerminalSimpleDTO terminalSimpleDTO = BeanConvertUtils.convert(param, TerminalSimpleDTO.class);
        terminalSimpleDTO.setAppId(appId);

        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
        terminalSimpleDTO.setUpdater(currentUser);
        try {
            return terminalService.update(terminalSimpleDTO);
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("该终端已存在，修改失败");
        }
    }


    public Integer getPresetTerminalSize(Long appId) {
        return terminalService.searchTerminalSize(null, null, true, appId);
    }

    public PagingResultDTO<TerminalSimpleDTO> listTerminals(String search,
                                                            List<Integer> terminalTypes, PagingSortDTO pagingSortDTO) {
        // 验证参数
        Preconditions.checkArgument(null != pagingSortDTO, "分页不能为空");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");

        // 获取大小
        Integer totalNum = terminalService.searchTerminalSize(search, terminalTypes, null, appId);
        // 获取分页明细
        List<TerminalSimpleDTO> terminals = terminalService
                .search(search, terminalTypes, null, appId, pagingSortDTO.getOrderBy(),
                        pagingSortDTO.getOrderRule(), pagingSortDTO.getOffset(), pagingSortDTO.getPageSize());

        PagingResultDTO<TerminalSimpleDTO> result = new PagingResultDTO<>();
        result.setTotalNum(totalNum)
                .setPageNum(pagingSortDTO.getCurrentPage())
                .setList(terminals);
        return result;
    }

    public TerminalSimpleDTO getTerminal(Long terminalId) {
        Preconditions.checkArgument(null != terminalId, "终端ID不能为空");

        return terminalService.getById(terminalId);
    }


    public void presetTerminal(Long appId, UserSimpleDTO currentUser) {
        // 插入预置的终端：Android, iPhone, Web
        List<TerminalSimpleDTO> presentedTerminals = terminalService.getPresented(appId, currentUser);
        if (CollectionUtils.isNotEmpty(presentedTerminals)) {
            // 查询全局公参，预置绑定
            List<ParamSimpleDTO> paramSimpleDTOS = paramService
                    .searchParam(null, Collections.singletonList(ParamTypeEnum.GLOBAL_PUBLIC_PARAM.getType()),
                            null, null, null, appId, null,
                            null, null, null, null);
            List<ParamBindItermParam> paramBindItems = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(paramSimpleDTOS)) {
                Set<Long> paramIds = paramSimpleDTOS.stream().map(ParamSimpleDTO::getId)
                        .collect(Collectors.toSet());
                List<ParamValueSimpleDTO> paramValues = paramValueService.getByParamIds(paramIds);
                Map<Long, List<Long>> paramId2ParamValueIdMap = Maps.newHashMap();// key表示参数ID, value表示参数值ID
                if (CollectionUtils.isNotEmpty(paramValues)) {
                    for (ParamValueSimpleDTO paramValue : paramValues) {
                        List<Long> paramValueIds = paramId2ParamValueIdMap
                                .computeIfAbsent(paramValue.getParamId(), k -> Lists.newArrayList());
                        paramValueIds.add(paramValue.getId());
                    }
                }

                for (ParamSimpleDTO paramSimpleDTO : paramSimpleDTOS) {
                    Long paramId = paramSimpleDTO.getId();
                    ParamBindItermParam paramBindIterm = new ParamBindItermParam();

                    paramBindIterm.setParamId(paramId)
                            .setValues(paramId2ParamValueIdMap.get(paramId))
                            .setMust(true)
                            .setNotEmpty(true);
                    paramBindItems.add(paramBindIterm);
                }
            }

            if (CollectionUtils.isEmpty(paramBindItems)) {
                throw new TerminalException("全局公参不存在，预置终端失败");
            }

            for (TerminalSimpleDTO terminal : presentedTerminals) {
                Long terminalId = terminalService.create(terminal);
                // 新建预置版本
                Long versionId = versionService
                        .presetVersion(appId, terminalId, EntityTypeEnum.TERMINAL.getType(), currentUser);
                // 新建参数绑定
                paramBindHelper
                        .createParamBind(paramBindItems, appId, terminalId, EntityTypeEnum.TERMINAL
                                .getType(), versionId, currentUser, currentUser);
            }

        }
    }

    public void presetTerminalVersion(Long appId, Long terminalId, UserSimpleDTO currentUser) {
        // 插入预置的终端：Android，iPhone
        TerminalSimpleDTO terminalSimpleDTO = terminalService.getById(terminalId);
        Preconditions.checkArgument(null != terminalSimpleDTO, "终端不存在");

        // 检查预置版本是否存在
        List<VersionSimpleDTO> presetVersions = versionService
                .getVersionByEntityId(terminalId, EntityTypeEnum.TERMINAL.getType(), "预置版本", appId);
        if (CollectionUtils.isNotEmpty(presetVersions)) {
            for (VersionSimpleDTO presetVersion : presetVersions) {
                if (presetVersion.getPreset()) {
                    throw new TerminalException(terminalSimpleDTO.getName() + "已存在预置版本");
                }
            }
        }

        // 查询全局公参，预置绑定
        List<ParamSimpleDTO> paramSimpleDTOS = paramService
                .searchParam(null, Collections.singletonList(ParamTypeEnum.GLOBAL_PUBLIC_PARAM
                        .getType()), null, null, null, appId, null, null, null, null, null);
        List<ParamBindItermParam> paramBindItems = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(paramSimpleDTOS)) {
            Set<Long> paramIds = paramSimpleDTOS.stream().map(ParamSimpleDTO::getId)
                    .collect(Collectors.toSet());
            List<ParamValueSimpleDTO> paramValues = paramValueService.getByParamIds(paramIds);
            Map<Long, List<Long>> paramId2ParamValueIdMap = Maps.newHashMap();// key表示参数ID, value表示参数值ID
            if (CollectionUtils.isNotEmpty(paramValues)) {
                for (ParamValueSimpleDTO paramValue : paramValues) {
                    List<Long> paramValueIds = paramId2ParamValueIdMap
                            .computeIfAbsent(paramValue.getParamId(), k -> Lists.newArrayList());
                    paramValueIds.add(paramValue.getId());
                }
            }

            for (ParamSimpleDTO paramSimpleDTO : paramSimpleDTOS) {
                Long paramId = paramSimpleDTO.getId();
                ParamBindItermParam paramBindIterm = new ParamBindItermParam();

                paramBindIterm.setParamId(paramId)
                        .setValues(paramId2ParamValueIdMap.get(paramId))
                        .setMust(true)
                        .setNotEmpty(true);
                paramBindItems.add(paramBindIterm);
            }
        }

        if (CollectionUtils.isEmpty(paramBindItems)) {
            throw new TerminalException("全局公参不存在，预置终端失败");
        }

        Long versionId = versionService
                .presetVersion(appId, terminalId, EntityTypeEnum.TERMINAL.getType(), currentUser);
        // 新建参数绑定
        paramBindHelper
                .createParamBind(paramBindItems, appId, terminalId, EntityTypeEnum.TERMINAL
                        .getType(), versionId, currentUser, currentUser);

    }

}
