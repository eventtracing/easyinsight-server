package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.version.VersionSimpleDTO;

import java.util.Collection;
import java.util.List;

public interface VersionService {

  List<VersionSimpleDTO> getVersionByEntityId(Long entityId, Integer entityType, String name,
      Long appId);

  Integer getVersionSizeByEntityId(Long entityId, Integer entityType, String name,
      Long appId);

  VersionSimpleDTO getVersionById(Long versionId);


  Long createVersion(VersionSimpleDTO versionSimpleDTO);

  Integer updateVersion(VersionSimpleDTO versionSimpleDTO);


  Integer deleteVersion(Long versionId);

  Integer deleteVersion(Long entityId, Integer entityType, Long appId);

  Integer searchVersionSize(Long entityId, Integer entityType, String search, Long appId);

  List<VersionSimpleDTO> searchVersion(Long entityId, Integer entityType, String search, Long appId,
      String orderBy, String orderRule, Integer offset, Integer pageSize);


  void setVersion(Long appId, Long entityId, Integer entityType, Long versionId,Boolean currentUsing);

  Long presetVersion(Long appId, Long entityId, Integer entityType, UserSimpleDTO currentUser);


  List<VersionSimpleDTO> getCurrentUsingVersionByEntityId(Long appId, Collection<Long> entityIds, Integer entityType);

  List<VersionSimpleDTO> searchVersion(Long appId, Collection<Long> versionIds, Collection<Integer> entityTypes);

  List<VersionSimpleDTO> searchVersionByEntityId(Long appId, Collection<Long> versionIds, Collection<Integer> entityTypes);

}
