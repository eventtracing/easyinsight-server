package com.netease.hz.bdms.easyinsight.service.helper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.tag.SpmTagSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.tag.TagSimpleDTO;
import com.netease.hz.bdms.easyinsight.service.service.TagService;
import com.netease.hz.bdms.easyinsight.service.service.SpmTagService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: xumengqiang
 * @date: 2021/11/10 15:44
 */

@Component
@Slf4j
public class SpmTagHelper {

    @Resource
    private SpmTagService spmTagService;

    @Resource
    private TagService tagService;

    /**
     * 批量插入SPM的绑定标签
     *
     * @param spmIds spmId列表
     * @param tagIds 标签ID列表
     * @param currentUser 当前用户
     */
    public void createSpmTag(List<Long> spmIds, List<Long> tagIds, UserSimpleDTO currentUser){
        // 参数检查
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(spmIds), "spmIds不能为空");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(tagIds), "tagIds不能为空");
        Preconditions.checkArgument(null != currentUser, "当前用户信息不能为空");

        // 记录构造
        List<SpmTagSimpleDTO> spmTagSimpleDTOS = Lists.newArrayList();
        for (Long spmId : spmIds) {
            for (Long tagId : tagIds) {
                // 构造记录
                SpmTagSimpleDTO spmTagSimpleDTO = new SpmTagSimpleDTO();
                spmTagSimpleDTO.setSpmId(spmId);
                spmTagSimpleDTO.setTagId(tagId);
                spmTagSimpleDTO.setCreator(currentUser);
                spmTagSimpleDTO.setUpdater(currentUser);
                // 记录加入集合
                spmTagSimpleDTOS.add(spmTagSimpleDTO);
            }
        }
        // 批量添加  // todo DuplicateKeyException 问题
        spmTagService.create(spmTagSimpleDTOS);
    }

    /**
     * 获取 spm 绑定的标签信息
     *
     * @param spmIds
     * @return
     */
    public Map<Long, List<TagSimpleDTO>> getSpmIdToTagSetMap(Collection<Long> spmIds){
        if(CollectionUtils.isEmpty(spmIds)){
            return null;
        }
        List<SpmTagSimpleDTO> spmTagSimpleDTOS = spmTagService.getBySpmIds(spmIds);
        Set<Long> tagIds = spmTagSimpleDTOS.stream()
                .map(SpmTagSimpleDTO::getTagId)
                .collect(Collectors.toSet());
        List<TagSimpleDTO> tagSimpleDTOS = tagService.getByIds(tagIds);
        Map<Long, TagSimpleDTO> tagMap = tagSimpleDTOS.stream()
                .collect(Collectors.toMap(TagSimpleDTO::getId, Function.identity()));

        Map<Long, List<TagSimpleDTO>> spmIdToTagIdSetMap = Maps.newHashMap();
        for (SpmTagSimpleDTO spmTagSimpleDTO : spmTagSimpleDTOS) {
            Long spmId = spmTagSimpleDTO.getSpmId();
            Long tagId = spmTagSimpleDTO.getTagId();
            if(tagMap.containsKey(tagId)) {
                List<TagSimpleDTO> spmTagSimpleDTOList = spmIdToTagIdSetMap
                        .computeIfAbsent(spmId, k -> Lists.newArrayList());
                spmTagSimpleDTOList.add(tagMap.get(tagId));
            }

        }
        return spmIdToTagIdSetMap;
    }
}
