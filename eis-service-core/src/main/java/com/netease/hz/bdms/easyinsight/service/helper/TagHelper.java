package com.netease.hz.bdms.easyinsight.service.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netease.hz.bdms.easyinsight.common.dto.tag.ObjTagSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.tag.TagSimpleDTO;
import com.netease.hz.bdms.easyinsight.service.service.ObjTagService;
import com.netease.hz.bdms.easyinsight.service.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TagHelper {

    @Resource
    private TagService tagService;
    @Resource
    private ObjTagService objTagService;

    /**
     * 给定对象变更ID，获取对象绑定标签信息，返回<objId, [tagSimpleDTO, ...]>映射
     *
     * @param objIds 对象变更历史ID集合
     * @return
     */
    public Map<Long, List<TagSimpleDTO>> getObjTagMap(Set<Long> objIds) {
        List<ObjTagSimpleDTO> objTagSimpleDTOS = objTagService.getByObjIds(objIds);

        Set<Long> tagIds = objTagSimpleDTOS.stream()
                .map(ObjTagSimpleDTO::getTagId)
                .collect(Collectors.toSet());

        List<TagSimpleDTO> tagSimpleDTOS = tagService.getByIds(tagIds);
        Map<Long, TagSimpleDTO> tagMap = tagSimpleDTOS.stream()
                .collect(Collectors.toMap(TagSimpleDTO::getId, Function.identity()));

        Map<Long, List<TagSimpleDTO>> objTagMap = Maps.newHashMap();
        for (ObjTagSimpleDTO objTagSimpleDTO : objTagSimpleDTOS) {
            Long objId = objTagSimpleDTO.getObjId();
            Long tagId = objTagSimpleDTO.getTagId();
            TagSimpleDTO tagSimpleDTO = tagMap.get(tagId);
            if (tagSimpleDTO != null) {
                List<TagSimpleDTO> tagList = objTagMap.computeIfAbsent(
                        objId, k -> Lists.newArrayList());
                tagList.add(tagSimpleDTO);
            }

        }

        return objTagMap;
    }
}
