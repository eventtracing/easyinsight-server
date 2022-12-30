package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.image.ImageRelationDTO;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.ImageRelationMapper;
import com.netease.hz.bdms.easyinsight.dao.model.ImageRelation;
import com.netease.hz.bdms.easyinsight.service.service.ImageRelationService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImageRelationServiceImpl implements ImageRelationService {

    @Autowired
    private ImageRelationMapper imageRelationMapper;


    private ImageRelationDTO do2Dto(ImageRelation imageRelation) {
        ImageRelationDTO imageRelationDTO = BeanConvertUtils
                .convert(imageRelation, ImageRelationDTO.class);
        if (null != imageRelationDTO) {
            UserSimpleDTO updater = new UserSimpleDTO(imageRelation.getUpdateEmail(),
                    imageRelation.getUpdateName());
            UserSimpleDTO creator = new UserSimpleDTO(imageRelation.getCreateEmail(),
                    imageRelation.getCreateName());

            imageRelationDTO.setCreator(creator)
                    .setUpdater(updater);
        }
        return imageRelationDTO;
    }

    private ImageRelation dto2Do(ImageRelationDTO imageRelationDTO) {
        ImageRelation imageRelation = BeanConvertUtils.convert(imageRelationDTO, ImageRelation.class);
        if (imageRelation != null) {
            UserSimpleDTO updater = imageRelationDTO.getUpdater();
            UserSimpleDTO creator = imageRelationDTO.getCreator();

            if (creator != null) {
                imageRelation.setCreateEmail(creator.getEmail())
                        .setCreateName(creator.getUserName());
            }
            if (updater != null) {
                imageRelation.setUpdateEmail(updater.getEmail())
                        .setUpdateName(updater.getUserName());
            }
        }
        return imageRelation;
    }

    @Override
    public void create(ImageRelationDTO imageRelationDTO) {
        Preconditions.checkArgument(null != imageRelationDTO, "图片关联对象不能为空");
        ImageRelation imageRelation = dto2Do(imageRelationDTO);
        if (imageRelation == null) {
            throw new CommonException("imageRelation为空");
        }
        // 公共信息设置
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        if(null != currUser) {
            imageRelation.setCreateName(currUser.getUserName());
            imageRelation.setCreateEmail(currUser.getEmail());
            imageRelation.setUpdateName(currUser.getUserName());
            imageRelation.setUpdateEmail(currUser.getEmail());
        }
        imageRelationMapper.insert(imageRelation);
    }

    @Override
    public void createBatch(List<ImageRelationDTO> imageRelationDTOs) {
        if (CollectionUtils.isNotEmpty(imageRelationDTOs)) {
            List<ImageRelation> imageRelations = imageRelationDTOs.stream()
                    .map(this::dto2Do).collect(Collectors.toList());
            // 设置公共基本信息
            UserDTO currUser = EtContext.get(ContextConstant.USER);
            if(null != currUser){
                imageRelations.forEach(imageRelation -> {
                    imageRelation.setCreateName(currUser.getUserName());
                    imageRelation.setCreateEmail(currUser.getEmail());
                    imageRelation.setUpdateName(currUser.getUserName());
                    imageRelation.setUpdateEmail(currUser.getEmail());
                });
            }

            imageRelationMapper.batchInsert(imageRelations);
        }
    }

    @Override
    public List<ImageRelationDTO> getByEntityId(Collection<Long> entityIds) {
        List<ImageRelationDTO> imageRelationDTOS = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(entityIds)) {
            List<ImageRelation> imageRelations = imageRelationMapper.selectByEntityId(entityIds);
            return imageRelations.stream().map(this::do2Dto).collect(Collectors.toList());
        }
        return imageRelationDTOS;
    }

    @Override
    public Integer deleteImageRelation(Collection<Long> entityIds, Integer entityType) {
        if (CollectionUtils.isNotEmpty(entityIds) && null != entityType) {
            return imageRelationMapper.deleteByEntityId(entityIds, entityType);
        }
        return 0;
    }

}
