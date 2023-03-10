package com.netease.hz.bdms.easyinsight.service.facade;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netease.eis.adapters.CacheAdapter;
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
 * ??????????????????????????????
 *
 * @author xumengqiang
 * @date 2021-12-31 ?????? 14:43
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
     * ??????/?????? ??????????????????
     */
    @Transactional(rollbackFor = Throwable.class)
    public void createSpmMapRelation(SpmMapItemCreateParam spmMapItemCreateParam) {
        // 1. ????????????
        Long spmId = spmMapItemCreateParam.getSpmId();
        List<String> spmOldList = spmMapItemCreateParam.getSpmOldList();
        Preconditions.checkArgument(null != spmId, "spm????????????");
        Preconditions.checkArgument(null != spmOldList, "oldSpmList????????????");

        // 2. ??????oldSpmList?????????????????????????????????
        List<String> filterSpmOldList = spmOldList.stream()
                .filter(v -> !Strings.isNullOrEmpty(v))
                .distinct()
                .collect(Collectors.toList());

        // 3. ?????????trackerIdList, spmOldList???????????????????????????
        List<SpmMapItemDTO> spmMapItemDTOS = Lists.newArrayList();
        for (String spmOld : filterSpmOldList) {
            SpmMapItemDTO spmMapItemDTO = new SpmMapItemDTO();
            spmMapItemDTO.setSpmId(spmId)
                    .setSpmOld(spmOld);
            spmMapItemDTOS.add(spmMapItemDTO);
        }

        // 4. ??????trackerId,spm???????????????????????????,?????????????????????????????????
        spmMapItemService.deleteBySpmId(Collections.singletonList(spmId));
        if (CollectionUtils.isNotEmpty(spmMapItemDTOS)) {
            spmMapItemService.create(spmMapItemDTOS);
        }

        // 5. ?????????????????????spm

        ArtificialSpmInfoDTO artificialSpmInfoDTO = new ArtificialSpmInfoDTO();
        artificialSpmInfoDTO.setId(spmId);
        artificialSpmInfoDTO.setSpmOldList(String.join(",", filterSpmOldList));
        artificialSpmInfoService.update(Collections.singletonList(artificialSpmInfoDTO));
        tempDisableListCache();
    }

    /**
     * ????????????SPM??????
     *
     * @param spmTagBindsParam
     */
    @Transactional(rollbackFor = Throwable.class)
    public void createSpmTagBinding(SpmTagBindsParam spmTagBindsParam) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        // 1. ????????????
        List<Long> spmIds = spmTagBindsParam.getSpmIds();
        List<Long> tagIds = spmTagBindsParam.getTagIds();
        UserDTO userInfo = EtContext.get(ContextConstant.USER);
        UserSimpleDTO currentUser = BeanConvertUtils.convert(userInfo, UserSimpleDTO.class);
        Preconditions.checkArgument(null != currentUser, "??????????????????????????????");
        if (CollectionUtils.isEmpty(spmIds)) {
            throw new CommonException("spmId??????????????????");
        }
        // ??????????????????????????? null ??????????????????????????????????????????
        if(CollectionUtils.isEmpty(tagIds)){
            tagIds = Lists.newArrayList();
        }
        // 2. ??????????????????????????????
        spmIds = spmIds.stream().distinct().collect(Collectors.toList());
        tagIds = tagIds.stream().distinct().collect(Collectors.toList());

        // 3. ????????????
        List<SpmTagSimpleDTO> spmTagSimpleDTOS = Lists.newArrayList();

        //?????????????????????spm
        SpmInfo query = new SpmInfo();
        query.setSource(SpmSourceTypeEnum.AITIFICIAL.getStatus());
        List<SpmInfoDTO> spmList = spmInfoService.search(appId, query);
        query.setSource(SpmSourceTypeEnum.POPVOLE.getStatus());
        List<SpmInfoDTO> spmList1 = spmInfoService.search(appId, query);
        spmList.addAll(spmList1);
        List<Long> finalSpmIds = spmIds;
        spmList = spmList.stream().filter(spmInfoDTO -> finalSpmIds.contains(spmInfoDTO.getId())).collect(Collectors.toList());
        //????????????
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
                // ??????????????????
                spmTagSimpleDTOS.add(spmTagSimpleDTO);
            }
        }

        // 4. ?????????????????????????????????
        spmTagService.deleteBySpmId(spmIds);
        if (CollectionUtils.isNotEmpty(spmTagSimpleDTOS)) {
            spmTagService.create(spmTagSimpleDTOS);
        }
        tempDisableListCache();
    }

    /**
     * ???????????? ????????????
     */
    public void updateSpmMapStatus(SpmMapStatusUpdateParam spmMapStatusUpdateParam) {
        // 1. ????????????
        List<Long> spmIds = spmMapStatusUpdateParam.getSpmIds();
        Integer status = spmMapStatusUpdateParam.getStatus();
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId,"???????????????????????????");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(spmIds), "spmId??????????????????");
        Preconditions.checkArgument(SpmMapStatusEnum.containsStatus(status), "????????????????????????");

        if (SpmMapStatusEnum.DOUBLE_PRETEST.getStatus().equals(status)) {
            // ??????????????????????????????????????????????????????
            for (Long spmId : spmIds) {
                SpmInfo query = new SpmInfo();
                query.setId(spmId);
                List<SpmInfoDTO> spmInfoDTOs = spmInfoService.search(appId, query);
                if (CollectionUtils.isEmpty(spmInfoDTOs)) {
                    continue;
                }

                SpmInfoDTO spm = spmInfoDTOs.get(0);
                if (StringUtils.isBlank(spm.getVersion())) {
                    throw new CommonException("?????????????????????????????????????????????????????? spmId=" + spmId);
                }
            }
            // ??????????????????????????????????????????????????????
            List<SpmMapItemDTO> allMapItem = spmMapItemService.getBySpmIds(spmIds);
            if (CollectionUtils.isEmpty(allMapItem)) {
                throw new CommonException("??????????????????????????????????????????SPM???????????????");
            }
            Map<Long, List<SpmMapItemDTO>> collect = allMapItem.stream().collect(Collectors.groupingBy(SpmMapItemDTO::getSpmId));
            for (Long spmId : spmIds) {
                List<SpmMapItemDTO> spmMapItemDTOS = collect.get(spmId);
                if (CollectionUtils.isEmpty(spmMapItemDTOS)) {
                    throw new CommonException("??????????????????????????????????????????SPM??????????????? spmId=" + spmId);
                }
                long spmOldCount = spmMapItemDTOS.stream().map(SpmMapItemDTO::getSpmOld).filter(StringUtils::isNotBlank).count();
                if (spmOldCount < 1) {
                    throw new CommonException("??????????????????????????????????????????SPM??????????????? spmId=" + spmId);
                }
            }
        }

        // 2. ????????????
        List<SpmInfoDTO> spmInfoDTOS = Lists.newArrayList();
        for (Long spmId : spmIds) {
            SpmInfoDTO spmInfoDTO = new SpmInfoDTO();
            spmInfoDTO.setId(spmId);
            spmInfoDTO.setStatus(status);
            // ????????????
            spmInfoDTOS.add(spmInfoDTO);
        }
        // 3. ??????
        spmInfoService.update(spmInfoDTOS);
        tempDisableListCache();
    }

    /**
     * ???????????? spm??????????????????
     */
    public void updateSpmVersion(SpmMapVersionUpdateParam spmMapVersionUpdateParam) {
        // 1. ????????????
        List<Long> spmIds = spmMapVersionUpdateParam.getSpmIds();
        String version = spmMapVersionUpdateParam.getVersion();
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(spmIds), "spmId??????????????????");
        // ??????????????????null????????????????????????????????????????????????????????????
        if(StringUtils.isBlank(version)){
            version = "";
        }
        // 2. ????????????
        List<SpmInfoDTO> spmInfoDTOS = Lists.newArrayList();
        for (Long spmId : spmIds) {
            SpmInfoDTO spmInfoDTO = new SpmInfoDTO();
            spmInfoDTO.setId(spmId);
            spmInfoDTO.setVersion(version);
            // ????????????
            spmInfoDTOS.add(spmInfoDTO);
        }
        // 3. ??????
        spmInfoService.update(spmInfoDTOS);
        tempDisableListCache();
    }

    /**
     * ??????SPM??????
     */
    public void updateSpmMapNote(SpmMapNoteUpdateParam spmMapNoteUpdateParam) {
        // 1. ????????????
        Long spmId = spmMapNoteUpdateParam.getSpmId();
        String note = spmMapNoteUpdateParam.getNote();
        // spm??????????????????????????????
        Preconditions.checkArgument(null != spmId, "spmId????????????");
        // 2. ????????????
        SpmInfoDTO spmInfoDTO = new SpmInfoDTO();
        spmInfoDTO.setId(spmId);
        spmInfoDTO.setNote(note);
        // 3. ??????
        spmInfoService.update(Collections.singletonList(spmInfoDTO));
        tempDisableListCache();
    }

    /**
     * ??????SPM????????????
     */
    public void createAndupdateSpmInfo(SpmInfoUpdateParam spmInfoUpdateParam) {
        // 1. ????????????
        Long spmId = spmInfoUpdateParam.getSpmId();
        String name = spmInfoUpdateParam.getName();
        String spm = spmInfoUpdateParam.getSpm();
//        // spm??????????????????????????????
//        Preconditions.checkArgument(null != spmId, "spmId????????????");

        // 2. ????????????
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

        // 3. ??????
        if(spmId == null){
            List<SpmInfoDTO> spmInfoDTOS = spmInfoService.getBySpm(Collections.singletonList(spm), spmInfoUpdateParam.getAppId());
            if(CollectionUtils.isNotEmpty(spmInfoDTOS)){
                for(SpmInfoDTO spmInfo : spmInfoDTOS) {
                    if(spmInfo.getTerminalId().equals(spmInfoUpdateParam.getTerminalId())) {
                        throw new CommonException("???spm????????????");
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
     * ???????????????????????????????????????????????????
     */
    public void tempDisableListCache() {
        cacheAdapter.setWithExpireTime(SPM_CACHE_HAS_RELEASE_KEY_PREFIX, "1", CACHE_TTL);
    }

    public PageResultVO<SpmMapInfoVO> listWithCache(SpmMapInfoListQueryVO param) {

        // ???????????????????????????????????????????????????
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
     * SPM????????????  // todo ????????????
     */
    public List<SpmMapInfoVO> list(SpmMapInfoListQueryVO param) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != appId, "?????????????????????");
        // 1. ????????????????????? ????????????spm????????????
        Long terminalId = param.getTerminalId();
        Map<String, EisReqPoolSpm> reqPoolSpmMap = reqPoolSpmHelper.getLatestSpmMap(appId, terminalId);
        Map<String, Boolean> spmToDeployedMap =reqPoolSpmHelper.getSpmIsDeployed(appId, terminalId);
        Map<Long, Integer> spmStatusMap = reqPoolSpmHelper.getReqPoolSpmStatusMap();


        // 2. ?????????`eis_spm_info`?????????????????????????????????
        SpmInfo query = new SpmInfo();
        query.setTerminalId(terminalId);
        List<SpmInfoDTO> spmInfoDTOS = spmInfoService.search(appId, query);
        List<SpmInfoDTO> atificialSpmInfoDTOS = spmInfoDTOS.stream()
                .filter(spmInfoDTO -> spmInfoDTO.getSource().equals(SpmSourceTypeEnum.AITIFICIAL.getStatus()) || spmInfoDTO.getSource().equals(SpmSourceTypeEnum.POPVOLE.getStatus()))
                .collect(Collectors.toList());
        Map<String, SpmInfoDTO> spmMapInfoMap = spmInfoDTOS.stream().filter(distinctByKey(SpmInfoDTO::getSpm))
                .collect(Collectors.toMap(SpmInfoDTO::getSpm, Function.identity()));
        // 3. ??????????????????
        List<Long> spmIds = spmInfoDTOS.stream()
                .distinct()
                .map(SpmInfoDTO::getId)
                .collect(Collectors.toList());
        // ?????? <spmId, [spmOld, ...]> ??????????????????
        Map<Long, List<String>> spmIdToSpmOldListMap = Maps.newHashMap();
        List<SpmMapItemDTO> spmMapItemDTOList = spmMapItemService.getBySpmIds(spmIds);
        for (SpmMapItemDTO spmMapItemDTO : spmMapItemDTOList) {
            Long spmId = spmMapItemDTO.getSpmId();
            String spmOld = spmMapItemDTO.getSpmOld();
            List<String> spmOldList = spmIdToSpmOldListMap
                    .computeIfAbsent(spmId, k -> Lists.newArrayList());
            spmOldList.add(spmOld);
        }
        // ?????? <spmId, [spmTag, ..., objTag]>
        // spm????????????
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
        // ????????????
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

        // 4. ???????????????????????????
        List<SpmMapInfoVO> spmMapInfoVOS = Lists.newArrayList();
        for (String spm : reqPoolSpmMap.keySet()) {
            if (!spmMapInfoMap.containsKey(spm)) {
                log.warn("spm={}?????????????????????`eis_spm_map_info`???", spm);
                continue;
            }
            EisReqPoolSpm currReqPoolSpm = reqPoolSpmMap.get(spm);
            SpmInfoDTO currSpmInfoDTO = spmMapInfoMap.get(spm);
            // ????????????
            Boolean isDeployed = spmToDeployedMap.getOrDefault(spm, false);
            Long spmId = currSpmInfoDTO.getId();
            String spmReverse = String.join("|",
                    Lists.reverse(Arrays.asList(spm.split("\\|"))));
            Long reqPoolEntityId = currReqPoolSpm.getId();
            Integer spmStatus = spmStatusMap.getOrDefault(reqPoolEntityId, 1);
            // ??????????????????SPM???????????????
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
            // ????????????
            spmMapInfoVOS.add(spmMapInfoVO);
        }
        //????????????spm
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
                // ????????????
                spmMapInfoVOS.add(0, atificialSpmMapInfoVO);
            }
        }
        // 5. ????????????????????? ????????????
        // ??????????????????
        if (null != param.getIsMapped()) {
            spmMapInfoVOS = spmMapInfoVOS.stream()
                    .filter(k -> param.getIsMapped() ? CollectionUtils.isNotEmpty(k.getSpmOldList())
                            : CollectionUtils.isEmpty(k.getSpmOldList()))
                    .collect(Collectors.toList());
        }
        // ????????????
        if (CollectionUtils.isNotEmpty(param.getMapStatus())) {
            spmMapInfoVOS = spmMapInfoVOS.stream()
                    .filter(k -> param.getMapStatus().contains(k.getMapStatus()))
                    .collect(Collectors.toList());
        }
        // spm??????
        if (null != param.getSpmStatus()) {
            spmMapInfoVOS = spmMapInfoVOS.stream()
                    .filter(k -> param.getSpmStatus().equals(k.getSpmStatus()))
                    .collect(Collectors.toList());
        }
        // spm??????
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
        // spm?????????
        if (StringUtils.isNotBlank(param.getSpmOrName())) {
            String spmOrName = param.getSpmOrName();
            spmMapInfoVOS = spmMapInfoVOS.stream()
                    .filter(k -> k.getSpm().contains(spmOrName) || k.getName().contains(spmOrName))
                    .collect(Collectors.toList());
        }
        // ???spm?????????????????????
        if (StringUtils.isNotBlank(param.getSpmOldOrMapVersion())) {
            String searchStr = param.getSpmOldOrMapVersion();
            spmMapInfoVOS = spmMapInfoVOS.stream()
                    .filter(k -> (searchStr.equals(k.getVersion()))
                            || (CollectionUtils.isNotEmpty(k.getSpmOldList())
                            && k.getSpmOldList().contains(searchStr)))
                    .collect(Collectors.toList());
        }

        // 6. ??????
        spmMapInfoVOS.sort(Comparator.comparing(SpmMapInfoVO::getSource, Comparator.reverseOrder()).thenComparing(SpmMapInfoVO::getSpmReverse));
        // spmReverse?????????????????????????????????????????????
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
            // ?????????????????????????????????????????????
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
