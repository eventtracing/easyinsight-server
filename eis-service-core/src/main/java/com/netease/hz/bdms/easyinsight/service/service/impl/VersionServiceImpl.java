package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.version.VersionSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.VersionSourceEnum;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.VersionMapper;
import com.netease.hz.bdms.easyinsight.dao.model.Version;
import com.netease.hz.bdms.easyinsight.service.service.VersionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VersionServiceImpl implements VersionService {

  private static final Map<String, String> orderByMap = ImmutableMap
      .of("createTime", "create_time", "updateTime", "update_time");
  private static final Map<String, String> orderRuleMap = ImmutableMap
      .of("descend", "desc", "ascend", "asc");
  @Autowired
  private VersionMapper versionMapper;

  private VersionSimpleDTO do2Dto(Version version) {
    VersionSimpleDTO versionSimpleDTO = BeanConvertUtils
        .convert(version, VersionSimpleDTO.class);
    if (null != versionSimpleDTO) {
      UserSimpleDTO updater = new UserSimpleDTO(version.getUpdateEmail(),
          version.getUpdateName());
      UserSimpleDTO creator = new UserSimpleDTO(version.getCreateEmail(),
          version.getCreateName());

      versionSimpleDTO.setCreator(creator)
          .setUpdater(updater);
    }
    return versionSimpleDTO;
  }

  private Version dto2Do(VersionSimpleDTO versionSimpleDTO) {
    Version version = BeanConvertUtils.convert(versionSimpleDTO, Version.class);
    if (version != null) {
      UserSimpleDTO updater = versionSimpleDTO.getUpdater();
      UserSimpleDTO creator = versionSimpleDTO.getCreator();

      if (creator != null) {
        version.setCreateEmail(creator.getEmail())
            .setCreateName(creator.getUserName());
      }
      if (updater != null) {
        version.setUpdateEmail(updater.getEmail())
            .setUpdateName(updater.getUserName());
      }
      if(version.getCreateTime() == null){
        version.setCreateTime(new Timestamp(System.currentTimeMillis()));
      }
      if(version.getUpdateTime() == null){
        version.setUpdateTime(new Timestamp(System.currentTimeMillis()));
      }
    }
    return version;
  }


  @Override
  public List<VersionSimpleDTO> getVersionByEntityId(Long entityId, Integer entityType, String name,
      Long appId) {
    Preconditions.checkArgument(null != appId, "产品ID不能为空");

    List<Version> versions = versionMapper.selectByEntityId(entityId, entityType, name, appId);
    return versions.stream().map(this::do2Dto).collect(Collectors.toList());
  }

  @Override
  public Integer getVersionSizeByEntityId(Long entityId, Integer entityType, String name,
      Long appId) {
    Preconditions.checkArgument(null != appId, "产品ID不能为空");

    return versionMapper.selectSizeByEntityId(entityId, entityType, name, appId);
  }

  @Override
  public VersionSimpleDTO getVersionById(Long versionId) {
    Preconditions.checkArgument(null != versionId, "版本ID不能为空");

    Version version = versionMapper.selectByPrimaryKey(versionId);
    return do2Dto(version);
  }

  @Override
  public Long createVersion(VersionSimpleDTO versionSimpleDTO) {
    Version version = dto2Do(versionSimpleDTO);
    Preconditions.checkArgument(null != version, "版本对象不能为空");
    versionMapper.insert(version);
    return version.getId();
  }

  @Override
  public Integer updateVersion(VersionSimpleDTO versionSimpleDTO) {
    Version version = dto2Do(versionSimpleDTO);
    Preconditions.checkArgument(null != version, "版本对象不能为空");
    return versionMapper.update(version);
  }

  @Override
  public Integer deleteVersion(Long versionId) {
    Preconditions.checkArgument(null != versionId, "版本ID不能为空");
    return versionMapper.delete(versionId);
  }

  @Override
  public Integer deleteVersion(Long entityId, Integer entityType, Long appId) {
    Preconditions.checkArgument(null != entityId && null != entityType, "关联版本的实体不能为空");
    Preconditions.checkArgument(null != appId, "产品ID不能为空");

    return versionMapper.deleteVersion(entityId, entityType, appId);
  }

  @Override
  public Integer searchVersionSize(Long entityId, Integer entityType, String search, Long appId) {
    Preconditions.checkArgument(null != appId, "版本Id不能为空");
    return versionMapper.searchSizeByEntityId(entityId, entityType, search, appId);
  }

  @Override
  public List<VersionSimpleDTO> searchVersion(Long entityId, Integer entityType, String search,
      Long appId, String orderBy, String orderRule, Integer offset, Integer pageSize) {
    Preconditions.checkArgument(null != appId, "版本Id不能为空");

    String dbOrderBy = orderByMap.get(orderBy);
    String dbOrderRule = orderRuleMap.get(orderRule);

    List<Version> versions = versionMapper
        .searchByEntityId(entityId, entityType, search, appId, dbOrderBy, dbOrderRule, offset,
            pageSize);
    return versions.stream().map(this::do2Dto).collect(Collectors.toList());
  }

  /**
   * 设置版本信息
   *
   * @param appId        产品ID,必填参数
   * @param entityId     关联元素ID，必填参数
   * @param entityType   关于元素类型，必填参数
   * @param versionId    版本ID，非必填参数
   * @param currentUsing 是否是当前使用版本
   * @return
   */
  @Override
  public void setVersion(Long appId, Long entityId, Integer entityType, Long versionId,
      Boolean currentUsing) {
    Preconditions.checkArgument(null != appId, "产品ID不能为空");
    Preconditions.checkArgument(null != entityId && null != entityType, "关联元素信息不能为空");
    Preconditions.checkArgument(null != currentUsing, "是否是当前使用版本参数不能为空");

    versionMapper.setVersion(appId, entityId, entityType, versionId, currentUsing);
  }

  @Override
  public Long presetVersion(Long appId, Long entityId, Integer entityType,
      UserSimpleDTO currentUser) {
    Preconditions.checkArgument(null != appId, "产品ID不能为空");
    Preconditions.checkArgument(null != entityId && null != entityType, "关联元素不能为空");

    String currentEmail = currentUser.getEmail();
    String currentName = currentUser.getUserName();

    Version version = new Version();
    version.setAppId(appId)
        .setName("预置版本")
        .setEntityId(entityId)
        .setEntityType(entityType)
        .setVersionSource(VersionSourceEnum.MANUAL.getType())
        .setCurrentUsing(true)
        .setPreset(true)
        .setCreateEmail(currentEmail)
        .setCreateName(currentName)
        .setUpdateEmail(currentEmail)
        .setUpdateName(currentName)
        .setCreateTime(new Timestamp(System.currentTimeMillis()))
        .setUpdateTime(new Timestamp(System.currentTimeMillis()));
    versionMapper.insert(version);
    return version.getId();
  }

  @Override
  public List<VersionSimpleDTO> getCurrentUsingVersionByEntityId(Long appId,
      Collection<Long> entityIds,
      Integer entityType) {
    Preconditions.checkArgument(null != appId, "产品ID不能为空");
    Preconditions
        .checkArgument(CollectionUtils.isNotEmpty(entityIds) && null != entityType, "实体信息不能为空");

    List<Version> versions = versionMapper
        .selectVersionByEntityId(appId, entityIds, Collections.singleton(entityType), true);
    return versions.stream().map(this::do2Dto).collect(Collectors.toList());
  }


  @Override
  public List<VersionSimpleDTO> searchVersion(Long appId, Collection<Long> versionIds,
      Collection<Integer> entityTypes) {
    Preconditions.checkArgument(null != appId, "产品ID不能为空");
    List<Version> versions = versionMapper.selectVersion(appId, versionIds, entityTypes);
    return versions.stream().map(this::do2Dto).collect(Collectors.toList());
  }

  @Override
  public List<VersionSimpleDTO> searchVersionByEntityId(Long appId, Collection<Long> entityIds,
      Collection<Integer> entityTypes) {
    Preconditions.checkArgument(null != appId, "产品ID不能为空");
    List<Version> versions = versionMapper.selectVersionByEntityId(appId, entityIds, entityTypes, null);
    return versions.stream().map(this::do2Dto).collect(Collectors.toList());
  }

}
