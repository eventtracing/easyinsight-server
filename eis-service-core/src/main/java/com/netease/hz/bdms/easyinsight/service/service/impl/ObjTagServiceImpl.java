package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.tag.ObjTagSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.tag.TagSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.dao.ObjTagMapper;
import com.netease.hz.bdms.easyinsight.dao.model.ObjTag;
import com.netease.hz.bdms.easyinsight.dao.model.ObjectBasic;
import com.netease.hz.bdms.easyinsight.service.service.ObjTagService;
import com.netease.hz.bdms.easyinsight.service.service.ObjectBasicService;
import com.netease.hz.bdms.easyinsight.service.service.TagService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ObjTagServiceImpl implements ObjTagService {

    @Autowired
    private ObjTagMapper objTagMapper;

    @Resource
    private TagService tagService;

    @Resource
    private ObjectBasicService objectBasicService;


    private ObjTagSimpleDTO do2Dto(ObjTag objTag) {
        ObjTagSimpleDTO objTagSimpleDTO = BeanConvertUtils.convert(objTag, ObjTagSimpleDTO.class);
        if (null != objTagSimpleDTO) {
            UserSimpleDTO updater = new UserSimpleDTO(objTag.getUpdateEmail(), objTag.getUpdateName());
            UserSimpleDTO creator = new UserSimpleDTO(objTag.getCreateEmail(), objTag.getCreateName());

            objTagSimpleDTO.setCreator(creator)
                    .setUpdater(updater);
        }
        return objTagSimpleDTO;
    }

    private ObjTag dto2Do(ObjTagSimpleDTO objTagSimpleDTO) {
        ObjTag objTag = BeanConvertUtils.convert(objTagSimpleDTO, ObjTag.class);
        if (objTag != null) {
            UserSimpleDTO updater = objTagSimpleDTO.getUpdater();
            UserSimpleDTO creator = objTagSimpleDTO.getCreator();

            if (creator != null) {
                objTag.setCreateEmail(creator.getEmail())
                        .setCreateName(creator.getUserName());
            }
            if (updater != null) {
                objTag.setUpdateEmail(updater.getEmail())
                        .setUpdateName(updater.getUserName());
            }
        }
        return objTag;
    }

    @Override
    public List<ObjTagSimpleDTO> getByTagIds(Set<Long> tagIds) {
        if(CollectionUtils.isNotEmpty(tagIds)){
            List<ObjTag> objTags = objTagMapper.selectObjTagsByTagIds(tagIds);
            // 历史存在重复数据（当时是按historyId区分的），现已不需要按historyId区分，因此查出来时，需要把tagId重复的去除（可能对应多个historyId）
            Map<String, ObjTag> map = new HashMap<>();
            objTags.forEach(objTag -> {
                String key = objTag.getObjId() + "_" + objTag.getTagId();
                map.put(key, objTag);
            });
            return map.values().stream().map(this::do2Dto).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    public ObjTagSimpleDTO getByObjectIdAndTagId(Long objId, Long tagId) {
        List<ObjTag> objTags = objTagMapper.selectObjTagsByObjIdAndTagId(objId, tagId);
        if (CollectionUtils.isEmpty(objTags)) {
            return null;
        }
        return objTags.stream().map(this::do2Dto).collect(Collectors.toList()).get(0);
    }

    @Override
    public void createBatch(Collection<ObjTagSimpleDTO> objTags) {
        if (CollectionUtils.isNotEmpty(objTags)) {
            UserDTO currUser = EtContext.get(ContextConstant.USER);
            List<ObjTag> objTagList = objTags.stream().map(this::dto2Do).collect(Collectors.toList());
            objTagList.forEach( objTag -> {
                objTag.setCreateName(currUser.getUserName());
                objTag.setCreateEmail(currUser.getEmail());
                objTag.setUpdateName(currUser.getUserName());
                objTag.setUpdateEmail(currUser.getEmail());
                objTag.setCreateTime(new Timestamp(System.currentTimeMillis()));
                objTag.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            });
            objTagMapper.batchInsert(objTagList);
        }
    }

    @Override
    public Integer deleteObjTagByObjIds(Collection<Long> objIds) {
        if (CollectionUtils.isNotEmpty(objIds)) {
            return objTagMapper.deleteObjTag(objIds);
        }
        return 0;
    }

    @Override
    public List<ObjTagSimpleDTO> selectObjTags(Collection<Long> tagIds, Collection<Long> historyIds,
                                               Collection<Long> objIds) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        List<ObjTag> objTags = objTagMapper.select(appId, tagIds, historyIds, objIds);
        return objTags.stream().map(this::do2Dto).collect(Collectors.toList());
    }

    @Override
    public List<ObjTagSimpleDTO> getByObjIds(Set<Long> objIds) {
        if (CollectionUtils.isNotEmpty(objIds)) {
            List<ObjTag> objTags = objTagMapper.selectObjTagsByObjIds(objIds);

            // 历史存在重复数据（当时是按historyId区分的），现已不需要按historyId区分，因此查出来时，需要把tagId重复的去除（可能对应多个historyId）
            Map<String, ObjTag> map = new HashMap<>();
            objTags.forEach(objTag -> {
                String key = objTag.getObjId() + "_" + objTag.getTagId();
                map.put(key, objTag);
            });

            return map.values().stream().map(this::do2Dto).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    public List<ObjTagSimpleDTO> listAll() {
        long offset = 0;
        int limit = 1000;
        List<ObjTag> tags = new ArrayList<>(1000);
        while (true) {
            List<ObjTag> list = objTagMapper.listByOffset(offset, limit);
            tags.addAll(list);
            if (CollectionUtils.isEmpty(list) || list.size() < limit) {
                break;
            }
            offset = list.stream().max(Comparator.comparingLong(ObjTag::getId)).get().getId();
        }
        return tags.stream().map(this::do2Dto).collect(Collectors.toList());
    }

    public void check() {
        List<ObjTagSimpleDTO> objTagList = listAll();

        List<Long> tagIds = objTagList.stream().map(ObjTagSimpleDTO::getTagId).collect(Collectors.toList());
        List<TagSimpleDTO> tags = tagService.getByIds(tagIds);
        Map<Long, TagSimpleDTO> tagsMap = new HashMap<>();
        tags.forEach(tag -> tagsMap.put(tag.getId(), tag));

        List<Long> objIds = objTagList.stream().map(ObjTagSimpleDTO::getObjId).collect(Collectors.toList());
        List<ObjectBasic> objectBasics = objectBasicService.getByIds(objIds);
        Map<Long, ObjectBasic> objsMap = new HashMap<>();
        objectBasics.forEach(objectBasic -> objsMap.put(objectBasic.getId(), objectBasic));

        List<ObjTagDetail> objTagDetails = objTagList.stream().map(objTagSimpleDTO -> {
            ObjTagDetail objTagDetail = new ObjTagDetail();
            objTagDetail.setObjTag(objTagSimpleDTO);
            Long objId = objTagSimpleDTO.getObjId();
            Long tagId = objTagSimpleDTO.getTagId();
            objTagDetail.setObject(objsMap.get(objId));
            objTagDetail.setTag(tagsMap.get(tagId));
            if (objTagDetail.getObject() == null) {
                return null;
            }
            return objTagDetail;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        List<String> desc = new ArrayList<>();
        Map<Long, List<ObjTagDetail>> groupByObjId = objTagDetails.stream().collect(Collectors.groupingBy(objTagDetail -> objTagDetail.getObjTag().getObjId()));
        groupByObjId.forEach((objId, l) -> {
            Set<Long> tagIdSet = new HashSet<>();
            Map<Long, List<ObjTagDetail>> groupingByHistoryId = l.stream().collect(Collectors.groupingBy(objTagDetail -> objTagDetail.getObjTag().getHistoryId()));
            // 查找是否有同一个对象下，不同的historyId下，tag打的不一样
            groupingByHistoryId.forEach((historyId, list) -> {
                Set<Long> currentTagIdSet = list.stream().map(objTagDetail -> objTagDetail.getObjTag().getTagId()).collect(Collectors.toSet());
                if (CollectionUtils.isEmpty(tagIdSet)) {
                    tagIdSet.addAll(currentTagIdSet);
                } else {
                    if (!StringUtils.equals(tagIdSet.toString(), currentTagIdSet.toString())) {
                        desc.add("objId " + objId + " 存在多个历史版本标签不一致情况");
                    }
                }
            });
        });

        log.info("desc={}", JsonUtils.toJson(desc));
    }

    @Data
    public static class ObjTagDetail {
        ObjTagSimpleDTO objTag;
        ObjectBasic object;
        TagSimpleDTO tag;
    }

}
