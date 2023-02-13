package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.tag.SpmTagSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.SpmTagMapper;
import com.netease.hz.bdms.easyinsight.dao.model.SpmTag;
import com.netease.hz.bdms.easyinsight.service.service.SpmTagService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: xumengqiang
 * @date: 2021/11/10 15:31
 */
@Service
public class SpmTagServiceImpl implements SpmTagService {
    @Autowired
    private SpmTagMapper spmTagMapper;

    private SpmTag dto2Do(SpmTagSimpleDTO spmTagSimpleDTO){
        SpmTag spmTag = BeanConvertUtils.convert(spmTagSimpleDTO, SpmTag.class);
        if(null != spmTag){
            UserSimpleDTO updater = spmTagSimpleDTO.getUpdater();
            UserSimpleDTO creator = spmTagSimpleDTO.getCreator();

            if (creator != null) {
                spmTag.setCreateEmail(creator.getEmail())
                        .setCreateName(creator.getUserName());
            }
            if (updater != null) {
                spmTag.setUpdateEmail(updater.getEmail())
                        .setUpdateName(updater.getUserName());
            }
        }
        return spmTag;
    }

    private SpmTagSimpleDTO do2Dto(SpmTag spmTag){
        SpmTagSimpleDTO spmTagSimpleDTO = BeanConvertUtils.convert(spmTag, SpmTagSimpleDTO.class);
        if (null != spmTagSimpleDTO) {
            UserSimpleDTO updater = new UserSimpleDTO(spmTag.getUpdateEmail(), spmTag.getUpdateName());
            UserSimpleDTO creator = new UserSimpleDTO(spmTag.getCreateEmail(), spmTag.getCreateName());

            spmTagSimpleDTO.setCreator(creator)
                    .setUpdater(updater);
        }
        return spmTagSimpleDTO;
    }

    @Override
    public List<Long> create(Collection<SpmTagSimpleDTO> spmTagSimpleDTOS) {
        // 数据转化
        List<SpmTag> spmTagList = spmTagSimpleDTOS.stream()
                .map(this::dto2Do)
                .collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(spmTagList)){
            spmTagMapper.insert(spmTagList);
            List<Long> res = spmTagList.stream()
                    .map(SpmTag::getId)
                    .collect(Collectors.toList());
            return res;
        }
        return Lists.newArrayList();
    }

    @Override
    public Integer deleteBySpmId(Collection<Long> spmIds) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(spmIds), "spmIds不能为空");
        return spmTagMapper.deleteBySpmId(spmIds);
    }

    @Override
    public List<SpmTagSimpleDTO> getBySpmIds(Collection<Long> spmIds) {
        // 1. 参数检查
        if(CollectionUtils.isNotEmpty(spmIds)) {
            // 2. 查询
            List<SpmTag> spmTagList = spmTagMapper.selectBySpmIds(spmIds);

            // 3. 数据转化
            List<SpmTagSimpleDTO> result = spmTagList.stream()
                    .map(this::do2Dto)
                    .collect(Collectors.toList());
            return result;
        }
        return Lists.newArrayList();
    }

    @Override
    public Integer deleteByIds(Collection<Long> ids) {
        if(CollectionUtils.isNotEmpty(ids)){
            return spmTagMapper.deleteByIds(ids);
        }
        return 0;
    }
}
