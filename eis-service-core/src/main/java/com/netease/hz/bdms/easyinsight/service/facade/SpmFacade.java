package com.netease.hz.bdms.easyinsight.service.facade;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netease.eis.adapters.CacheAdapter;
import com.netease.hz.bdms.easyinsight.common.OpenSource;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.ListHolder;
import com.netease.hz.bdms.easyinsight.common.dto.common.CommonAggregateDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;

import com.netease.hz.bdms.easyinsight.common.dto.spm.*;
import com.netease.hz.bdms.easyinsight.common.dto.tag.ObjTagSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.tag.SpmTagSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.tag.TagSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.*;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.obj.TwoTuple;
import com.netease.hz.bdms.easyinsight.common.param.spm.*;
import com.netease.hz.bdms.easyinsight.common.util.*;
import com.netease.hz.bdms.easyinsight.common.vo.PageResultVO;
import com.netease.hz.bdms.easyinsight.common.vo.spm.SpmMapInfoListQueryVO;
import com.netease.hz.bdms.easyinsight.common.vo.spm.SpmMapInfoVO;
import com.netease.hz.bdms.easyinsight.dao.model.*;
import com.netease.hz.bdms.easyinsight.service.helper.ReqPoolSpmHelper;
import com.netease.hz.bdms.easyinsight.service.helper.SpmMapHelper;

import com.netease.hz.bdms.easyinsight.service.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * 新旧埋点映射功能模块
 *
 * @author xumengqiang
 * @date 2021-12-31 下午 14:43
 */

@Component
@Slf4j
public class SpmFacade {

    @Autowired
    SpmMapHelper spmMapHelper;

    @Autowired
    SpmMapItemService spmMapItemService;

    @Autowired
    SpmTagService spmTagService;

    @Autowired
    SpmInfoService spmInfoService;

    @Autowired
    ReqPoolSpmHelper reqPoolSpmHelper;

    @Autowired
    TagService tagService;

    @Autowired
    ObjTagService objTagService;

    @Autowired
    ArtificialSpmInfoService artificialSpmInfoService;

    @Autowired
    ObjectBasicService objectBasicService;

    @Resource
    private CacheAdapter cacheAdapter;

    private static final String SPM_CACHE_KEY_PREFIX = "spmList_";
    private static final String SPM_CACHE_HAS_RELEASE_KEY_PREFIX = "spmListHasRelease";
    private static final int CACHE_TTL = 600;

    /**
     * 新建/更新 新旧埋点映射
     */
    @Transactional(rollbackFor = Throwable.class)
    public void createSpmMapRelation(SpmMapItemCreateParam spmMapItemCreateParam) {
        // 1. 参数校验
        Long spmId = spmMapItemCreateParam.getSpmId();
        List<String> spmOldList = spmMapItemCreateParam.getSpmOldList();
        Preconditions.checkArgument(null != spmId, "spm不能为空");
        Preconditions.checkArgument(null != spmOldList, "oldSpmList不能为空");

        // 2. 除去oldSpmList中空字符串、重复字符串
        List<String> filterSpmOldList = spmOldList.stream()
                .filter(v -> !Strings.isNullOrEmpty(v))
                .distinct()
                .collect(Collectors.toList());

        // 3. 分别将trackerIdList, spmOldList拆分，构成多条记录
        List<SpmMapItemDTO> spmMapItemDTOS = Lists.newArrayList();
        for (String spmOld : filterSpmOldList) {
            SpmMapItemDTO spmMapItemDTO = new SpmMapItemDTO();
            spmMapItemDTO.setSpmId(spmId)
                    .setSpmOld(spmOld);
            spmMapItemDTOS.add(spmMapItemDTO);
        }

        // 4. 依据trackerId,spm删除已有的映射关系,再批量插入新的映射关系
        spmMapItemService.deleteBySpmId(Collections.singletonList(spmId));
        if (CollectionUtils.isNotEmpty(spmMapItemDTOS)) {
            spmMapItemService.create(spmMapItemDTOS);
        }

        // 5. 处理人工创建的spm

        ArtificialSpmInfoDTO artificialSpmInfoDTO = new ArtificialSpmInfoDTO();
        artificialSpmInfoDTO.setId(spmId);
        artificialSpmInfoDTO.setSpmOldList(String.join(",", filterSpmOldList));
        artificialSpmInfoService.update(Collections.singletonList(artificialSpmInfoDTO));
        tempDisableListCache();
    }

    /**
     * 批量绑定SPM标签
     *
     * @param spmTagBindsParam
     */
    @Transactional(rollbackFor = Throwable.class)
    public void createSpmTagBinding(SpmTagBindsParam spmTagBindsParam) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        // 1. 参数检查
        List<Long> spmIds = spmTagBindsParam.getSpmIds();
        List<Long> tagIds = spmTagBindsParam.getTagIds();
        UserDTO userInfo = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(userInfo, UserSimpleDTO.class);
        Preconditions.checkArgument(null != currentUser, "当前用户信息不能为空");
        if (CollectionUtils.isEmpty(spmIds)) {
            throw new CommonException("spmId集合不能为空");
        }
        // 若传入的标签集合为 null 或者为空集，统一替换为空集合
        if(CollectionUtils.isEmpty(tagIds)){
            tagIds = Lists.newArrayList();
        }
        // 2. 过滤可能存在的重复值
        spmIds = spmIds.stream().distinct().collect(Collectors.toList());
        tagIds = tagIds.stream().distinct().collect(Collectors.toList());

        // 3. 封装数据
        List<SpmTagSimpleDTO> spmTagSimpleDTOS = Lists.newArrayList();

        //查询人工添加的spm
        SpmInfo query = new SpmInfo();
        query.setSource(SpmSourceTypeEnum.AITIFICIAL.getStatus());
        List<SpmInfoDTO> spmList = spmInfoService.search(appId, query);
        query.setSource(SpmSourceTypeEnum.POPVOLE.getStatus());
        List<SpmInfoDTO> spmList1 = spmInfoService.search(appId, query);
        spmList.addAll(spmList1);
        List<Long> finalSpmIds = spmIds;
        spmList = spmList.stream().filter(spmInfoDTO -> finalSpmIds.contains(spmInfoDTO.getId())).collect(Collectors.toList());
        //查询标签
//        List<Long> artificialSpmIds = spmList.stream().map(SpmInfoDTO::getId).collect(Collectors.toList());
        List<TagSimpleDTO> tagSimpleDTOS = tagService.getByIds(tagIds);
        Map<String, String> spmTagMap = new HashMap<>();
        for(TagSimpleDTO tagSimpleDTO : tagSimpleDTOS){
            spmTagMap.put(String.valueOf(tagSimpleDTO.getId()), tagSimpleDTO.getName());
        }

        List<ArtificialSpmInfoDTO> artificialSpmInfoDTOS = new ArrayList<>();
        for(SpmInfoDTO spmInfoDTO : spmList){
            ArtificialSpmInfoDTO artificialSpmInfoDTO = new ArtificialSpmInfoDTO();
            artificialSpmInfoDTO.setId(spmInfoDTO.getId());
            artificialSpmInfoDTO.setSpmTag(JsonUtils.toJson(spmTagMap));
            artificialSpmInfoDTOS.add(artificialSpmInfoDTO);
        }
        artificialSpmInfoService.update(artificialSpmInfoDTOS);


        for (Long spmId : spmIds) {
            for (Long tagId : tagIds) {
                SpmTagSimpleDTO spmTagSimpleDTO = new SpmTagSimpleDTO();
                spmTagSimpleDTO.setSpmId(spmId);
                spmTagSimpleDTO.setTagId(tagId);
                spmTagSimpleDTO.setCreator(currentUser);
                spmTagSimpleDTO.setUpdater(currentUser);
                // 记录加入集合
                spmTagSimpleDTOS.add(spmTagSimpleDTO);
            }
        }

        // 4. 先批量删除，后批量添加
        spmTagService.deleteBySpmId(spmIds);
        if (CollectionUtils.isNotEmpty(spmTagSimpleDTOS)) {
            spmTagService.create(spmTagSimpleDTOS);
        }
        tempDisableListCache();
    }

    /**
     * 批量更新 映射状态
     */
    public void updateSpmMapStatus(SpmMapStatusUpdateParam spmMapStatusUpdateParam) {
        // 1. 参数检查
        List<Long> spmIds = spmMapStatusUpdateParam.getSpmIds();
        Integer status = spmMapStatusUpdateParam.getStatus();
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId,"未指定应用产品信息");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(spmIds), "spmId集合不能为空");
        Preconditions.checkArgument(SpmMapStatusEnum.containsStatus(status), "不支持的映射状态");

        if (SpmMapStatusEnum.DOUBLE_PRETEST.getStatus().equals(status)) {
            // 映射状态改为双打预发时需要版本为必填
            for (Long spmId : spmIds) {
                SpmInfo query = new SpmInfo();
                query.setId(spmId);
                List<SpmInfoDTO> spmInfoDTOs = spmInfoService.search(appId, query);
                if (CollectionUtils.isEmpty(spmInfoDTOs)) {
                    continue;
                }

                SpmInfoDTO spm = spmInfoDTOs.get(0);
                if (StringUtils.isBlank(spm.getVersion())) {
                    throw new CommonException("映射状态改为双打预发时需要版本为必填 spmId=" + spmId);
                }
            }
            // 映射状态改为双打预发时需要版本为必填
            List<SpmMapItemDTO> allMapItem = spmMapItemService.getBySpmIds(spmIds);
            if (CollectionUtils.isEmpty(allMapItem)) {
                throw new CommonException("映射状态改为双打预发时需要老SPM映射为必填");
            }
            Map<Long, List<SpmMapItemDTO>> collect = allMapItem.stream().collect(Collectors.groupingBy(SpmMapItemDTO::getSpmId));
            for (Long spmId : spmIds) {
                List<SpmMapItemDTO> spmMapItemDTOS = collect.get(spmId);
                if (CollectionUtils.isEmpty(spmMapItemDTOS)) {
                    throw new CommonException("映射状态改为双打预发时需要老SPM映射为必填 spmId=" + spmId);
                }
                long spmOldCount = spmMapItemDTOS.stream().map(SpmMapItemDTO::getSpmOld).filter(StringUtils::isNotBlank).count();
                if (spmOldCount < 1) {
                    throw new CommonException("映射状态改为双打预发时需要老SPM映射为必填 spmId=" + spmId);
                }
            }
        }

        // 2. 封装数据
        List<SpmInfoDTO> spmInfoDTOS = Lists.newArrayList();
        for (Long spmId : spmIds) {
            SpmInfoDTO spmInfoDTO = new SpmInfoDTO();
            spmInfoDTO.setId(spmId);
            spmInfoDTO.setStatus(status);
            // 加入列表
            spmInfoDTOS.add(spmInfoDTO);
        }
        // 3. 更新
        spmInfoService.update(spmInfoDTOS);
        tempDisableListCache();
    }

    /**
     * 批量更新 spm映射生效版本
     */
    public void updateSpmVersion(SpmMapVersionUpdateParam spmMapVersionUpdateParam) {
        // 1. 参数检查
        List<Long> spmIds = spmMapVersionUpdateParam.getSpmIds();
        String version = spmMapVersionUpdateParam.getVersion();
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(spmIds), "spmId集合不能为空");
        // 如果传入的为null或者为空字符串，统一换成空字符串以便插入
        if(StringUtils.isBlank(version)){
            version = "";
        }
        // 2. 封装数据
        List<SpmInfoDTO> spmInfoDTOS = Lists.newArrayList();
        for (Long spmId : spmIds) {
            SpmInfoDTO spmInfoDTO = new SpmInfoDTO();
            spmInfoDTO.setId(spmId);
            spmInfoDTO.setVersion(version);
            // 加入列表
            spmInfoDTOS.add(spmInfoDTO);
        }
        // 3. 更新
        spmInfoService.update(spmInfoDTOS);
        tempDisableListCache();
    }

    /**
     * 更新SPM备注
     */
    public void updateSpmMapNote(SpmMapNoteUpdateParam spmMapNoteUpdateParam) {
        // 1. 参数检查
        Long spmId = spmMapNoteUpdateParam.getSpmId();
        String note = spmMapNoteUpdateParam.getNote();
        // spm备注可为空，无需检查
        Preconditions.checkArgument(null != spmId, "spmId不能为空");
        // 2. 数据封装
        SpmInfoDTO spmInfoDTO = new SpmInfoDTO();
        spmInfoDTO.setId(spmId);
        spmInfoDTO.setNote(note);
        // 3. 更新
        spmInfoService.update(Collections.singletonList(spmInfoDTO));
        tempDisableListCache();
    }

    /**
     * 更新SPM基本信息
     */
    public void createAndupdateSpmInfo(SpmInfoUpdateParam spmInfoUpdateParam) {
        // 1. 参数检查
        Long spmId = spmInfoUpdateParam.getSpmId();
        String name = spmInfoUpdateParam.getName();
        String spm = spmInfoUpdateParam.getSpm();
//        // spm备注可为空，无需检查
//        Preconditions.checkArgument(null != spmId, "spmId不能为空");

        // 2. 数据封装
        SpmInfoDTO spmInfoDTO = new SpmInfoDTO();
        spmInfoDTO.setId(spmId);
        spmInfoDTO.setName(name);
        spmInfoDTO.setSpm(spm);
        spmInfoDTO.setStatus(spmInfoUpdateParam.getStatus());
        spmInfoDTO.setNote(spmInfoUpdateParam.getNote());
        spmInfoDTO.setVersion(spmInfoUpdateParam.getVersion());
        spmInfoDTO.setUpdateTime(new Timestamp(System.currentTimeMillis()));

        ArtificialSpmInfoDTO artificialSpmInfoDTO = new ArtificialSpmInfoDTO();
        artificialSpmInfoDTO.setId(spmId);
        artificialSpmInfoDTO.setName(name);
        artificialSpmInfoDTO.setSpm(spm);
        artificialSpmInfoDTO.setStatus(spmInfoUpdateParam.getStatus());
        artificialSpmInfoDTO.setSpmStatus(spmInfoUpdateParam.getSpmStatus());
        artificialSpmInfoDTO.setSpmTag(spmInfoUpdateParam.getSpmTag());
        artificialSpmInfoDTO.setSpmOldList(spmInfoUpdateParam.getSpmOldList());
        artificialSpmInfoDTO.setNote(spmInfoUpdateParam.getNote());
        artificialSpmInfoDTO.setVersion(spmInfoUpdateParam.getVersion());
        artificialSpmInfoDTO.setUpdateTime(new Timestamp(System.currentTimeMillis()));

        // 3. 更新
        if(spmId == null){
            List<SpmInfoDTO> spmInfoDTOS = spmInfoService.getBySpm(Collections.singletonList(spm), spmInfoUpdateParam.getAppId());
            if(CollectionUtils.isNotEmpty(spmInfoDTOS)){
                for(SpmInfoDTO spmInfo : spmInfoDTOS) {
                    if(spmInfo.getTerminalId().equals(spmInfoUpdateParam.getTerminalId())) {
                        throw new CommonException("该spm已存在！");
                    }
                }
            }
            spmInfoDTO.setSource(SpmSourceTypeEnum.AITIFICIAL.getStatus());
            spmInfoDTO.setAppId(spmInfoUpdateParam.getAppId());
            spmInfoDTO.setCreateTime(new Timestamp(System.currentTimeMillis()));
            spmInfoDTO.setTerminalId(spmInfoUpdateParam.getTerminalId());
            List<Long> spmIds = spmInfoService.create(Collections.singletonList(spmInfoDTO));

            artificialSpmInfoDTO.setSource(SpmSourceTypeEnum.AITIFICIAL.getStatus());
            artificialSpmInfoDTO.setAppId(spmInfoUpdateParam.getAppId());
            artificialSpmInfoDTO.setTerminalId(spmInfoUpdateParam.getTerminalId());
            artificialSpmInfoDTO.setCreateTime(new Timestamp(System.currentTimeMillis()));
            artificialSpmInfoDTO.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            artificialSpmInfoDTO.setId(CollectionUtils.isNotEmpty(spmIds) ? spmIds.get(0) : null);
            artificialSpmInfoService.create(Collections.singletonList(artificialSpmInfoDTO));

        }else {
            artificialSpmInfoService.updateById(artificialSpmInfoDTO);
            spmInfoService.updateById(spmInfoDTO);
        }

        tempDisableListCache();
    }

    public SpmCheckInfoDTO checkSpmInfo(String spmInfo) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        SpmCheckInfoDTO spmCheckInfoDTO = new SpmCheckInfoDTO();

//        List<SpmInfoDTO> spmInfoDTOS = spmInfoService.getBySpm(Collections.singleton(spmInfo), appId);
//        if(CollectionUtils.isNotEmpty(spmInfoDTOS)){
//            for(SpmInfoDTO spmInfoDTO : spmInfoDTOS) {
//                if(spmInfoDTO.getTerminalId().equals(spmInfo.getTerminalId())) {
//                    spmCheckInfoDTO.setCheckResult(-2);
//                    return spmCheckInfoDTO;
//                }
//            }
//
//        }

        List<String> spmInfoList = Arrays.asList(spmInfo.split("\\|"));
        if(CollectionUtils.isEmpty(spmInfoList)){
            spmCheckInfoDTO.setCheckResult(-1);
            return spmCheckInfoDTO;
        }

        StringBuilder spmName = new StringBuilder();
        for(String oid : spmInfoList){
            ObjectBasic objectBasic = objectBasicService.getByOid(appId, oid);
            if(objectBasic == null){
                spmCheckInfoDTO.setCheckResult(0);
                spmCheckInfoDTO.setInValidOid(oid);
                return spmCheckInfoDTO;
            }
            spmName.append(objectBasic.getName());
        }

        spmCheckInfoDTO.setCheckResult(1);
        spmCheckInfoDTO.setSpmName(spmName.toString());
        return spmCheckInfoDTO;

    }

    /**
     * 如果最近有发布，不走缓存，防止不准
     */
    public void tempDisableListCache() {
        cacheAdapter.setWithExpireTime(SPM_CACHE_HAS_RELEASE_KEY_PREFIX, "1", CACHE_TTL);
    }

    public PageResultVO<SpmMapInfoVO> listWithCache(SpmMapInfoListQueryVO param) {

        // 如果最近有发布，不走缓存，防止不准
        if (cacheAdapter.get(SPM_CACHE_HAS_RELEASE_KEY_PREFIX) != null) {
            List<SpmMapInfoVO> spmMapInfoVOS = list(param);
            return new PageResultVO<>(spmMapInfoVOS,
                    param.getPageSize(), param.getPageNum());
        }
        ListHolder listHolder = CacheUtils.getAndSetIfAbsent(() -> SPM_CACHE_KEY_PREFIX + getMD5(param),
                () -> {
                    List<SpmMapInfoVO> list = list(param);
                    if (list == null) {
                        list = new ArrayList<>(0);
                    }
                    return new ListHolder().setList(list.stream().map(JsonUtils::toJson).collect(Collectors.toList()));
                },
                (key) -> cacheAdapter.get(key),
                (key, value) -> cacheAdapter.setWithExpireTime(key, value, CACHE_TTL),
                ListHolder.class);
        List<SpmMapInfoVO> spmMapInfoVOS = listHolder == null ? new ArrayList<>(0)
                : listHolder.getList().stream().map(s -> JsonUtils.parseObject(s, SpmMapInfoVO.class)).collect(Collectors.toList());

        return new PageResultVO<>(spmMapInfoVOS,
                param.getPageSize(), param.getPageNum());
    }

    /**
     * SPM列表展示  // todo 分页展示
     */
    public List<SpmMapInfoVO> list(SpmMapInfoListQueryVO param) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "未指定产品信息");
        // 1. 获取当前产品下 指定端的spm及其信息
        Long terminalId = param.getTerminalId();
        Map<String, EisReqPoolSpm> reqPoolSpmMap = reqPoolSpmHelper.getLatestSpmMap(appId, terminalId);
        Map<String, Boolean> spmToDeployedMap =reqPoolSpmHelper.getSpmIsDeployed(appId, terminalId);
        Map<Long, Integer> spmStatusMap = reqPoolSpmHelper.getReqPoolSpmStatusMap();


        // 2. 读取表`eis_spm_info`中当前产品下的全部信息
        SpmInfo query = new SpmInfo();
        query.setTerminalId(terminalId);
        List<SpmInfoDTO> spmInfoDTOS = spmInfoService.search(appId, query);
        List<SpmInfoDTO> atificialSpmInfoDTOS = spmInfoDTOS.stream()
                .filter(spmInfoDTO -> spmInfoDTO.getSource().equals(SpmSourceTypeEnum.AITIFICIAL.getStatus()) || spmInfoDTO.getSource().equals(SpmSourceTypeEnum.POPVOLE.getStatus()))
                .collect(Collectors.toList());
        Map<String, SpmInfoDTO> spmMapInfoMap = spmInfoDTOS.stream().filter(distinctByKey(SpmInfoDTO::getSpm))
                .collect(Collectors.toMap(SpmInfoDTO::getSpm, Function.identity()));
        // 3. 相关信息查询
        List<Long> spmIds = spmInfoDTOS.stream()
                .distinct()
                .map(SpmInfoDTO::getId)
                .collect(Collectors.toList());
        // 构建 <spmId, [spmOld, ...]> 新旧埋点映射
        Map<Long, List<String>> spmIdToSpmOldListMap = Maps.newHashMap();
        List<SpmMapItemDTO> spmMapItemDTOList = spmMapItemService.getBySpmIds(spmIds);
        for (SpmMapItemDTO spmMapItemDTO : spmMapItemDTOList) {
            Long spmId = spmMapItemDTO.getSpmId();
            String spmOld = spmMapItemDTO.getSpmOld();
            List<String> spmOldList = spmIdToSpmOldListMap
                    .computeIfAbsent(spmId, k -> Lists.newArrayList());
            spmOldList.add(spmOld);
        }
        // 构建 <spmId, [spmTag, ..., objTag]>
        // spm标签映射
        Map<Long, List<CommonAggregateDTO>> spmIdToTagAggregateDTOMap = Maps.newHashMap();
        List<SpmTagSimpleDTO> spmTagSimpleDTOList = spmTagService.getBySpmIds(spmIds);
        TagSimpleDTO tagQuery = new TagSimpleDTO();
        tagQuery.setAppId(appId);
        List<TagSimpleDTO> tagList = tagService.search(tagQuery);
        Map<Long, String> tagIdToNameMap = tagList.stream()
                .collect(Collectors.toMap(TagSimpleDTO::getId, TagSimpleDTO::getName));
        for (SpmTagSimpleDTO spmTagSimpleDTO : spmTagSimpleDTOList) {
            Long spmId = spmTagSimpleDTO.getSpmId();
            Long tagId = spmTagSimpleDTO.getTagId();
            String tagName = tagIdToNameMap.get(tagId);
            List<CommonAggregateDTO> tagAggregateDTOList = spmIdToTagAggregateDTOMap
                    .computeIfAbsent(spmId, k -> Lists.newArrayList());
            CommonAggregateDTO tagAggregateDTO = new CommonAggregateDTO();
            tagAggregateDTO.setKey(tagId.toString())
                    .setValue(tagName);
            tagAggregateDTOList.add(tagAggregateDTO);
        }
        // 对象标签
        Map<Long, List<CommonAggregateDTO>> objTagListMap = Maps.newHashMap();
        Set<Long> objIds = reqPoolSpmMap.values().stream()
                .map(EisReqPoolSpm::getObjId).collect(Collectors.toSet());
        List<ObjTagSimpleDTO> objTagSimpleDTOS = objTagService.getByObjIds(objIds);
        for (ObjTagSimpleDTO objTagSimpleDTO : objTagSimpleDTOS) {
            Long objId = objTagSimpleDTO.getObjId();
            Long tagId = objTagSimpleDTO.getTagId();
            String tagName = tagIdToNameMap.getOrDefault(tagId, "None");

            List<CommonAggregateDTO> objTagAggregateList = objTagListMap
                    .computeIfAbsent(objId, k -> Lists.newArrayList());
            CommonAggregateDTO tagAggregateDTO = new CommonAggregateDTO();
            tagAggregateDTO.setKey(tagId.toString())
                    .setValue(tagName);
            objTagAggregateList.add(tagAggregateDTO);
        }

        // 4. 构建全量的返回信息
        List<SpmMapInfoVO> spmMapInfoVOS = Lists.newArrayList();
        for (String spm : reqPoolSpmMap.keySet()) {
            if (!spmMapInfoMap.containsKey(spm)) {
                log.warn("spm={}未同步更新至表`eis_spm_map_info`中", spm);
                continue;
            }
            EisReqPoolSpm currReqPoolSpm = reqPoolSpmMap.get(spm);
            SpmInfoDTO currSpmInfoDTO = spmMapInfoMap.get(spm);
            // 聚合信息
            Boolean isDeployed = spmToDeployedMap.getOrDefault(spm, false);
            Long spmId = currSpmInfoDTO.getId();
            String spmReverse = String.join("|",
                    Lists.reverse(Arrays.asList(spm.split("\\|"))));
            Long reqPoolEntityId = currReqPoolSpm.getId();
            Integer spmStatus = spmStatusMap.getOrDefault(reqPoolEntityId, 1);
            // 对象标签放入SPM标签上显示
            List<CommonAggregateDTO> tagAggregateDTOList = spmIdToTagAggregateDTOMap
                    .computeIfAbsent(spmId, k -> Lists.newArrayList());
            Long objId = currReqPoolSpm.getObjId();
            List<CommonAggregateDTO> objTagList = objTagListMap.getOrDefault(
                    objId, new ArrayList<>());
            tagAggregateDTOList.addAll(objTagList);


            SpmMapInfoVO spmMapInfoVO = new SpmMapInfoVO();
            spmMapInfoVO.setId(currSpmInfoDTO.getId())
                    .setSpm(currSpmInfoDTO.getSpm())
                    .setSpmReverse(spmReverse)
                    .setName(currSpmInfoDTO.getName())
                    .setVersion(currSpmInfoDTO.getVersion())
                    .setNote(currSpmInfoDTO.getNote())
                    .setMapStatus(currSpmInfoDTO.getStatus())
                    .setSpmStatus(spmStatus)
                    .setIsDeployed(isDeployed)
                    .setSource(currSpmInfoDTO.getSource())
                    .setSpmOldList(spmIdToSpmOldListMap.getOrDefault(spmId, null))
                    .setTags(spmIdToTagAggregateDTOMap.getOrDefault(spmId, null));
            // 加入列表
            spmMapInfoVOS.add(spmMapInfoVO);
        }
        //人工编辑spm
        if(CollectionUtils.isNotEmpty(atificialSpmInfoDTOS)) {
            List<String> spms = atificialSpmInfoDTOS.stream().map(SpmInfoDTO::getSpm).collect(Collectors.toList());
            List<ArtificialSpmInfoDTO> artificialSpmInfos = artificialSpmInfoService.getBySpm(spms, appId);
            Map<TwoTuple<String, Long>, ArtificialSpmInfoDTO> artificialSpmInfoDTOMap = artificialSpmInfos.stream().collect(Collectors.toMap(k -> new TwoTuple<>(k.getSpm(), k.getTerminalId()), Function.identity()));

            for (SpmInfoDTO spmInfoDTO : atificialSpmInfoDTOS) {
                SpmMapInfoVO atificialSpmMapInfoVO = new SpmMapInfoVO();
                String reverse = String.join("|", Lists.reverse(Arrays.asList(spmInfoDTO.getSpm().split("\\|"))));
                ArtificialSpmInfoDTO artificialSpmInfoDTO = artificialSpmInfoDTOMap.get(new TwoTuple<>(spmInfoDTO.getSpm(), spmInfoDTO.getTerminalId()));
                //
                List<String> spmOldList = new ArrayList<>();
                List<CommonAggregateDTO> spmTags = new ArrayList<>();
                if (artificialSpmInfoDTO != null) {
                    if (artificialSpmInfoDTO.getSpmOldList() != null) {
                        spmOldList = Arrays.asList(artificialSpmInfoDTO.getSpmOldList().split(","));
                    }
                    Map<String, String> spmTagMap = JsonUtils.parseMap(artificialSpmInfoDTO.getSpmTag());
                    if (spmTagMap != null) {
                        for (String tagKey : spmTagMap.keySet()) {
                            String tagValue = spmTagMap.get(tagKey);
                            CommonAggregateDTO commonAggregateDTO = new CommonAggregateDTO();
                            commonAggregateDTO.setKey(tagKey);
                            commonAggregateDTO.setValue(tagValue);
                            spmTags.add(commonAggregateDTO);
                        }
                    }
                }


                atificialSpmMapInfoVO.setId(spmInfoDTO.getId())
                        .setSpm(spmInfoDTO.getSpm())
                        .setSpmReverse(reverse)
                        .setName(spmInfoDTO.getName())
                        .setVersion(spmInfoDTO.getVersion())
                        .setNote(spmInfoDTO.getNote())
                        .setMapStatus(spmInfoDTO.getStatus())
                        .setSource(spmInfoDTO.getSource())
                        .setSpmStatus(artificialSpmInfoDTO != null ? artificialSpmInfoDTO.getSpmStatus() : 1)
                        .setIsDeployed(artificialSpmInfoDTO != null && artificialSpmInfoDTO.getSpmStatus().equals(ProcessStatusEnum.ONLINE.getState()))
                        .setSpmOldList(spmOldList)
                        .setTags(spmTags);
                // 加入列表
                spmMapInfoVOS.add(0, atificialSpmMapInfoVO);
            }
        }
        // 5. 根据传入的条件 进行过滤
        // 是否配置映射
        if (null != param.getIsMapped()) {
            spmMapInfoVOS = spmMapInfoVOS.stream()
                    .filter(k -> param.getIsMapped() ? CollectionUtils.isNotEmpty(k.getSpmOldList())
                            : CollectionUtils.isEmpty(k.getSpmOldList()))
                    .collect(Collectors.toList());
        }
        // 映射状态
        if (CollectionUtils.isNotEmpty(param.getMapStatus())) {
            spmMapInfoVOS = spmMapInfoVOS.stream()
                    .filter(k -> param.getMapStatus().contains(k.getMapStatus()))
                    .collect(Collectors.toList());
        }
        // spm状态
        if (null != param.getSpmStatus()) {
            spmMapInfoVOS = spmMapInfoVOS.stream()
                    .filter(k -> param.getSpmStatus().equals(k.getSpmStatus()))
                    .collect(Collectors.toList());
        }
        // spm标签
        if (null != (param.getTagId())) {
            Long tagId = param.getTagId();
            spmMapInfoVOS = spmMapInfoVOS.stream()
                    .filter(k -> {
                        if(CollectionUtils.isEmpty(k.getTags())) {
                            return false;
                        }
                        List<Long> tagIdList = k.getTags().stream()
                                .map(CommonAggregateDTO::getKey)
                                .map(Long::valueOf)
                                .collect(Collectors.toList());
                        return tagIdList.contains(tagId);
                    }).collect(Collectors.toList());
        }
        // spm或名称
        if (StringUtils.isNotBlank(param.getSpmOrName())) {
            String spmOrName = param.getSpmOrName();
            spmMapInfoVOS = spmMapInfoVOS.stream()
                    .filter(k -> k.getSpm().contains(spmOrName) || k.getName().contains(spmOrName))
                    .collect(Collectors.toList());
        }
        // 老spm或映射生效版本
        if (StringUtils.isNotBlank(param.getSpmOldOrMapVersion())) {
            String searchStr = param.getSpmOldOrMapVersion();
            spmMapInfoVOS = spmMapInfoVOS.stream()
                    .filter(k -> (searchStr.equals(k.getVersion()))
                            || (CollectionUtils.isNotEmpty(k.getSpmOldList())
                            && k.getSpmOldList().contains(searchStr)))
                    .collect(Collectors.toList());
        }

        // 6. 排序
        spmMapInfoVOS.sort(Comparator.comparing(SpmMapInfoVO::getSource, Comparator.reverseOrder()).thenComparing(SpmMapInfoVO::getSpmReverse));
        // spmReverse字段仅用于排序，无需向前端展示
        spmMapInfoVOS.forEach(v -> v.setSpmReverse(null));

        return spmMapInfoVOS;
    }

    private static String getMD5(SpmMapInfoListQueryVO param) {
        if (param == null) {
            return "NULL";
        }
        try {
            SpmMapInfoListQueryVO paramCopy = new SpmMapInfoListQueryVO();
            paramCopy.setTerminalId(param.getTerminalId());
            paramCopy.setIsMapped(param.getIsMapped());
            paramCopy.setMapStatus(param.getMapStatus());
            paramCopy.setSpmStatus(param.getSpmStatus());
            paramCopy.setTagId(param.getTagId());
            paramCopy.setSpmOldOrMapVersion(param.getSpmOldOrMapVersion());
            paramCopy.setSpmOrName(param.getSpmOrName());
            // 这里不用分页，这个列表是假分页
            paramCopy.setPageSize(0);
            paramCopy.setPageNum(0);
            return MD5MsgDigest.compute(JsonUtils.toJson(paramCopy));
        } catch (Exception e) {
            throw new RuntimeException("MD5MsgDigest.compute getExtraWhere failed", e);
        }
    }


    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

}
