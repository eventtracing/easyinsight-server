package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.image.ImageRelationDTO;

import java.util.Collection;
import java.util.List;

public interface ImageRelationService {
  void create(ImageRelationDTO imageRelationDTO);

  void createBatch(List<ImageRelationDTO> imageRelationDTOs);

  List<ImageRelationDTO> getByEntityId(Collection<Long> entityIds);

  Integer deleteImageRelation(Collection<Long> entityIds, Integer entityType);
}
