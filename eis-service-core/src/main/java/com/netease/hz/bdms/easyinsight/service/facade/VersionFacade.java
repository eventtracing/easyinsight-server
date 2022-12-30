package com.netease.hz.bdms.easyinsight.service.facade;

import com.google.common.base.Preconditions;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.version.VersionSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.VersionSourceEnum;
import com.netease.hz.bdms.easyinsight.common.param.version.VersionCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.version.VersionSetParam;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.service.service.VersionService;
import com.netease.hz.bdms.easyinsight.service.helper.ParamBindHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;


@Slf4j
@Component
public class VersionFacade {

    @Autowired
    private VersionService versionService;
    @Autowired
    private ParamBindHelper paramBindHelper;


  public List<VersionSimpleDTO> listVersions(Long entityId, Integer entityType,
      String search) {
    // 验证参数
    Preconditions.checkArgument(null != entityId && null != entityType, "关联元素不能为空");

    Long appId = EtContext.get(ContextConstant.APP_ID);

    // 获取分页明细
    List<VersionSimpleDTO> versions = versionService
        .searchVersion(entityId, entityType, search, appId, null, null, null, null);

    return versions;
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public void setVersion(VersionSetParam param) {
    Preconditions.checkArgument(null != param, "参数不能为空");
    Preconditions.checkArgument(null != param.getVersionId(), "版本ID不能为空");
    Preconditions
        .checkArgument(null != param.getEntityId() && null != param.getEntityType(), "关联元素不能为空");

    // 检查参数
    Long appId = EtContext.get(ContextConstant.APP_ID);
    Long versionId = param.getVersionId();
    VersionSimpleDTO versionSimpleDTO = versionService.getVersionById(versionId);
    Preconditions.checkArgument(null != versionSimpleDTO, "该版本不存在，不可置为当前使用版本");
    Preconditions.checkArgument(null != param.getEntityId(), "param.getEntityId()不能为空");
    Preconditions.checkArgument(null != param.getEntityType(), "param.getEntityType()不能为空");
    Preconditions.checkArgument(param.getEntityId().equals(versionSimpleDTO.getEntityId())
        && param.getEntityType().equals( versionSimpleDTO.getEntityType()), "关联元素不正确");
    Long entityId = versionSimpleDTO.getEntityId();
    Integer entityType = versionSimpleDTO.getEntityType();

    // 将同entityId, entityType, appId下的其他产品置为当前不可使用
    versionService.setVersion(appId, entityId, entityType, null, false);
    // 将版本指定为当前正在使用版本
    versionService.setVersion(appId, entityId, entityType, versionId, true);
  }

  public Long createVersion(VersionCreateParam param) {
    Preconditions.checkArgument(null != param, "版本不能为空");
    Preconditions.checkArgument(StringUtils.isNotBlank(param.getName()), "版本不能为空");
    Preconditions
        .checkArgument(null != param.getEntityId() && null != param.getEntityType(), "关联元素不能为空");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "产品信息不能为空");

    // 判断是否存在同名版本
    List<VersionSimpleDTO> versions = versionService
        .getVersionByEntityId(param.getEntityId(), param.getEntityType(), param.getName(), appId);
    Preconditions.checkArgument(CollectionUtils.isEmpty(versions), "该版本名称已存在");

    // 新建版本
    VersionSimpleDTO versionSimpleDTO = BeanConvertUtils.convert(param, VersionSimpleDTO.class);
    UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
    UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);

    versionSimpleDTO.setAppId(appId)
        .setCurrentUsing(false)
        .setPreset(false)
        .setCreator(currentUser)
        .setUpdater(currentUser)
        .setVersionSource(VersionSourceEnum.MANUAL.getType());

    return versionService.createVersion(versionSimpleDTO);
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public Integer deleteVersion(Long versionId, Long entityId, Integer entityType) {
    Preconditions.checkArgument(null != versionId, "版本ID不能为空");
    Preconditions.checkArgument(null != entityId && null != entityType, "关联元素不能为空");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "产品信息不能为空");

    paramBindHelper.deleteParamBinds(appId, Collections.singleton(entityId), entityType, versionId);
    return versionService.deleteVersion(versionId);
  }

}
