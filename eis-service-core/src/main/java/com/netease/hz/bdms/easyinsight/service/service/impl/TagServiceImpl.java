package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.netease.eis.adapters.CacheAdapter;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.tag.TagSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.common.util.CacheUtils;
import com.netease.hz.bdms.easyinsight.dao.TagMapper;
import com.netease.hz.bdms.easyinsight.dao.model.Tag;
import com.netease.hz.bdms.easyinsight.service.service.TagService;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Resource
    private CacheAdapter cacheAdapter;

    private static final Map<String, String> orderByMap = ImmutableMap
            .of("createTime", "create_time", "updateTime", "update_time");
    private static final Map<String, String> orderRuleMap = ImmutableMap
            .of("descend", "desc", "ascend", "asc");


    private TagSimpleDTO do2Dto(Tag tag) {
        TagSimpleDTO tagSimpleDTO = BeanConvertUtils.convert(tag, TagSimpleDTO.class);
        if (null != tagSimpleDTO) {
            UserSimpleDTO updater = new UserSimpleDTO(tag.getUpdateEmail(), tag.getUpdateName());
            UserSimpleDTO creator = new UserSimpleDTO(tag.getCreateEmail(), tag.getCreateName());

            tagSimpleDTO.setCreator(creator)
                    .setUpdater(updater);
        }
        return tagSimpleDTO;
    }

    private Tag dto2Do(TagSimpleDTO tagSimpleDTO) {
        Tag tag = BeanConvertUtils.convert(tagSimpleDTO, Tag.class);
        if (tag != null) {
            UserSimpleDTO updater = tagSimpleDTO.getUpdater();
            UserSimpleDTO creator = tagSimpleDTO.getCreator();

            if (creator != null) {
                tag.setCreateEmail(creator.getEmail())
                        .setCreateName(creator.getUserName());
            }
            if (updater != null) {
                tag.setUpdateEmail(updater.getEmail())
                        .setUpdateName(updater.getUserName());
            }
        }
        return tag;
    }

    @Override
    public Long create(TagSimpleDTO tagSimpleDTO) {
        Tag tag = dto2Do(tagSimpleDTO);
        Preconditions.checkArgument(null != tag, "标签参数不能为空");

        tagMapper.insertTag(tag);
        return tag.getId();
    }

    @Override
    public List<TagSimpleDTO> getByIds(Collection<Long> tagIds) {
        if (CollectionUtils.isNotEmpty(tagIds)) {
            List<Tag> tags = tagMapper.selectTags(tagIds);
            return tags.stream().map(this::do2Dto).collect(Collectors.toList());
        }

        return Lists.newArrayList();
    }


    @Override
    public List<TagSimpleDTO> searchTags(String keyword, Integer type, Integer offset, Integer count,
                                         String orderBy, String orderRule) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        String dbOrderBy = orderByMap.get(orderBy);
        String dbOrderRule = orderRuleMap.get(orderRule);
        List<Tag> tags = tagMapper.searchTags(keyword, type, appId, offset, count, dbOrderBy, dbOrderRule);
        return tags.stream().map(this::do2Dto).collect(Collectors.toList());
    }

    @Override
    public TagSimpleDTO getTag(Long appId, Integer type, String name) {
        List<Tag> tags = tagMapper.selectTagByName(appId, type, name);
        if (CollectionUtils.isNotEmpty(tags)) {
            return do2Dto(tags.get(0));
        }
        return null;
    }

    @Override
    public Integer selectTotal(Long appId, Integer type, String keyword) {
        return tagMapper.searchTagSize(appId, type, keyword);
    }

    @Override
    public List<TagSimpleDTO> search(TagSimpleDTO query) {
        Tag tag = dto2Do(query);

        List<Tag> tags = tagMapper.search(tag);

        return tags.stream().map(this::do2Dto).collect(Collectors.toList());
    }

    @Override
    public List<TagSimpleDTO> getAllTags(Long appId) {
        TagListHolder listHolder = CacheUtils.getAndSetIfAbsent(() -> "getAllTags_" + appId,
            () -> doGetAllEventCodes(appId),
            (key) -> cacheAdapter.get(key),
            (key, value) -> cacheAdapter.setWithExpireTime(key, value, 120),
            TagListHolder.class);
        return listHolder == null ? new ArrayList<>(0) : listHolder.getList();
}

    public TagListHolder doGetAllEventCodes(Long appId) {
        List<Tag> tags = tagMapper.selectAllTagByAppId(appId);
        return new TagListHolder().setList(tags.stream().map(this::do2Dto).collect(Collectors.toList()));
    }

    @Accessors(chain = true)
    @Data
    public static class TagListHolder {
        private List<TagSimpleDTO> list;
    }
}
