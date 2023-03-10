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
        // ????????????
        Preconditions.checkArgument(null != param, "????????????????????????");
        Preconditions.checkArgument(StringUtils.isNotBlank(param.getName()), "????????????????????????");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");
        AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
        Preconditions.checkArgument(null != appSimpleDTO, "??????????????????");

        // ?????????????????????????????????
        List<TerminalSimpleDTO> existsTerminals = terminalService
                .getByName(param.getName(), appId);
        Preconditions.checkArgument(CollectionUtils.isEmpty(existsTerminals), "?????????????????????????????????");

        // ??????????????????
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
            throw new DomainException("?????????????????????????????????");
        }

        // ??????????????????
        if (Boolean.TRUE.equals(presetVersion)) {
            Long versionId = versionService
                    .presetVersion(appId, terminalId, EntityTypeEnum.TERMINAL.getType(), currentUser);

            // ?????????????????????????????????
            List<ParamSimpleDTO> paramSimpleDTOS = paramService
                    .searchParam(null, Collections.singletonList(ParamTypeEnum.GLOBAL_PUBLIC_PARAM
                            .getType()), null, null, null, appId, null, null, null, null, null);
            if (CollectionUtils.isNotEmpty(paramSimpleDTOS)) {
                Set<Long> paramIds = paramSimpleDTOS.stream().map(ParamSimpleDTO::getId)
                        .collect(Collectors.toSet());
                List<ParamValueSimpleDTO> paramValues = paramValueService.getByParamIds(paramIds);
                Map<Long, List<Long>> paramId2ParamValueIdMap = Maps.newHashMap();// key????????????ID, value???????????????ID
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
        // ????????????
        Preconditions.checkArgument(null != param, "????????????????????????");
        Preconditions.checkArgument(null != param.getId(), "??????ID????????????");
        Preconditions.checkArgument(StringUtils.isNotBlank(param.getName()), "????????????????????????");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");

        TerminalSimpleDTO existsTerminal = terminalService.getById(param.getId());
        Preconditions.checkArgument(null != existsTerminal, "?????????????????????????????????");
        Preconditions.checkArgument(appId == existsTerminal.getAppId(), "?????????????????????????????????????????????????????????????????????");

        // ????????????
        TerminalSimpleDTO terminalSimpleDTO = BeanConvertUtils.convert(param, TerminalSimpleDTO.class);
        terminalSimpleDTO.setAppId(appId);

        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
        terminalSimpleDTO.setUpdater(currentUser);
        try {
            return terminalService.update(terminalSimpleDTO);
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("?????????????????????????????????");
        }
    }


    public Integer getPresetTerminalSize(Long appId) {
        return terminalService.searchTerminalSize(null, null, true, appId);
    }

    public PagingResultDTO<TerminalSimpleDTO> listTerminals(String search,
                                                            List<Integer> terminalTypes, PagingSortDTO pagingSortDTO) {
        // ????????????
        Preconditions.checkArgument(null != pagingSortDTO, "??????????????????");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");

        // ????????????
        Integer totalNum = terminalService.searchTerminalSize(search, terminalTypes, null, appId);
        // ??????????????????
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
        Preconditions.checkArgument(null != terminalId, "??????ID????????????");

        return terminalService.getById(terminalId);
    }


    public void presetTerminal(Long appId, UserSimpleDTO currentUser) {
        // ????????????????????????Android, iPhone, Web
        List<TerminalSimpleDTO> presentedTerminals = terminalService.getPresented(appId, currentUser);
        if (CollectionUtils.isNotEmpty(presentedTerminals)) {
            // ?????????????????????????????????
            List<ParamSimpleDTO> paramSimpleDTOS = paramService
                    .searchParam(null, Collections.singletonList(ParamTypeEnum.GLOBAL_PUBLIC_PARAM.getType()),
                            null, null, null, appId, null,
                            null, null, null, null);
            List<ParamBindItermParam> paramBindItems = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(paramSimpleDTOS)) {
                Set<Long> paramIds = paramSimpleDTOS.stream().map(ParamSimpleDTO::getId)
                        .collect(Collectors.toSet());
                List<ParamValueSimpleDTO> paramValues = paramValueService.getByParamIds(paramIds);
                Map<Long, List<Long>> paramId2ParamValueIdMap = Maps.newHashMap();// key????????????ID, value???????????????ID
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
                throw new TerminalException("??????????????????????????????????????????");
            }

            for (TerminalSimpleDTO terminal : presentedTerminals) {
                Long terminalId = terminalService.create(terminal);
                // ??????????????????
                Long versionId = versionService
                        .presetVersion(appId, terminalId, EntityTypeEnum.TERMINAL.getType(), currentUser);
                // ??????????????????
                paramBindHelper
                        .createParamBind(paramBindItems, appId, terminalId, EntityTypeEnum.TERMINAL
                                .getType(), versionId, currentUser, currentUser);
            }

        }
    }

    public void presetTerminalVersion(Long appId, Long terminalId, UserSimpleDTO currentUser) {
        // ????????????????????????Android???iPhone
        TerminalSimpleDTO terminalSimpleDTO = terminalService.getById(terminalId);
        Preconditions.checkArgument(null != terminalSimpleDTO, "???????????????");

        // ??????????????????????????????
        List<VersionSimpleDTO> presetVersions = versionService
                .getVersionByEntityId(terminalId, EntityTypeEnum.TERMINAL.getType(), "????????????", appId);
        if (CollectionUtils.isNotEmpty(presetVersions)) {
            for (VersionSimpleDTO presetVersion : presetVersions) {
                if (presetVersion.getPreset()) {
                    throw new TerminalException(terminalSimpleDTO.getName() + "?????????????????????");
                }
            }
        }

        // ?????????????????????????????????
        List<ParamSimpleDTO> paramSimpleDTOS = paramService
                .searchParam(null, Collections.singletonList(ParamTypeEnum.GLOBAL_PUBLIC_PARAM
                        .getType()), null, null, null, appId, null, null, null, null, null);
        List<ParamBindItermParam> paramBindItems = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(paramSimpleDTOS)) {
            Set<Long> paramIds = paramSimpleDTOS.stream().map(ParamSimpleDTO::getId)
                    .collect(Collectors.toSet());
            List<ParamValueSimpleDTO> paramValues = paramValueService.getByParamIds(paramIds);
            Map<Long, List<Long>> paramId2ParamValueIdMap = Maps.newHashMap();// key????????????ID, value???????????????ID
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
            throw new TerminalException("??????????????????????????????????????????");
        }

        Long versionId = versionService
                .presetVersion(appId, terminalId, EntityTypeEnum.TERMINAL.getType(), currentUser);
        // ??????????????????
        paramBindHelper
                .createParamBind(paramBindItems, appId, terminalId, EntityTypeEnum.TERMINAL
                        .getType(), versionId, currentUser, currentUser);

    }

}
