package com.netease.hz.bdms.easyinsight.service.facade;


import com.google.common.base.Preconditions;
import com.netease.hz.bdms.easyinsight.common.enums.TagTypeEnum;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingResultDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.tag.TagSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.service.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TagFacade {

    @Autowired
    private TagService tagService;


    public Long createTag(String name, Integer type) {
        Preconditions.checkArgument(StringUtils.isNotBlank(name), "标签名不能为空");
        Preconditions.checkArgument(TagTypeEnum.containsType(type), "不支持的标签类型");
        Long appId = EtContext.get(ContextConstant.APP_ID);
        UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);

        // 检查name是否已存在:若已存在，则直接返回数据库中已存在的，否则创建再返回（创建时若遇到重复键的报错，则继续查询一次）
        TagSimpleDTO tag = tagService.getTag(appId, type, name);
        if (tag != null) {
            return tag.getId();
        } else {
            UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
            TagSimpleDTO tagSimpleDTO = new TagSimpleDTO();
            tagSimpleDTO.setAppId(appId)
                    .setType(type)
                    .setName(name)
                    .setCreator(currentUser)
                    .setUpdater(currentUser);
            try {
                return tagService.create(tagSimpleDTO);
            } catch (DuplicateKeyException e) {
                log.info("标签名称={}已存在", name, e);
                tag = tagService.getTag(appId, type, name);
                return tag.getId();
            }
        }
    }

    public PagingResultDTO<TagSimpleDTO> searchTag(String search, Integer type, PagingSortDTO pagingSortDTO) {
        Long appId = EtContext.get(ContextConstant.APP_ID);
        Preconditions.checkArgument(null != pagingSortDTO, "分页参数不能为空");
        Preconditions.checkArgument(TagTypeEnum.containsType(type), "不支持的标签类型");
        Integer total = tagService.selectTotal(appId, type, search);
        List<TagSimpleDTO> tags = tagService
                .searchTags(search, type, pagingSortDTO.getOffset(), pagingSortDTO.getPageSize(),
                        pagingSortDTO.getOrderBy(), pagingSortDTO.getOrderRule());
        PagingResultDTO<TagSimpleDTO> result = new PagingResultDTO();
        result.setTotalNum(total)
                .setPageNum(pagingSortDTO.getCurrentPage())
                .setList(tags);
        return result;
    }

    /**
     * 同步 对象标签至SPM标签
     *
     */
    @Transactional(rollbackFor = Throwable.class)
    public void syncTag(){
        Long appId = EtContext.get(ContextConstant.APP_ID);
        // 读取全部对象标签
        TagSimpleDTO objTagQuery = new TagSimpleDTO();
        objTagQuery.setAppId(appId)
                .setType(TagTypeEnum.OBJ_TAG.getType());
        List<TagSimpleDTO> objTagList = tagService.search(objTagQuery);
        // 读取全部SPM标签
        TagSimpleDTO spmTagQuery = new TagSimpleDTO();
        spmTagQuery.setAppId(appId)
                .setType(TagTypeEnum.SPM_TAG.getType());
        List<TagSimpleDTO> spmTagList = tagService.search(spmTagQuery);
        Set<String> spmTags = spmTagList.stream()
                .map(TagSimpleDTO::getName)
                .collect(Collectors.toSet());
        // 同步SPM
        for (TagSimpleDTO tagSimpleDTO : objTagList) {
            String name = tagSimpleDTO.getName();
            if(spmTags.contains(name)) {
                continue;
            }
            TagSimpleDTO spmTag = new TagSimpleDTO();
            spmTag.setType(TagTypeEnum.SPM_TAG.getType())
                    .setAppId(appId)
                    .setName(name);

            tagService.create(spmTag);
        }
    }
}
