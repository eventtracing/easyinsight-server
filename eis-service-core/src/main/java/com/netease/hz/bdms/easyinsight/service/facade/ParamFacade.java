package com.netease.hz.bdms.easyinsight.service.facade;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netease.eis.adapters.RealtimeConfigAdapter;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.app.AppSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.*;
import com.netease.hz.bdms.easyinsight.common.dto.obj.ObjectBasicDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamListItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamValueSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindValueSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parampool.ObjBusinessPrivateParamDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parampool.ObjBusinessPrivateParamSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parampool.ParamPoolItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.paramvalue.RuleTemplateSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.EntityTypeEnum;
import com.netease.hz.bdms.easyinsight.common.enums.ParamAggreTypeEum;
import com.netease.hz.bdms.easyinsight.common.enums.ParamTypeEnum;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.exception.DomainException;
import com.netease.hz.bdms.easyinsight.common.exception.ParamException;
import com.netease.hz.bdms.easyinsight.common.param.param.ParamCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.param.ParamUpdateParam;
import com.netease.hz.bdms.easyinsight.common.param.param.parampool.ParamPoolCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.param.parampool.ParamPoolUpdateParam;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.common.vo.param.ParamPoolItemVO;
import com.netease.hz.bdms.easyinsight.dao.model.EisObjTerminalTracker;
import com.netease.hz.bdms.easyinsight.dao.model.ObjectBasic;
import com.netease.hz.bdms.easyinsight.service.service.*;
import com.netease.hz.bdms.easyinsight.service.service.obj.ObjTerminalTrackerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Component
public class ParamFacade implements InitializingBean {

    @Resource
    private RealtimeConfigAdapter realtimeConfigAdapter;

    @Autowired
    ParamService paramService;

    @Autowired
    ParamValueService paramValueService;

    @Autowired
    ParamPoolService paramPoolService;

    @Autowired
    AppService appService;

    @Autowired
    ParamBindService paramBindService;

    @Autowired
    ParamBindValueService paramBindValueService;

    @Autowired
    ObjTerminalTrackerService objTrackerService;

    @Autowired
    RuleTemplateService ruleTemplateService;

    @Autowired
    ObjectBasicService objectBasicService;

    private Set<String> forbiddenParamCodes = new HashSet<>();

    public Long createParam(ParamCreateParam param) {
        // ????????????
        Preconditions.checkArgument(null != param, "??????????????????");
        Preconditions.checkArgument(null != param.getParamType(), "????????????????????????");
        Preconditions.checkArgument(null != param.getValueType(), "???????????????????????????");

        if (forbiddenParamCodes.contains(param.getCode())) {
            throw new CommonException("?????????" + param.getCode() + "??????????????????");
        }

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");
        AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
        Preconditions.checkArgument(null != appSimpleDTO, "??????????????????");

        // ?????????????????????????????????
//    Integer existsParamSize = paramService.getParamSizeByCode(param.getCode(), appId);
//    Preconditions.checkArgument(existsParamSize <= 0, param.getCode() + "?????????");
//
//    // ??????????????????????????????????????????????????????????????????
//    Integer existsParamPoolItemSize = paramPoolService
//        .getParamPoolItemSizeByCode(param.getCode(), appId);
//    Preconditions.checkArgument(existsParamPoolItemSize <= 0, param.getCode() + "?????????");

        // ????????????
        ParamSimpleDTO paramSimpleDTO = BeanConvertUtils.convert(param, ParamSimpleDTO.class);
        paramSimpleDTO.setAppId(appId);

        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
        paramSimpleDTO.setCreator(currentUser)
                .setUpdater(currentUser);
        try {
            return paramService.createParam(paramSimpleDTO);
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("?????????????????????????????????");
        }
    }


    public Long createObjBusinessPrivateParam(ParamCreateParam param) {
        // ????????????
        Preconditions.checkArgument(null != param, "??????????????????");
        Preconditions.checkArgument(null != param.getParamType(), "????????????????????????");
        Preconditions.checkArgument(null != param.getValueType(), "???????????????????????????");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");
        AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
        Preconditions.checkArgument(null != appSimpleDTO, "??????????????????");

        if (forbiddenParamCodes.contains(param.getCode())) {
            throw new CommonException("?????????" + param.getCode() + "??????????????????");
        }

        // ?????????????????????????????????
        List<ParamSimpleDTO> existsParam = paramService.getParamByCode(param.getCode(), appId);
        if (CollectionUtils.isNotEmpty(existsParam)) {
            for (ParamSimpleDTO existParam : existsParam) {
                if (existParam.getName().equalsIgnoreCase(param.getName())) {
                    throw new ParamException(param.getName() + "?????????");
                }
            }
        }

        // ????????????
        ParamSimpleDTO paramSimpleDTO = BeanConvertUtils.convert(param, ParamSimpleDTO.class);
        paramSimpleDTO.setAppId(appId);

        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
        paramSimpleDTO.setCreator(currentUser)
                .setUpdater(currentUser);
        try {
            return paramService.createParam(paramSimpleDTO);
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("?????????????????????????????????");
        }
    }

    public Integer updateParam(ParamUpdateParam param) {
        // ????????????
        Preconditions.checkArgument(null != param, "??????????????????");
        Preconditions.checkArgument(null != param.getParamType(), "????????????????????????");
        Preconditions.checkArgument(null != param.getId(), "??????????????????ID????????????");
        Preconditions.checkArgument(StringUtils.isNotBlank(param.getCode()), "??????ID????????????");
        Preconditions.checkArgument(null != param.getParamType(), "????????????????????????");
        Preconditions.checkArgument(null != param.getValueType(), "???????????????????????????");

        if (forbiddenParamCodes.contains(param.getCode())) {
            throw new CommonException("?????????" + param.getCode() + "??????????????????");
        }

        // ?????????????????????????????????
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");

        ParamSimpleDTO existsParam = paramService.getParamById(param.getId());
        Preconditions.checkArgument(null != existsParam, "?????????????????????????????????");
        Preconditions.checkArgument(appId == existsParam.getAppId(), "??????????????????????????????????????????????????????????????????");

        // ??????????????????code???????????????
        List<ParamSimpleDTO> existsParams = paramService.getParamByCode(param.getCode(), appId);
        if (existsParams == null) {
            existsParams = new ArrayList<>(0);
        }
        // ????????????????????????paramType????????????????????????ID????????????
        existsParams = existsParams.stream().filter(dto -> !param.getId().equals(dto.getId()) && param.getParamType().equals(dto.getParamType())).collect(Collectors.toList());
        // ????????????
        if (CollectionUtils.isNotEmpty(existsParams)) {
            throw new CommonException("??????code??????????????? " + JsonUtils.toJson(existsParams));
        }

        // ????????????
        ParamSimpleDTO paramSimpleDTO = BeanConvertUtils.convert(param, ParamSimpleDTO.class);
        paramSimpleDTO.setAppId(appId);

        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
        paramSimpleDTO.setUpdater(currentUser);
        try {
            return paramService.updateParam(paramSimpleDTO);
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("??????????????????????????????????????????");
        }
    }


    public Integer updateObjBusinessPrivateParam(ParamUpdateParam param) {
        // ????????????
        Preconditions.checkArgument(null != param, "??????????????????");
        Preconditions.checkArgument(null != param.getParamType(), "????????????????????????");
        Preconditions.checkArgument(null != param.getId(), "??????????????????ID????????????");
        Preconditions.checkArgument(StringUtils.isNotBlank(param.getCode()), "??????ID????????????");
        Preconditions.checkArgument(null != param.getParamType(), "????????????????????????");
        Preconditions.checkArgument(null != param.getValueType(), "???????????????????????????");

        if (forbiddenParamCodes.contains(param.getCode())) {
            throw new CommonException("?????????" + param.getCode() + "??????????????????");
        }

        // ?????????????????????????????????
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");

        ParamSimpleDTO existsParam = paramService.getParamById(param.getId());
        Preconditions.checkArgument(null != existsParam, "?????????????????????????????????");
        Preconditions.checkArgument(appId.equals(existsParam.getAppId()), "??????????????????????????????????????????????????????????????????");

        // ??????????????????code???????????????
        List<ParamSimpleDTO> existsParamWithSameCode = paramService
                .getParamByCode(param.getCode(), appId);
        if (CollectionUtils.isNotEmpty(existsParamWithSameCode)) {
            for (ParamSimpleDTO paramSimpleDTO : existsParamWithSameCode) {
                if (!param.getId().equals(paramSimpleDTO.getId())) {
                    if (param.getParamType().equals(ParamTypeEnum.OBJ_BUSINESS_PRIVATE_PARAM.getType())) {
                        throw new ParamException(param.getCode() + "?????????");
                    } else if (paramSimpleDTO.getName().equalsIgnoreCase(param.getName())) {
                        throw new ParamException(param.getName() + "?????????");
                    }
                }
            }
        }

        // ????????????
        ParamSimpleDTO paramSimpleDTO = BeanConvertUtils.convert(param, ParamSimpleDTO.class);
        paramSimpleDTO.setAppId(appId);

        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
        paramSimpleDTO.setUpdater(currentUser);
        try {
            return paramService.updateParam(paramSimpleDTO);
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("??????????????????????????????????????????");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Integer deleteParam(Long paramId) {
        Preconditions.checkArgument(null != paramId, "??????ID????????????");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");

        // ??????????????????????????????
        List<ParamBindSimpleDTO> paramBindSimpleDTOS = paramBindService
                .getParamBindByParamId(Collections.singletonList(paramId), null);
        Preconditions.checkArgument(paramBindSimpleDTOS.size() <= 0, "?????????????????????????????????");

        // ??????????????????????????????
        paramValueService.deleteByParamId(paramId);
        return paramService.deleteParam(paramId);
    }

    public List<ParamListItemDTO> listAllParams(String search, List<Integer> paramTypes) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(paramTypes), "????????????????????????");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");

        // ????????????????????????code-type?????????key??????map
        List<ParamSimpleDTO> paramSimpleDTOS = paramService
                .searchParam(search, paramTypes, null, null, null, appId, null, null, null, null, null);
        Map<String, List<ParamSimpleDTO>> paramSimpleDTOMap = Maps.newHashMap();
        for (ParamSimpleDTO paramSimpleDTO : paramSimpleDTOS) {
            String key = paramSimpleDTO.getCode() + "-" + paramSimpleDTO.getParamType();
            List<ParamSimpleDTO> curLists = paramSimpleDTOMap
                    .computeIfAbsent(key, k -> Lists.newLinkedList());
            curLists.add(paramSimpleDTO);
        }

        // ??????????????????
        List<ParamListItemDTO> result = Lists.newArrayList();
        for (String key : paramSimpleDTOMap.keySet()) {
            List<ParamSimpleDTO> curParams = paramSimpleDTOMap.get(key);
            ParamSimpleDTO oneParam = curParams.get(0);
            ParamListItemDTO itemDTO = new ParamListItemDTO();
            itemDTO.setCode(oneParam.getCode())
                    .setParamType(oneParam.getParamType())
                    .setItems(curParams);
            result.add(itemDTO);
        }
        return result;
    }

    public TagAggreListDTO aggreParam(Integer paramType, List<Integer> aggreTypes) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(aggreTypes), "????????????????????????");
        Preconditions.checkArgument(null != paramType, "????????????????????????");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");

        TagAggreListDTO tagAggreListDTO = new TagAggreListDTO();
        for (Integer aggreType : aggreTypes) {
            ParamAggreTypeEum paramAggreTypeEum = ParamAggreTypeEum.fromAggreType(aggreType);
            switch (paramAggreTypeEum) {
                case CREATOR: {
                    List<UserSimpleDTO> creators = paramService.getCreators(appId, paramType);

                    List<CommonAggregateDTO> creatorTags = Lists.newArrayList();
                    if (CollectionUtils.isNotEmpty(creators)) {
                        for (UserSimpleDTO creator : creators) {
                            CommonAggregateDTO creatorTag = new CommonAggregateDTO();
                            creatorTag.setKey(creator.getEmail())
                                    .setValue(creator.getUserName());
                            creatorTags.add(creatorTag);
                        }
                    }
                    tagAggreListDTO.setCreators(creatorTags);
                    break;
                }
            }
        }
        return tagAggreListDTO;
    }

    public PagingResultDTO<ParamSimpleDTO> listParams(String search, Integer paramType,
                                                      List<String> createEmails, List<Integer> valueTypes, PagingSortDTO pagingSortDTO) {
        // ????????????
        Preconditions.checkArgument(null != pagingSortDTO, "??????????????????");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");

        // ????????????
        List<Integer> paramTypes = Lists.newArrayList();
        if (paramType != null) {
            paramTypes.add(paramType);
        }
        Integer totalNum = paramService
                .searchParamSize(search, paramTypes, createEmails, valueTypes, null, appId, null);
        // ??????????????????
        List<ParamSimpleDTO> params = paramService
                .searchParam(search, paramTypes, createEmails, valueTypes, null, appId, null,
                        pagingSortDTO.getOrderBy(),
                        pagingSortDTO.getOrderRule(),
                        pagingSortDTO.getOffset(), pagingSortDTO.getPageSize());

        PagingResultDTO<ParamSimpleDTO> result = new PagingResultDTO<ParamSimpleDTO>();
        result.setTotalNum(totalNum)
                .setPageNum(pagingSortDTO.getCurrentPage())
                .setList(params);
        return result;
    }

    public ParamDTO getParam(Long paramId) {
        // ????????????
        Preconditions.checkArgument(null != paramId, "??????ID????????????");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");

        // ????????????
        ParamSimpleDTO paramSimpleDTO = paramService.getParamById(paramId);
        Preconditions.checkArgument(null != paramSimpleDTO, "???????????????");

        List<ParamValueSimpleDTO> values = paramValueService
                .getById(paramSimpleDTO.getId(), null);
        List<Long> paramValueIds = values.stream().map(ParamValueSimpleDTO::getId)
                .collect(Collectors.toList());
        Map<Long, Boolean> paramValueUsingMap = paramBindValueService.getParamValueUsed(paramValueIds);
        for (ParamValueSimpleDTO paramValue : values) {
            Boolean used = paramValueUsingMap.get(paramValue.getId());
            paramValue.setUsed(null != used ? used : false);
        }

        ParamDTO paramDTO = BeanConvertUtils.convert(paramSimpleDTO, ParamDTO.class);
        paramDTO.setValues(values);
        return paramDTO;
    }

    public List<ParamPoolItemDTO> listParamPoolItem(String search) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");

        return paramPoolService.searchParamPoolItem(search, appId, null, null, null, null);
    }

    /**
     * ???????????????????????????PARAM??????
     * @param search
     * @return
     */
    public List<ParamPoolItemVO> listParamPoolItemWithParam(String search) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");

        // 1. ??????????????????
        List<ParamPoolItemDTO> paramPoolItems = paramPoolService.searchParamPoolItem(search, appId, null, null, null, null);
        if (CollectionUtils.isEmpty(paramPoolItems)) {
            return new ArrayList<>(0);
        }
        List<ParamPoolItemVO> result = paramPoolItems.stream()
                .filter(Objects::nonNull)
                .map(dto -> BeanConvertUtils.convert(dto, ParamPoolItemVO.class))
                .collect(Collectors.toList());


        // 2. ????????????
        List<String> codes = paramPoolItems.stream().map(ParamPoolItemDTO::getCode).distinct().collect(Collectors.toList());
        List<ParamSimpleDTO> paramSimples = paramService.listAllByAppIdAndCodes(codes, appId, ParamTypeEnum.OBJ_BUSINESS_PRIVATE_PARAM.getType());
        if (paramSimples == null) {
            paramSimples = new ArrayList<>(0);
        }
        Map<String, List<ParamSimpleDTO>> groupByCode = paramSimples.stream().collect(Collectors.groupingBy(ParamSimpleDTO::getCode));

        result.forEach(vo -> {
            List<ParamSimpleDTO> paramSimpleDTOS = groupByCode.get(vo.getCode());
            vo.setParams(paramSimpleDTOS == null ? new ArrayList<>(0) : paramSimpleDTOS);
        });
        return result;
    }


    public Long createParamPoolItem(ParamPoolCreateParam param) {
        Preconditions.checkArgument(null != param, "????????????????????????????????????");
        Preconditions.checkArgument(StringUtils.isNotBlank(param.getCode()), "??????????????????code????????????");

        if (forbiddenParamCodes.contains(param.getCode())) {
            throw new CommonException("?????????" + param.getCode() + "??????????????????");
        }

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");
        AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
        Preconditions.checkArgument(null != appSimpleDTO, "??????????????????");

        ParamPoolItemDTO paramPoolItemDTO = BeanConvertUtils.convert(param, ParamPoolItemDTO.class);
        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
        paramPoolItemDTO.setCreator(currentUser);
        paramPoolItemDTO.setUpdater(currentUser);
        paramPoolItemDTO.setAppId(appId);

        // ?????????????????????????????????code
        List<ParamSimpleDTO> paramsOfCode = paramService
                .getParamByCode(param.getCode(), appId);
        if (!CollectionUtils.isEmpty(paramsOfCode)) {
            for (ParamSimpleDTO paramSimpleDTO : paramsOfCode) {
                if (paramSimpleDTO.getParamType().equals(param.getParamType())) {
                    throw new ParamException("??????????????????????????????????????????");
                }
            }
        }

        // ??????????????????????????????????????????????????????????????????
        Integer existsParamPoolItemSize = paramPoolService
                .getParamPoolItemSizeByCode(param.getCode(), appId);
        Preconditions.checkArgument(existsParamPoolItemSize <= 0, param.getCode() + "?????????");

        try {
            return paramPoolService.createParamPoolItem(paramPoolItemDTO);
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("?????????????????????????????????");
        }
    }

    public Integer updateParamPoolItem(ParamPoolUpdateParam param) {
        Preconditions.checkArgument(null != param, "????????????????????????????????????");
        Preconditions.checkArgument(StringUtils.isNotBlank(param.getCode()), "??????????????????code????????????");
        Preconditions.checkArgument(null != param.getId(), "????????????????????????ID????????????");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");
        ParamPoolItemDTO existsParamPoolItem = paramPoolService.getParamPoolItemById(param.getId());
        Preconditions.checkArgument(null != existsParamPoolItem, "???????????????????????????");
        Preconditions.checkArgument(appId == existsParamPoolItem.getAppId(), "??????????????????????????????????????????????????????????????????");

//    // ?????????????????????????????????code
//    Integer existsParamSize = paramService
//        .getParamSizeByCode(param.getCode(), appId);
//    Preconditions.checkArgument(existsParamSize <= 0, param.getCode() + "?????????");

        // ????????????????????????????????????????????????????????????????????????code????????????
        List<ParamPoolItemDTO> paramPoolItemDTOS = paramPoolService
                .getParamPoolItemByCode(param.getCode(), appId);
        if (CollectionUtils.isNotEmpty(paramPoolItemDTOS)) {
            for (ParamPoolItemDTO paramPoolItem : paramPoolItemDTOS) {
                if (!param.getId().equals(paramPoolItem.getId())) {
                    throw new ParamException(param.getCode() + "?????????");
                }
            }
        }

        ParamPoolItemDTO paramPoolItemDTO = BeanConvertUtils.convert(param, ParamPoolItemDTO.class);
        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
        paramPoolItemDTO.setUpdater(currentUser);

        try {
            // ???????????????code??????
            paramService.updateCode(appId, ParamTypeEnum.OBJ_BUSINESS_PRIVATE_PARAM.getType(), existsParamPoolItem.getCode(), param.getCode());
            return paramPoolService.updateParamPoolItem(paramPoolItemDTO);
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("??????????????????????????????????????????");
        } catch (Exception e) {
            log.debug("", e);
            throw new DomainException("????????????");
        }
    }

    public Integer deleteParamPoolItem(Long paramPoolId) {
        Preconditions.checkArgument(null != paramPoolId, "????????????????????????????????????");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");

        ParamPoolItemDTO paramPoolItemDTO = paramPoolService.getParamPoolItemById(paramPoolId);

        if (paramPoolItemDTO != null) {
            // ?????????????????????????????????
            Integer existsParamSize = paramService.getParamSizeByCode(paramPoolItemDTO.getCode(), appId);
            Preconditions.checkArgument(existsParamSize <= 0, "?????????????????????????????????????????????");

            return paramPoolService.deleteParamPoolItem(paramPoolId);
        }
        return 0;
    }

    public PagingResultDTO<ObjBusinessPrivateParamSimpleDTO> listObjBusinessPrivateParams(
            String code, List<String> createEmails,
            String search, List<Integer> valueTypes,
            PagingSortDTO pagingSortDTO) {
        // ????????????
        Preconditions.checkArgument(null != pagingSortDTO, "??????????????????");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");

        Integer paramType = ParamTypeEnum.OBJ_BUSINESS_PRIVATE_PARAM.getType();
        List<Integer> paramTypes = Lists.newArrayList();
        if (paramType != null) {
            paramTypes.add(paramType);
        }

        // ???search?????????????????????????????????????????????????????????ID??? ????????????????????????????????????????????????????????????
        List<Long> candidateParamIdFromObjs = Lists.newArrayList();
        if (StringUtils.isNotBlank(search)) {
            ObjectBasic objectBasicQuery = new ObjectBasic();
            objectBasicQuery.setAppId(appId);
            List<ObjectBasic> objectBasicList = objectBasicService.search(objectBasicQuery);
            objectBasicList = objectBasicList.stream()
                    .filter(obj -> obj.getOid().contains(search))
                    .collect(Collectors.toList());
            Set<Long> candidateObjIds = objectBasicList.stream()
                    .map(ObjectBasic::getId)
                    .collect(Collectors.toSet());
            EisObjTerminalTracker trackerQuery = new EisObjTerminalTracker();
            trackerQuery.setAppId(appId);
            List<EisObjTerminalTracker> objTrackerList = objTrackerService.search(trackerQuery);
            Set<Long> candidateTrackerIds = objTrackerList.stream()
                    .filter(k -> candidateObjIds.contains(k.getObjId()))
                    .map(EisObjTerminalTracker::getId)
                    .collect(Collectors.toSet());
            if(!candidateTrackerIds.isEmpty()) {
                List<ParamBindSimpleDTO> paramBindSimpleDTOS = paramBindService.getByEntityIds(
                        candidateTrackerIds, Collections.singleton(EntityTypeEnum.OBJTRACKER.getType()), null, appId);
                candidateParamIdFromObjs = paramBindSimpleDTOS.stream()
                        .map(ParamBindSimpleDTO::getParamId)
                        .collect(Collectors.toList());
            }
        }

        // ???search????????????????????????
        List<Long> candidateParamIdFromParams = paramService.searchParamIdsByName(
                search, paramTypes, createEmails, valueTypes, code, appId, null);
        Set<Long> candidateIds = Sets.newHashSet();
        if (CollectionUtils.isNotEmpty(candidateParamIdFromObjs)) {
            candidateIds.addAll(candidateParamIdFromObjs);
        }
        if (CollectionUtils.isNotEmpty(candidateParamIdFromParams)) {
            candidateIds.addAll(candidateParamIdFromParams);
        }


        // ????????????: ??????search???????????????????????????????????????????????????????????????candidateIds???????????????????????????search??????
        Integer totalNum = paramService.searchParamSize(
                null, paramTypes, createEmails, valueTypes, code, appId, candidateIds);
        // ??????????????????: ??????search???????????????????????????????????????????????????????????????candidateIds???????????????????????????search??????
        List<ParamSimpleDTO> params = paramService.searchParam(
                null, paramTypes, createEmails, valueTypes, code, appId, candidateIds,
                pagingSortDTO.getOrderBy(),
                pagingSortDTO.getOrderRule(),
                pagingSortDTO.getOffset(),
                pagingSortDTO.getPageSize());

        // ???????????????????????????????????????
        List<ObjBusinessPrivateParamSimpleDTO> objBusinessPrivateParamSimpleDTOS = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(params)) {
            // ??????paramId?????????????????????
            List<Long> paramIds = params.stream().map(ParamSimpleDTO::getId).collect(Collectors.toList());
            List<ParamBindSimpleDTO> paramBindSimpleDTOS = paramBindService
                    .getParamBindByParamId(paramIds, EntityTypeEnum.OBJTRACKER.getType());

            // ?????????????????????????????????
            Set<Long> trackerIds = paramBindSimpleDTOS.stream()
                    .map(ParamBindSimpleDTO::getEntityId)
                    .collect(Collectors.toSet());
            List<EisObjTerminalTracker> objTrackers = objTrackerService.getByIds(trackerIds);
            Set<Long> objIds = objTrackers.stream()
                    .map(EisObjTerminalTracker::getObjId)
                    .collect(Collectors.toSet());

            List<ObjectBasic> basicObjs = objectBasicService.getByIds(objIds);
            Map<Long, ObjectBasic> objId2BasicObjMap = basicObjs.stream().collect(
                    Collectors.toMap(ObjectBasic::getId, Function.identity(), (k1, k2) -> k1));

            Map<Long, ObjectBasic> trackerId2BasicObjMap = Maps.newHashMap();
            for (EisObjTerminalTracker objTracker : objTrackers) {
                ObjectBasic basicObj = objId2BasicObjMap.get(objTracker.getObjId());
                trackerId2BasicObjMap.put(objTracker.getId(), basicObj);
            }

            // ??????paramId???????????????ID
            Map<Long, Set<ObjectBasic>> paramId2BasicObjMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(paramBindSimpleDTOS)) {
                for (ParamBindSimpleDTO paramBindSimpleDTO : paramBindSimpleDTOS) {
                    Long paramId = paramBindSimpleDTO.getParamId();
                    Long trackerId = paramBindSimpleDTO.getEntityId(); // ????????????ID
                    ObjectBasic objBasic = trackerId2BasicObjMap.get(trackerId);
                    Set<ObjectBasic> tmpObjs = paramId2BasicObjMap
                            .computeIfAbsent(paramId, k -> Sets.newHashSet());
                    tmpObjs.add(objBasic);
                }
            }
            // ????????????param?????????????????????
            for (ParamSimpleDTO param : params) {
                ObjBusinessPrivateParamSimpleDTO objBusinessPrivateParamSimpleDTO = BeanConvertUtils
                        .convert(param, ObjBusinessPrivateParamSimpleDTO.class);
                Set<ObjectBasic> objectBasicSet = paramId2BasicObjMap
                        .computeIfAbsent(param.getId(), k -> Sets.newHashSet());
                Set<ObjectBasicDTO> objectBasicDTOSet = objectBasicSet.stream()
                        .map(k -> BeanConvertUtils.convert(k, ObjectBasicDTO.class))
                        .collect(Collectors.toSet());
                objBusinessPrivateParamSimpleDTO.setBinds(objectBasicDTOSet);
                objBusinessPrivateParamSimpleDTOS.add(objBusinessPrivateParamSimpleDTO);
            }
        }

        PagingResultDTO<ObjBusinessPrivateParamSimpleDTO> result = new PagingResultDTO<>();
        result.setTotalNum(totalNum)
                .setPageNum(pagingSortDTO.getCurrentPage())
                .setList(objBusinessPrivateParamSimpleDTOS);
        return result;
    }

    public ObjBusinessPrivateParamDTO getObjBusinessPrivateParams(Long paramId) {
        // ????????????
        Preconditions.checkArgument(null != paramId, "??????ID????????????");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");

        // ????????????
        ParamSimpleDTO paramSimpleDTO = paramService.getParamById(paramId);
        Preconditions.checkArgument(null != paramSimpleDTO, "???????????????");

        List<ParamValueSimpleDTO> values = paramValueService
                .getById(paramSimpleDTO.getId(), null);

        // ??????????????????
        List<Long> paramValueIds = values.stream().map(ParamValueSimpleDTO::getId)
                .collect(Collectors.toList());
        Map<Long, Boolean> paramValueUsingMap = paramBindValueService.getParamValueUsed(paramValueIds);
        for (ParamValueSimpleDTO paramValue : values) {
            Boolean used = paramValueUsingMap.get(paramValue.getId());
            paramValue.setUsed(null != used ? used : false);
        }

        ObjBusinessPrivateParamDTO paramDTO = BeanConvertUtils
                .convert(paramSimpleDTO, ObjBusinessPrivateParamDTO.class);
        paramDTO.setValues(values);
        return paramDTO;
    }

    public List<RuleTemplateSimpleDTO> getParamRuleTemplate() {
        return ruleTemplateService.getAllRuleTemplate();
    }

    public void addParamRuleTemplate(RuleTemplateSimpleDTO ruleTemplateSimpleDTO) {
        if (ruleTemplateSimpleDTO == null) {
            throw new CommonException("ruleTemplateSimpleDTO is null");
        }
        if (StringUtils.isBlank(ruleTemplateSimpleDTO.getName())) {
            throw new CommonException("?????????????????????");
        }
        if (StringUtils.isBlank(ruleTemplateSimpleDTO.getRule())) {
            throw new CommonException("?????????????????????");
        }
        List<RuleTemplateSimpleDTO> allRuleTemplate = ruleTemplateService.getAllRuleTemplate();
        if (CollectionUtils.isNotEmpty(allRuleTemplate)) {
            allRuleTemplate.forEach(ruleTemplate -> {
                if (ruleTemplate.getName().equals(ruleTemplateSimpleDTO.getName())) {
                    throw new CommonException("??????????????????" + ruleTemplateSimpleDTO.getName());
                }
            });
        }
        UserDTO userDTO = EtContext.get(ContextConstant.USER);
        if (userDTO == null) {
            throw new CommonException("????????????????????????");
        }
        ruleTemplateSimpleDTO.setCreator(new UserSimpleDTO(userDTO.getEmail(), userDTO.getUserName()));
        ruleTemplateSimpleDTO.setUpdater(new UserSimpleDTO(userDTO.getEmail(), userDTO.getUserName()));
        ruleTemplateSimpleDTO.setCreateTime(System.currentTimeMillis());
        ruleTemplateSimpleDTO.setUpdateTime(System.currentTimeMillis());
        ruleTemplateService.add(ruleTemplateSimpleDTO);
    }

    public void deleteParamRuleTemplate(Long id) {
        ruleTemplateService.delete(id);
    }

    /**
     * todo ???????????????
     * ????????????
     *
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void syncParamBindDescription(){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");
        List<ParamBindSimpleDTO> paramBindSimpleDTOS = paramBindService.getByAppId(appId);

        Set<Long> paramBindIds = paramBindSimpleDTOS.stream()
                .filter(k -> Strings.isNullOrEmpty(k.getDescription()))
                .map(ParamBindSimpleDTO::getId)
                .collect(Collectors.toSet());
        List<ParamBindValueSimpleDTO> paramBindValueSimpleDTOS = paramBindValueService
                .getByBindIds(paramBindIds);
        Map<Long, Set<Long>> paramBindIdToValueIdsMap = Maps.newHashMap();
        for (ParamBindValueSimpleDTO paramBindValueSimpleDTO : paramBindValueSimpleDTOS) {
            Long paramBindId = paramBindValueSimpleDTO.getBindId();
            Long valueId = paramBindValueSimpleDTO.getParamValueId();
            Set<Long> valueIds = paramBindIdToValueIdsMap.computeIfAbsent(paramBindId, k -> Sets.newHashSet());
            valueIds.add(valueId);
        }

        Set<Long> valueIds = paramBindValueSimpleDTOS.stream()
                .map(ParamBindValueSimpleDTO::getParamValueId)
                .collect(Collectors.toSet());
        List<ParamValueSimpleDTO> paramValueSimpleDTOS = paramValueService.getByIds(valueIds);
        Map<Long, ParamValueSimpleDTO> paramValueMap = paramValueSimpleDTOS.stream()
                .filter(k -> !Strings.isNullOrEmpty(k.getDescription()))
                .collect(Collectors.toMap(ParamValueSimpleDTO::getId, Function.identity()));

        for (Long paramBindId : paramBindIdToValueIdsMap.keySet()) {
            Set<Long> valueIdSet = paramBindIdToValueIdsMap.get(paramBindId);
            String description = valueIdSet.stream()
                    .filter(paramValueMap::containsKey)
                    .map(paramValueMap::get)
                    .map(ParamValueSimpleDTO::getDescription)
                    .collect(Collectors.joining("|"));

            ParamBindSimpleDTO paramBindSimpleDTO = new ParamBindSimpleDTO();
            paramBindSimpleDTO.setId(paramBindId);
            paramBindSimpleDTO.setDescription(description);
            paramBindService.updateParamBind(paramBindSimpleDTO);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        realtimeConfigAdapter.listenJSON("forbiddenParamCodes", (s) -> forbiddenParamCodes = JsonUtils.parseObject(s, new TypeReference<Set<String>>() {}));
    }

}
