package com.netease.hz.bdms.easyinsight.service.helper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.obj.param.ParamWithValueItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamValueSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindValueSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.EntityTypeEnum;
import com.netease.hz.bdms.easyinsight.common.exception.ParamBindException;
import com.netease.hz.bdms.easyinsight.common.obj.FourTuple;
import com.netease.hz.bdms.easyinsight.common.param.param.paramBind.ParamBindItermParam;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.service.service.ParamBindService;
import com.netease.hz.bdms.easyinsight.service.service.ParamBindValueService;
import com.netease.hz.bdms.easyinsight.service.service.ParamService;
import com.netease.hz.bdms.easyinsight.service.service.ParamValueService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ParamBindHelper {

    @Autowired
    private ParamBindService paramBindService;
    @Autowired
    private ParamBindValueService paramBindValueService;
    @Autowired
    private ParamService paramService;
    @Autowired
    private ParamValueService paramValueService;

    /**
     * 新增参数绑定
     *
     * @param paramBindItems 参数绑定信息，非必填参数
     * @param appId          产品ID，必填参数
     * @param entityId       关联ID，必填参数
     * @param entityType     关联元素类型，必填参数
     * @param versionId      版本ID，非必填参数
     * @param creator        创建者，必填参数
     * @param updater        最近更新人，必填参数
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void createParamBind(List<ParamBindItermParam> paramBindItems, Long appId, Long entityId,
                                Integer entityType, Long versionId, UserSimpleDTO creator, UserSimpleDTO updater) {
        Preconditions.checkArgument(null != appId, "产品ID不能为空");
        Preconditions.checkArgument(null != entityId && null != entityType, "关联元素不能为空");
        Preconditions.checkArgument(null != creator, "创建人信息不能为空");
        Preconditions.checkArgument(null != updater, "最近更新人信息不能为空");

        // 检查参数绑定
        Set<Long> paramIds = paramBindItems.stream().map(ParamBindItermParam::getParamId).collect(
                Collectors.toSet());
        List<ParamSimpleDTO> paramSimpleDTOS = paramService.getParamByIds(paramIds);
        Set<Integer> paramTypes = paramSimpleDTOS.stream()
                .map(ParamSimpleDTO::getParamType)
                .collect(Collectors.toSet());

        EntityTypeEnum entityTypeEnum = EntityTypeEnum.fromType(entityType);
        Boolean checkLegal = entityTypeEnum.checkLegalParamType(paramTypes);
        Preconditions.checkArgument(checkLegal, "存在不允许绑定的参数类型，绑定失败");

        if (CollectionUtils.isNotEmpty(paramBindItems)) {
            // 检查paramId是否合并
            Set<Long> curParamIds = Sets.newHashSet();
            for (ParamBindItermParam paramBindItem : paramBindItems) {
                Long paramId = paramBindItem.getParamId();
                if (curParamIds.contains(paramId)) {
                    log.warn("参数绑定,参数重复：entityId={},entityType={},paramBind={},creator={},updater={}",
                            entityId, entityType, paramBindItem, creator, updater);
                    throw new ParamBindException("参数配置，存在两行重复的参数");
                } else {
                    curParamIds.add(paramId);
                }
            }

            for (ParamBindItermParam paramBindItem : paramBindItems) {
                ParamBindSimpleDTO paramBindSimpleDTO = BeanConvertUtils
                        .convert(paramBindItem, ParamBindSimpleDTO.class);
                if (paramBindSimpleDTO == null) {
                    continue;
                }
                // 将绑定的参数写入eis_param_bind
                paramBindSimpleDTO.setAppId(appId)
                        .setEntityId(entityId)
                        .setEntityType(entityType)
                        .setAppId(appId)
                        .setVersionId(versionId)
                        .setCreator(creator)
                        .setUpdater(updater);
                Long bindId = paramBindService.createParamBind(paramBindSimpleDTO);

                // 将绑定的参数值写入eis_param_bind_value
                List<ParamBindValueSimpleDTO> paramBindValueDTOS = Lists.newArrayList();
                if (CollectionUtils.isNotEmpty(paramBindItem.getValues())) {
                    for (Long paramValueId : paramBindItem.getValues()) {
                        ParamBindValueSimpleDTO paramBindValue = new ParamBindValueSimpleDTO();
                        paramBindValue.setAppId(appId)
                                .setBindId(bindId)
                                .setParamValueId(paramValueId)
                                .setCreator(creator)
                                .setUpdater(updater);
                        paramBindValueDTOS.add(paramBindValue);
                    }
                }
                paramBindValueService.createParamBindValue(paramBindValueDTOS);
            }
        }
    }


    /**
     * 删除参数绑定
     *
     * @param appId      产品ID，必填参数
     * @param entityIds  关联ID集合，必填参数
     * @param entityType 关联元素类型，必填参数
     * @param versionId  版本ID，非必填参数
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteParamBinds(Long appId, Collection<Long> entityIds, Integer entityType,
                                 Long versionId) {
        Preconditions.checkArgument(null != appId, "产品ID不能为空");
        if (CollectionUtils.isNotEmpty(entityIds) && null != entityType) {
            List<Long> bindSimpleIds = paramBindService
                    .getParamBindIdsByEntityIds(entityIds, entityType, versionId, appId);

            if (CollectionUtils.isNotEmpty(bindSimpleIds)) {
                paramBindValueService.deleteByBindIds(bindSimpleIds);
                paramBindService.deleteByIds(Sets.newHashSet(bindSimpleIds));
            }
        }
    }

    /**
     * 查询参数绑定
     *
     * @param appId      产品ID，必填参数
     * @param entityId   关联ID，必填参数
     * @param entityType 关联元素类型，必填参数
     * @param versionId  版本ID，非必填参数
     */
    public List<ParamBindItemDTO> getParamBinds(Long appId, Long entityId, Integer entityType, Long versionId) {
        Preconditions.checkArgument(null != appId, "产品ID不能为空");
        Preconditions.checkArgument(null != entityId && null != entityType, "关联元素不能为空");
        // 获取参数绑定集合
        List<ParamBindSimpleDTO> paramBindSimpleDTOS = paramBindService.getByEntityIds(
                Collections.singleton(entityId), Collections.singleton(entityType), versionId, appId);

        List<ParamBindItemDTO> result = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(paramBindSimpleDTOS)) {
            // 从参数绑定集合中获取参数绑定ID，参数ID
            Set<Long> paramBindIds = Sets.newHashSet();
            Set<Long> paramIds = Sets.newHashSet();
            for (ParamBindSimpleDTO paramBindSimpleDTO : paramBindSimpleDTOS) {
                paramBindIds.add(paramBindSimpleDTO.getId());
                paramIds.add(paramBindSimpleDTO.getParamId());
            }

            // 参数绑定值的map: key表示绑定ID， value表示参数绑定值对象集合
            List<ParamBindValueSimpleDTO> paramBindValues = paramBindValueService
                    .getByBindIds(paramBindIds);
            Map<Long, List<ParamBindValueSimpleDTO>> paramBindValueMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(paramBindValues)) {
                for (ParamBindValueSimpleDTO paramBindValue : paramBindValues) {
                    List<ParamBindValueSimpleDTO> paramBindLists = paramBindValueMap
                            .computeIfAbsent(paramBindValue.getBindId(), k -> Lists.newArrayList());
                    paramBindLists.add(paramBindValue);
                }
            }

            // 参数的map: key表示参数ID，value表示参数对象
            List<ParamSimpleDTO> params = paramService.getParamByIds(paramIds);
            Map<Long, ParamSimpleDTO> paramMap = params.stream()
                    .collect(Collectors.toMap(ParamSimpleDTO::getId, Function.identity(), (v1, v2) -> v1));

            // 参数值的map: key表示参数ID， value表示参数值对象列表
            List<ParamValueSimpleDTO> paramValues = paramValueService.getByParamIds(paramIds);
            Map<Long, List<ParamValueSimpleDTO>> paramValueMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(paramValues)) {
                for (ParamValueSimpleDTO paramValue : paramValues) {
                    // 将paramValue按paramId分类加入到paramValueMap中
                    List<ParamValueSimpleDTO> paramValueLists = paramValueMap
                            .computeIfAbsent(paramValue.getParamId(), k -> Lists.newArrayList());
                    paramValueLists.add(paramValue);
                }
            }

            // 对参数绑定集合进行遍历，组合形成ParamBindItemDTO结构：
            // 从ParamSimpleDTO（来源paramMap）中构成主体结构：
            // 将ParamBindSimpleDTO补充到其中，将ParamValueSimpleDTO（来源paramBindValueMap）补充values部分，
            // 用ParamBindValueSimpleDTO补充selectedValues(来源paramBindValueMap)部分
            for (ParamBindSimpleDTO paramBindSimpleDTO : paramBindSimpleDTOS) {
                Long paramId = paramBindSimpleDTO.getParamId();
                Long paramBindId = paramBindSimpleDTO.getId();

                ParamSimpleDTO paramSimpleDTO = paramMap.get(paramId);
                if (paramSimpleDTO != null) {
                    ParamBindItemDTO itemDTO = BeanConvertUtils
                            .convert(paramSimpleDTO, ParamBindItemDTO.class);
                    if (itemDTO != null) {
                        List<ParamValueSimpleDTO> values = paramValueMap.get(paramId);
                        List<ParamBindValueSimpleDTO> bindParamValues = paramBindValueMap.get(paramBindId);
                        List<Long> paramValueIds = new ArrayList<>(0);
                        if (CollectionUtils.isNotEmpty(bindParamValues)) {
                            paramValueIds = bindParamValues.stream().sorted(Comparator.comparingLong(ParamBindValueSimpleDTO::getId))
                                    .map(ParamBindValueSimpleDTO::getParamValueId).collect(Collectors.toList());
                        }

                        itemDTO.setValues(values == null ? Lists.newArrayList() : values)
                                .setSource(paramBindSimpleDTO.getSource())
                                .setSourceDetail(paramBindSimpleDTO.getSourceDetail())
                                .setMust(paramBindSimpleDTO.getMust())
                                .setNotEmpty(paramBindSimpleDTO.getNotEmpty())
                                .setNeedTest(paramBindSimpleDTO.getNeedTest())
                                .setBindId(paramBindId)
                                .setDescription(paramBindSimpleDTO.getDescription())
                                .setIsEncode(paramBindSimpleDTO.getIsEncode())  // v1.4.4 添加
                                .setSelectedValues(paramValueIds);

                        result.add(itemDTO);
                    }
                } else {
                    log.warn("参数异常：paramBind={},appId={},entityId={},entityType={},versionId={}",
                            paramBindSimpleDTO, appId, entityId, entityType, versionId);
                    throw new ParamBindException("paramId=" + paramId + "参数不存在，数据异常");
                }
            }
        }
        return result;
    }

    /**
     * 构建不同产品下，所有实体上绑定的参数及其取值信息
     * @return <(appId, entityId, entityType, versionId), List<ParamBindItemDTO>>映射
     */
    public Map<FourTuple<Long, Long, Integer, Long>, List<ParamBindItemDTO>> getAllParamBindInfo() {
        // 1. 读取表`eis_param_bind`全部内容
        List<ParamBindSimpleDTO> paramBindSimpleDTOS = paramBindService.getByAppId(null);
        // 获取不同产品下所有的paramId,用于后续的批量读取
        Set<Long> paramIdSet = paramBindSimpleDTOS.stream()
                .map(ParamBindSimpleDTO::getParamId)
                .collect(Collectors.toSet());
        // 获取不同产品下所有bindId,用于后续的批量读取
        Set<Long> bindIdList = paramBindSimpleDTOS.stream()
                .map(ParamBindSimpleDTO::getId)
                .collect(Collectors.toSet());
        // 2. 依据不同产品下所有的paramId, 读取表`eis_param`获取相应的信息，并构建<paramId, ParamSimpleDTO>映射
        List<ParamSimpleDTO> paramSimpleDTOList = paramService.getParamByIds(paramIdSet);
        Map<Long, ParamSimpleDTO> paramIdToParamSimpleDTOMap = paramSimpleDTOList.stream()
                .collect(Collectors.toMap(paramSimpleDTO -> paramSimpleDTO.getId(),
                                          paramSimpleDTO -> paramSimpleDTO));

        // 3. 依据不同产品下所有的bindId, 读取表`eis_param_bind_value`获取相应信息，构建<bindId, [paramValueId_1, ...]>映射
        List<ParamBindValueSimpleDTO> paramBindValueSimpleDTOS = paramBindValueService.getByBindIds(bindIdList);
        Map<Long, List<Long>> bindIdToParamValueIdsMap = new HashMap<>();
        for (ParamBindValueSimpleDTO paramBindValueSimpleDTO : paramBindValueSimpleDTOS) {
            Long currBindId = paramBindValueSimpleDTO.getBindId();
            Long currParamValueId = paramBindValueSimpleDTO.getParamValueId();

            List<Long> paramValueIdList = bindIdToParamValueIdsMap.computeIfAbsent(currBindId, k -> new ArrayList<>());
            paramValueIdList.add(currParamValueId);
        }
        // 4. 读取表`eis_param_value`表，获取对应的取值信息，构建<paramValueId, ParamValueSimpleDTO>映射
        Set<Long> paramValueIds = paramBindValueSimpleDTOS.stream()
                .map(ParamBindValueSimpleDTO::getParamValueId)
                .collect(Collectors.toSet());
        List<ParamValueSimpleDTO> paramValueSimpleDTOList =
                paramValueService.getByIds(paramValueIds);
        Map<Long, ParamValueSimpleDTO> paramValueIdToParamValueSimpleDTOMap = paramValueSimpleDTOList.stream()
                .collect(Collectors.toMap(paramValueSimpleDTO -> paramValueSimpleDTO.getId(),
                                          paramValueSimpleDTO -> paramValueSimpleDTO));  //id保证唯一性，无需考虑重复key问题

        // 5. 构建<(appId, entityId, entityType, versionId), List<ParamBindItemDTO>>映射
        Map<FourTuple<Long, Long, Integer, Long>, List<ParamBindItemDTO>> result = new HashMap<>();
        for (ParamBindSimpleDTO paramBindSimpleDTO : paramBindSimpleDTOS) {
            Long currEntityId = paramBindSimpleDTO.getEntityId();
            Integer currEntityType = paramBindSimpleDTO.getEntityType();
            Long currVersionId = paramBindSimpleDTO.getVersionId();
            Long currAppId = paramBindSimpleDTO.getAppId();
            FourTuple<Long, Long, Integer, Long> key =
                    new FourTuple<>(currAppId, currEntityId, currEntityType, currVersionId);
            // 获取当前参数的基本信息
            Long currParamId = paramBindSimpleDTO.getParamId();
            ParamSimpleDTO paramSimpleDTO = paramIdToParamSimpleDTOMap.get(currParamId);
            // 获取当前参数的取值信息
            Long currParamBindId = paramBindSimpleDTO.getId();
            // /* 注意，一个实体可能绑定了某个参数，但是该参数并没有关于值约束信息，因此需使用getOrDefault() */
            List<Long> currParamValueIds = bindIdToParamValueIdsMap.getOrDefault(currParamBindId, new ArrayList<>());
            List<ParamValueSimpleDTO> currParamValueSimpleDTOS = currParamValueIds.stream()
                    .map(paramValueIdToParamValueSimpleDTOMap::get)
                    .collect(Collectors.toList());
            // 根据以上信息，构建 ParamBindItemDTO
            ParamBindItemDTO paramBindItemDTO = BeanConvertUtils.convert(paramSimpleDTO, ParamBindItemDTO.class);
            if (paramBindItemDTO != null) {
                paramBindItemDTO.setDescription(paramBindSimpleDTO.getDescription()); // 替换为参数绑定的描述
                paramBindItemDTO.setValues(currParamValueSimpleDTOS)
                        .setMust(paramBindSimpleDTO.getMust())
                        .setNotEmpty(paramBindSimpleDTO.getNotEmpty())
                        .setNeedTest(paramBindSimpleDTO.getNeedTest())
                        .setBindId(currParamBindId)
                        .setSelectedValues(currParamValueIds);
                result.computeIfAbsent(key, k -> new ArrayList<>()).add(paramBindItemDTO);
            }
        }
        return result;
    }

    @Deprecated  // v2.0.0重写
    public List<ParamWithValueItemDTO> getParamBindWithValue(Long appId, Collection<Long> entityIds,
                                                             Collection<Integer> entityTypes,
                                                             Long versionId) {
        Preconditions.checkArgument(null != appId, "产品ID不能为空");
        Preconditions.checkArgument(
                CollectionUtils.isNotEmpty(entityIds) && CollectionUtils.isNotEmpty(entityTypes),
                "关联元素不能为空");
        // 获取参数绑定集合
        List<ParamBindSimpleDTO> paramBindSimpleDTOS = paramBindService
                .getByEntityIds(entityIds, entityTypes, versionId, appId);

        List<ParamWithValueItemDTO> result = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(paramBindSimpleDTOS)) {
            // 从参数绑定集合中获取参数绑定ID，参数ID
            Set<Long> paramBindIds = Sets.newHashSet();
            Set<Long> paramIds = Sets.newHashSet();
            Map<Long, List<ParamBindSimpleDTO>> entityId2ParamBind = Maps.newHashMap();
            for (ParamBindSimpleDTO paramBindSimpleDTO : paramBindSimpleDTOS) {
                paramBindIds.add(paramBindSimpleDTO.getId());
                paramIds.add(paramBindSimpleDTO.getParamId());

                List<ParamBindSimpleDTO> tmpParamBinds = entityId2ParamBind
                        .computeIfAbsent(paramBindSimpleDTO.getEntityId(), k -> Lists.newArrayList());
                tmpParamBinds.add(paramBindSimpleDTO);
            }

            // 参数绑定值的map: key表示绑定ID， value表示参数绑定值对象集合
            List<ParamBindValueSimpleDTO> paramBindValues = paramBindValueService
                    .getByBindIds(paramBindIds);
            Map<Long, List<ParamBindValueSimpleDTO>> bindId2ParamBindMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(paramBindValues)) {
                for (ParamBindValueSimpleDTO paramBindValue : paramBindValues) {
                    List<ParamBindValueSimpleDTO> paramBindLists = bindId2ParamBindMap
                            .computeIfAbsent(paramBindValue.getBindId(), k -> Lists.newArrayList());
                    paramBindLists.add(paramBindValue);
                }
            }
            // 参数的map: key表示参数ID，value表示参数对象
            List<ParamSimpleDTO> params = paramService.getParamByIds(paramIds);
            Map<Long, ParamSimpleDTO> paramId2ParamMap = params.stream()
                    .collect(Collectors.toMap(ParamSimpleDTO::getId, Function.identity(), (v1, v2) -> v1));

            // 参数值Map: key表示参数值ID，value表示参数值对象
            List<ParamValueSimpleDTO> paramValues = paramValueService.getByParamIds(paramIds);
            Map<Long, ParamValueSimpleDTO> paramValueId2ValueMap = paramValues.stream()
                    .collect(Collectors.toMap(ParamValueSimpleDTO::getId, Function.identity(), (v1, v2) -> v1));

            // 对参数绑定集合进行遍历，组合形成ParamWithValueItemDTO结构：
            // 从ParamSimpleDTO（来源paramMap）中构成主体结构：
            // 将ParamBindSimpleDTO补充到其中，用ParamBindValueSimpleDTO补充values部分
            for (Long entityId : entityId2ParamBind.keySet()) {
                List<ParamBindSimpleDTO> tmpParamBinds = entityId2ParamBind.get(entityId);
                if (CollectionUtils.isNotEmpty(tmpParamBinds)) {
                    for (ParamBindSimpleDTO paramBindSimpleDTO : tmpParamBinds) {
                        Long paramId = paramBindSimpleDTO.getParamId();
                        Long paramBindId = paramBindSimpleDTO.getId();

                        ParamSimpleDTO paramSimpleDTO = paramId2ParamMap.get(paramId);
                        if (paramSimpleDTO != null) {
                            ParamWithValueItemDTO itemDTO = BeanConvertUtils
                                    .convert(paramSimpleDTO, ParamWithValueItemDTO.class);
                            if (itemDTO != null) {

                                List<ParamBindValueSimpleDTO> bindValues = bindId2ParamBindMap.get(paramBindId);
                                List<String> bindValueStrs = Lists.newArrayList();
                                if (CollectionUtils.isNotEmpty(bindValues)) {
                                    for (ParamBindValueSimpleDTO bindValue : bindValues) {
                                        ParamValueSimpleDTO value = paramValueId2ValueMap
                                                .get(bindValue.getParamValueId());
                                        if (value != null) {
                                            bindValueStrs.add(value.getCode());
                                        }
                                    }
                                }

                                itemDTO.setEntityId(paramBindSimpleDTO.getEntityId())
                                        .setEntityType(paramBindSimpleDTO.getEntityType())
                                        .setDescription(paramBindSimpleDTO.getDescription())// 替换为参数绑定的描述
                                        .setValues(bindValueStrs)
                                        .setVersionId(paramBindSimpleDTO.getVersionId())
                                        .setNeedTest(paramBindSimpleDTO.getNeedTest())
                                        .setIsEncode(paramBindSimpleDTO.getIsEncode())
                                        .setMust(paramBindSimpleDTO.getMust())
                                        .setNotEmpty(paramBindSimpleDTO.getNotEmpty());
                                result.add(itemDTO);
                            }
                        } else {
                            log.warn("参数异常：paramBind={},appId={},entityIds={},entityTypes={},versionId={}",
                                    paramBindSimpleDTO, appId, entityIds, entityTypes, versionId);
                            throw new ParamBindException("paramId=" + paramId + "参数不存在，数据异常");
                        }
                    }
                }
            }
        }
        return result;
    }


    /**
     * 查询参数绑定
     *
     * @param appId      产品ID，必填参数
     * @param entityIds  关联ID集合，必填参数
     * @param entityType 关联元素类型，必填参数
     * @param versionId  版本ID，非必填参数
     */
    @Deprecated  // v2.0.0重写
    public Map<Long, List<ParamBindItemDTO>> getParamBinds(Long appId, Collection<Long> entityIds,
                                                           Integer entityType, Long versionId) {
        Preconditions.checkArgument(null != appId, "产品ID不能为空");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(entityIds) && null != entityType,
                "关联元素不能为空");
        // 获取参数绑定集合
        List<ParamBindSimpleDTO> paramBindSimpleDTOS = paramBindService
                .getByEntityIds(entityIds, Collections.singleton(entityType), versionId, appId);

        Map<Long, List<ParamBindItemDTO>> result = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(paramBindSimpleDTOS)) {
            // 从参数绑定集合中获取参数绑定ID，参数ID
            Set<Long> paramBindIds = Sets.newHashSet();
            Set<Long> paramIds = Sets.newHashSet();
            Map<Long, List<ParamBindSimpleDTO>> entityId2ParamBind = Maps.newHashMap();
            for (ParamBindSimpleDTO paramBindSimpleDTO : paramBindSimpleDTOS) {
                paramBindIds.add(paramBindSimpleDTO.getId());
                paramIds.add(paramBindSimpleDTO.getParamId());

                List<ParamBindSimpleDTO> tmpParamBinds = entityId2ParamBind
                        .computeIfAbsent(paramBindSimpleDTO.getEntityId(), k -> Lists.newArrayList());
                tmpParamBinds.add(paramBindSimpleDTO);
            }

            // 参数绑定值的map: key表示绑定ID， value表示参数绑定值对象集合
            List<ParamBindValueSimpleDTO> paramBindValues = paramBindValueService
                    .getByBindIds(paramBindIds);
            Map<Long, List<ParamBindValueSimpleDTO>> paramBindValueMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(paramBindValues)) {
                for (ParamBindValueSimpleDTO paramBindValue : paramBindValues) {
                    List<ParamBindValueSimpleDTO> paramBindLists = paramBindValueMap
                            .computeIfAbsent(paramBindValue.getBindId(), k -> Lists.newArrayList());
                    paramBindLists.add(paramBindValue);
                }
            }

            // 参数的map: key表示参数ID，value表示参数对象
            List<ParamSimpleDTO> params = paramService.getParamByIds(paramIds);
            Map<Long, ParamSimpleDTO> paramMap = params.stream()
                    .collect(Collectors.toMap(ParamSimpleDTO::getId, Function.identity(), (v1, v2) -> v1));

            // 参数值的map: key表示参数ID， value表示参数值对象列表
            List<ParamValueSimpleDTO> paramValues = paramValueService.getByParamIds(paramIds);
            Map<Long, List<ParamValueSimpleDTO>> paramValueMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(paramValues)) {
                for (ParamValueSimpleDTO paramValue : paramValues) {
                    // 将paramValue按paramId分类加入到paramValueMap中
                    List<ParamValueSimpleDTO> paramValueLists = paramValueMap
                            .computeIfAbsent(paramValue.getParamId(), k -> Lists.newArrayList());
                    paramValueLists.add(paramValue);
                }
            }

            // 对参数绑定集合进行遍历，组合形成ParamBindItemDTO结构：
            // 从ParamSimpleDTO（来源paramMap）中构成主体结构：
            // 将ParamBindSimpleDTO补充到其中，将ParamValueSimpleDTO（来源paramBindValueMap）补充values部分，
            // 用ParamBindValueSimpleDTO补充selectedValues(来源paramBindValueMap)部分
            for (Long entityId : entityId2ParamBind.keySet()) {
                List<ParamBindSimpleDTO> tmpParamBinds = entityId2ParamBind.get(entityId);
                if (CollectionUtils.isNotEmpty(tmpParamBinds)) {
                    List<ParamBindItemDTO> resultParamBinds = Lists.newArrayList();
                    result.put(entityId, resultParamBinds);
                    for (ParamBindSimpleDTO paramBindSimpleDTO : tmpParamBinds) {
                        Long paramId = paramBindSimpleDTO.getParamId();
                        Long paramBindId = paramBindSimpleDTO.getId();

                        ParamSimpleDTO paramSimpleDTO = paramMap.get(paramId);
                        if (paramSimpleDTO != null) {
                            ParamBindItemDTO itemDTO = BeanConvertUtils
                                    .convert(paramSimpleDTO, ParamBindItemDTO.class);
                            if (itemDTO != null) {
                                itemDTO.setDescription(paramBindSimpleDTO.getDescription());// 替换为参数绑定的描述

                                List<ParamValueSimpleDTO> values = paramValueMap.get(paramId);
                                List<ParamBindValueSimpleDTO> bindParamValues = paramBindValueMap.get(paramBindId);
                                Set<Long> paramValueIds = Sets.newHashSet();
                                if (CollectionUtils.isNotEmpty(bindParamValues)) {
                                    paramValueIds = bindParamValues.stream()
                                            .map(ParamBindValueSimpleDTO::getParamValueId).collect(Collectors.toSet());
                                }

                                itemDTO.setValues(values == null ? Lists.newArrayList() : values)
                                        .setMust(paramBindSimpleDTO.getMust())
                                        .setNotEmpty(paramBindSimpleDTO.getNotEmpty())
                                        .setBindId(paramBindId)
                                        .setIsEncode(paramBindSimpleDTO.getIsEncode())  // v1.4.4 新增
                                        .setSelectedValues(Lists.newArrayList(paramValueIds));

                                resultParamBinds.add(itemDTO);
                            }
                        } else {
                            log.warn("参数异常：paramBind={},appId={},entityIds={},entityType={},versionId={}",
                                    paramBindSimpleDTO, appId, entityIds, entityType, versionId);
                            throw new ParamBindException("paramId=" + paramId + "参数不存在，数据异常");
                        }
                    }
                }
            }
        }
        return result;
    }
}
