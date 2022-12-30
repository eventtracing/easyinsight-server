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
        // 验证参数
        Preconditions.checkArgument(null != param, "参数不能为空");
        Preconditions.checkArgument(null != param.getParamType(), "参数类型不能为空");
        Preconditions.checkArgument(null != param.getValueType(), "参数值类型不能为空");

        if (forbiddenParamCodes.contains(param.getCode())) {
            throw new CommonException("参数名" + param.getCode() + "已被禁止使用");
        }

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");
        AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
        Preconditions.checkArgument(null != appSimpleDTO, "该产品不存在");

        // 验证当前参数是否已存在
//    Integer existsParamSize = paramService.getParamSizeByCode(param.getCode(), appId);
//    Preconditions.checkArgument(existsParamSize <= 0, param.getCode() + "已存在");
//
//    // 检查参数池（对象业务私参）候选中是否有此参数
//    Integer existsParamPoolItemSize = paramPoolService
//        .getParamPoolItemSizeByCode(param.getCode(), appId);
//    Preconditions.checkArgument(existsParamPoolItemSize <= 0, param.getCode() + "已存在");

        // 插入记录
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
            throw new DomainException("该参数已存在，创建失败");
        }
    }


    public Long createObjBusinessPrivateParam(ParamCreateParam param) {
        // 验证参数
        Preconditions.checkArgument(null != param, "参数不能为空");
        Preconditions.checkArgument(null != param.getParamType(), "参数类型不能为空");
        Preconditions.checkArgument(null != param.getValueType(), "参数值类型不能为空");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");
        AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
        Preconditions.checkArgument(null != appSimpleDTO, "该产品不存在");

        if (forbiddenParamCodes.contains(param.getCode())) {
            throw new CommonException("参数名" + param.getCode() + "已被禁止使用");
        }

        // 验证当前参数是否已存在
        List<ParamSimpleDTO> existsParam = paramService.getParamByCode(param.getCode(), appId);
        if (CollectionUtils.isNotEmpty(existsParam)) {
            for (ParamSimpleDTO existParam : existsParam) {
                if (existParam.getName().equalsIgnoreCase(param.getName())) {
                    throw new ParamException(param.getName() + "已存在");
                }
            }
        }

        // 插入记录
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
            throw new DomainException("该参数已存在，创建失败");
        }
    }

    public Integer updateParam(ParamUpdateParam param) {
        // 验证参数
        Preconditions.checkArgument(null != param, "参数不能为空");
        Preconditions.checkArgument(null != param.getParamType(), "参数类型不能为空");
        Preconditions.checkArgument(null != param.getId(), "参数主键标识ID不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(param.getCode()), "参数ID不能为空");
        Preconditions.checkArgument(null != param.getParamType(), "参数类型不能为空");
        Preconditions.checkArgument(null != param.getValueType(), "参数值类型不能为空");

        if (forbiddenParamCodes.contains(param.getCode())) {
            throw new CommonException("参数名" + param.getCode() + "已被禁止使用");
        }

        // 验证当前参数是否已存在
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");

        ParamSimpleDTO existsParam = paramService.getParamById(param.getId());
        Preconditions.checkArgument(null != existsParam, "该参数不存在，修改失败");
        Preconditions.checkArgument(appId == existsParam.getAppId(), "未指定产品信息或该参数不在该产品下，修改失败");

        // 验证当前参数code是否有重复
        List<ParamSimpleDTO> existsParams = paramService.getParamByCode(param.getCode(), appId);
        if (existsParams == null) {
            existsParams = new ArrayList<>(0);
        }
        // 判断重复的范围在paramType内，且本次更新的ID不算重复
        existsParams = existsParams.stream().filter(dto -> !param.getId().equals(dto.getId()) && param.getParamType().equals(dto.getParamType())).collect(Collectors.toList());
        // 重复判断
        if (CollectionUtils.isNotEmpty(existsParams)) {
            throw new CommonException("已有code相同的对象 " + JsonUtils.toJson(existsParams));
        }

        // 插入记录
        ParamSimpleDTO paramSimpleDTO = BeanConvertUtils.convert(param, ParamSimpleDTO.class);
        paramSimpleDTO.setAppId(appId);

        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
        paramSimpleDTO.setUpdater(currentUser);
        try {
            return paramService.updateParam(paramSimpleDTO);
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("修改后的参数已存在，修改失败");
        }
    }


    public Integer updateObjBusinessPrivateParam(ParamUpdateParam param) {
        // 验证参数
        Preconditions.checkArgument(null != param, "参数不能为空");
        Preconditions.checkArgument(null != param.getParamType(), "参数类型不能为空");
        Preconditions.checkArgument(null != param.getId(), "参数主键标识ID不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(param.getCode()), "参数ID不能为空");
        Preconditions.checkArgument(null != param.getParamType(), "参数类型不能为空");
        Preconditions.checkArgument(null != param.getValueType(), "参数值类型不能为空");

        if (forbiddenParamCodes.contains(param.getCode())) {
            throw new CommonException("参数名" + param.getCode() + "已被禁止使用");
        }

        // 验证当前参数是否已存在
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");

        ParamSimpleDTO existsParam = paramService.getParamById(param.getId());
        Preconditions.checkArgument(null != existsParam, "该参数不存在，修改失败");
        Preconditions.checkArgument(appId.equals(existsParam.getAppId()), "未指定产品信息或该参数不在该产品下，修改失败");

        // 验证当前参数code是否有重复
        List<ParamSimpleDTO> existsParamWithSameCode = paramService
                .getParamByCode(param.getCode(), appId);
        if (CollectionUtils.isNotEmpty(existsParamWithSameCode)) {
            for (ParamSimpleDTO paramSimpleDTO : existsParamWithSameCode) {
                if (!param.getId().equals(paramSimpleDTO.getId())) {
                    if (param.getParamType().equals(ParamTypeEnum.OBJ_BUSINESS_PRIVATE_PARAM.getType())) {
                        throw new ParamException(param.getCode() + "已存在");
                    } else if (paramSimpleDTO.getName().equalsIgnoreCase(param.getName())) {
                        throw new ParamException(param.getName() + "已存在");
                    }
                }
            }
        }

        // 更新记录
        ParamSimpleDTO paramSimpleDTO = BeanConvertUtils.convert(param, ParamSimpleDTO.class);
        paramSimpleDTO.setAppId(appId);

        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
        paramSimpleDTO.setUpdater(currentUser);
        try {
            return paramService.updateParam(paramSimpleDTO);
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("修改后的参数已存在，修改失败");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Integer deleteParam(Long paramId) {
        Preconditions.checkArgument(null != paramId, "参数ID不能为空");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");

        // 检查该参数是否被引用
        List<ParamBindSimpleDTO> paramBindSimpleDTOS = paramBindService
                .getParamBindByParamId(Collections.singletonList(paramId), null);
        Preconditions.checkArgument(paramBindSimpleDTOS.size() <= 0, "该参数被引用，删除失败");

        // 先删参数值，再删参数
        paramValueService.deleteByParamId(paramId);
        return paramService.deleteParam(paramId);
    }

    public List<ParamListItemDTO> listAllParams(String search, List<Integer> paramTypes) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(paramTypes), "参数类型不能为空");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");

        // 查询参数，并按“code-type”作为key形成map
        List<ParamSimpleDTO> paramSimpleDTOS = paramService
                .searchParam(search, paramTypes, null, null, null, appId, null, null, null, null, null);
        Map<String, List<ParamSimpleDTO>> paramSimpleDTOMap = Maps.newHashMap();
        for (ParamSimpleDTO paramSimpleDTO : paramSimpleDTOS) {
            String key = paramSimpleDTO.getCode() + "-" + paramSimpleDTO.getParamType();
            List<ParamSimpleDTO> curLists = paramSimpleDTOMap
                    .computeIfAbsent(key, k -> Lists.newLinkedList());
            curLists.add(paramSimpleDTO);
        }

        // 整理最终结果
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
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(aggreTypes), "聚合选项不能为空");
        Preconditions.checkArgument(null != paramType, "参数类型不能为空");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");

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
        // 验证参数
        Preconditions.checkArgument(null != pagingSortDTO, "分页不能为空");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");

        // 获取大小
        List<Integer> paramTypes = Lists.newArrayList();
        if (paramType != null) {
            paramTypes.add(paramType);
        }
        Integer totalNum = paramService
                .searchParamSize(search, paramTypes, createEmails, valueTypes, null, appId, null);
        // 获取分页明细
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
        // 验证参数
        Preconditions.checkArgument(null != paramId, "参数ID不能为空");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");

        // 获取数据
        ParamSimpleDTO paramSimpleDTO = paramService.getParamById(paramId);
        Preconditions.checkArgument(null != paramSimpleDTO, "参数不存在");

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
        Preconditions.checkArgument(null != appId, "未指定产品信息");

        return paramPoolService.searchParamPoolItem(search, appId, null, null, null, null);
    }

    /**
     * 列出需求池，并组装PARAM详情
     * @param search
     * @return
     */
    public List<ParamPoolItemVO> listParamPoolItemWithParam(String search) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");

        // 1. 列出基本列表
        List<ParamPoolItemDTO> paramPoolItems = paramPoolService.searchParamPoolItem(search, appId, null, null, null, null);
        if (CollectionUtils.isEmpty(paramPoolItems)) {
            return new ArrayList<>(0);
        }
        List<ParamPoolItemVO> result = paramPoolItems.stream()
                .filter(Objects::nonNull)
                .map(dto -> BeanConvertUtils.convert(dto, ParamPoolItemVO.class))
                .collect(Collectors.toList());


        // 2. 组装详情
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
        Preconditions.checkArgument(null != param, "参数池中元素信息不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(param.getCode()), "参数池元素的code不能为空");

        if (forbiddenParamCodes.contains(param.getCode())) {
            throw new CommonException("参数名" + param.getCode() + "已被禁止使用");
        }

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");
        AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
        Preconditions.checkArgument(null != appSimpleDTO, "该产品不存在");

        ParamPoolItemDTO paramPoolItemDTO = BeanConvertUtils.convert(param, ParamPoolItemDTO.class);
        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
        paramPoolItemDTO.setCreator(currentUser);
        paramPoolItemDTO.setUpdater(currentUser);
        paramPoolItemDTO.setAppId(appId);

        // 检查参数表中是否已有此code
        List<ParamSimpleDTO> paramsOfCode = paramService
                .getParamByCode(param.getCode(), appId);
        if (!CollectionUtils.isEmpty(paramsOfCode)) {
            for (ParamSimpleDTO paramSimpleDTO : paramsOfCode) {
                if (paramSimpleDTO.getParamType().equals(param.getParamType())) {
                    throw new ParamException("该参数已在当前参数类型下存在");
                }
            }
        }

        // 检查参数池（对象业务私参）候选中是否有此参数
        Integer existsParamPoolItemSize = paramPoolService
                .getParamPoolItemSizeByCode(param.getCode(), appId);
        Preconditions.checkArgument(existsParamPoolItemSize <= 0, param.getCode() + "已存在");

        try {
            return paramPoolService.createParamPoolItem(paramPoolItemDTO);
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("该参数已存在，创建失败");
        }
    }

    public Integer updateParamPoolItem(ParamPoolUpdateParam param) {
        Preconditions.checkArgument(null != param, "参数池中元素信息不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(param.getCode()), "参数池元素的code不能为空");
        Preconditions.checkArgument(null != param.getId(), "参数池元素的主键ID不能为空");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");
        ParamPoolItemDTO existsParamPoolItem = paramPoolService.getParamPoolItemById(param.getId());
        Preconditions.checkArgument(null != existsParamPoolItem, "参数池中元素不存在");
        Preconditions.checkArgument(appId == existsParamPoolItem.getAppId(), "未指定产品信息或该参数不在该产品下，修改失败");

//    // 检查参数表中是否已有此code
//    Integer existsParamSize = paramService
//        .getParamSizeByCode(param.getCode(), appId);
//    Preconditions.checkArgument(existsParamSize <= 0, param.getCode() + "已存在");

        // 检查参数池（对象业务私参）候选中是否已有其他参数code与之相同
        List<ParamPoolItemDTO> paramPoolItemDTOS = paramPoolService
                .getParamPoolItemByCode(param.getCode(), appId);
        if (CollectionUtils.isNotEmpty(paramPoolItemDTOS)) {
            for (ParamPoolItemDTO paramPoolItem : paramPoolItemDTOS) {
                if (!param.getId().equals(paramPoolItem.getId())) {
                    throw new ParamException(param.getCode() + "已存在");
                }
            }
        }

        ParamPoolItemDTO paramPoolItemDTO = BeanConvertUtils.convert(param, ParamPoolItemDTO.class);
        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
        paramPoolItemDTO.setUpdater(currentUser);

        try {
            // 更新参数中code的值
            paramService.updateCode(appId, ParamTypeEnum.OBJ_BUSINESS_PRIVATE_PARAM.getType(), existsParamPoolItem.getCode(), param.getCode());
            return paramPoolService.updateParamPoolItem(paramPoolItemDTO);
        } catch (DuplicateKeyException e) {
            log.debug("", e);
            throw new DomainException("修改后的参数已存在，修改失败");
        } catch (Exception e) {
            log.debug("", e);
            throw new DomainException("修改异常");
        }
    }

    public Integer deleteParamPoolItem(Long paramPoolId) {
        Preconditions.checkArgument(null != paramPoolId, "参数池中元素信息不能为空");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");

        ParamPoolItemDTO paramPoolItemDTO = paramPoolService.getParamPoolItemById(paramPoolId);

        if (paramPoolItemDTO != null) {
            // 验证当前参数是否已存在
            Integer existsParamSize = paramService.getParamSizeByCode(paramPoolItemDTO.getCode(), appId);
            Preconditions.checkArgument(existsParamSize <= 0, "该参数存在对应中文名，删除失败");

            return paramPoolService.deleteParamPoolItem(paramPoolId);
        }
        return 0;
    }

    public PagingResultDTO<ObjBusinessPrivateParamSimpleDTO> listObjBusinessPrivateParams(
            String code, List<String> createEmails,
            String search, List<Integer> valueTypes,
            PagingSortDTO pagingSortDTO) {
        // 验证参数
        Preconditions.checkArgument(null != pagingSortDTO, "分页不能为空");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");

        Integer paramType = ParamTypeEnum.OBJ_BUSINESS_PRIVATE_PARAM.getType();
        List<Integer> paramTypes = Lists.newArrayList();
        if (paramType != null) {
            paramTypes.add(paramType);
        }

        // 将search应用到对象搜索上：从参数名字中获取对象ID， 在从参数绑定关系中获取对应的参数候选集合
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

        // 将search应用到参数搜索上
        List<Long> candidateParamIdFromParams = paramService.searchParamIdsByName(
                search, paramTypes, createEmails, valueTypes, code, appId, null);
        Set<Long> candidateIds = Sets.newHashSet();
        if (CollectionUtils.isNotEmpty(candidateParamIdFromObjs)) {
            candidateIds.addAll(candidateParamIdFromObjs);
        }
        if (CollectionUtils.isNotEmpty(candidateParamIdFromParams)) {
            candidateIds.addAll(candidateParamIdFromParams);
        }


        // 获取大小: 由于search前面已经应用到关联对象和参数上，结果体现在candidateIds上，故这里不再指定search搜索
        Integer totalNum = paramService.searchParamSize(
                null, paramTypes, createEmails, valueTypes, code, appId, candidateIds);
        // 获取分页明细: 由于search前面已经应用到关联对象和参数上，结果体现在candidateIds上，故这里不再指定search搜索
        List<ParamSimpleDTO> params = paramService.searchParam(
                null, paramTypes, createEmails, valueTypes, code, appId, candidateIds,
                pagingSortDTO.getOrderBy(),
                pagingSortDTO.getOrderRule(),
                pagingSortDTO.getOffset(),
                pagingSortDTO.getPageSize());

        // 整理当前参数关联的对象集合
        List<ObjBusinessPrivateParamSimpleDTO> objBusinessPrivateParamSimpleDTOS = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(params)) {
            // 根据paramId查询对应的对象
            List<Long> paramIds = params.stream().map(ParamSimpleDTO::getId).collect(Collectors.toList());
            List<ParamBindSimpleDTO> paramBindSimpleDTOS = paramBindService
                    .getParamBindByParamId(paramIds, EntityTypeEnum.OBJTRACKER.getType());

            // 获取所有对象的基本详情
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

            // 整理paramId关联的对象ID
            Map<Long, Set<ObjectBasic>> paramId2BasicObjMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(paramBindSimpleDTOS)) {
                for (ParamBindSimpleDTO paramBindSimpleDTO : paramBindSimpleDTOS) {
                    Long paramId = paramBindSimpleDTO.getParamId();
                    Long trackerId = paramBindSimpleDTO.getEntityId(); // 对象埋点ID
                    ObjectBasic objBasic = trackerId2BasicObjMap.get(trackerId);
                    Set<ObjectBasic> tmpObjs = paramId2BasicObjMap
                            .computeIfAbsent(paramId, k -> Sets.newHashSet());
                    tmpObjs.add(objBasic);
                }
            }
            // 整理每个param关联的对象集合
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
        // 验证参数
        Preconditions.checkArgument(null != paramId, "参数ID不能为空");

        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");

        // 获取数据
        ParamSimpleDTO paramSimpleDTO = paramService.getParamById(paramId);
        Preconditions.checkArgument(null != paramSimpleDTO, "参数不存在");

        List<ParamValueSimpleDTO> values = paramValueService
                .getById(paramSimpleDTO.getId(), null);

        // 对应的参数值
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
            throw new CommonException("规则名不可为空");
        }
        if (StringUtils.isBlank(ruleTemplateSimpleDTO.getRule())) {
            throw new CommonException("表达式不可为空");
        }
        List<RuleTemplateSimpleDTO> allRuleTemplate = ruleTemplateService.getAllRuleTemplate();
        if (CollectionUtils.isNotEmpty(allRuleTemplate)) {
            allRuleTemplate.forEach(ruleTemplate -> {
                if (ruleTemplate.getName().equals(ruleTemplateSimpleDTO.getName())) {
                    throw new CommonException("规则名重复：" + ruleTemplateSimpleDTO.getName());
                }
            });
        }
        UserDTO userDTO = EtContext.get(ContextConstant.USER);
        if (userDTO == null) {
            throw new CommonException("获取当前用户失败");
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
     * todo 交付时删除
     * 清洗数据
     *
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void syncParamBindDescription(){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");
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
