package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.tag.TagSimpleDTO;

import java.util.Collection;
import java.util.List;

public interface TagService {
    Long create(TagSimpleDTO tagSimpleDTO);

    List<TagSimpleDTO> getByIds(Collection<Long> tagIds);

    List<TagSimpleDTO> searchTags(String keyword, Integer type, Integer offset, Integer count, String orderBy, String orderRule);

    TagSimpleDTO getTag(Long appId, Integer type, String name);

    Integer selectTotal(Long appId, Integer type, String keyword);

    List<TagSimpleDTO> search(TagSimpleDTO query);

    List<TagSimpleDTO> getAllTags(Long appId);
}
