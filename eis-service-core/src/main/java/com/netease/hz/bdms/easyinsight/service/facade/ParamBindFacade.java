package com.netease.hz.bdms.easyinsight.service.facade;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.app.AppSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingResultDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindValueSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.version.VersionSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.VersionSourceEnum;
import com.netease.hz.bdms.easyinsight.common.exception.ParamBindException;
import com.netease.hz.bdms.easyinsight.common.param.param.paramBind.ParamBindCopyParam;
import com.netease.hz.bdms.easyinsight.common.param.param.paramBind.ParamBindCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.param.paramBind.ParamBindItermParam;
import com.netease.hz.bdms.easyinsight.common.param.param.paramBind.ParamBindUpdateParam;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.service.service.AppService;
import com.netease.hz.bdms.easyinsight.service.service.ParamBindService;
import com.netease.hz.bdms.easyinsight.service.service.ParamBindValueService;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Component
public class ParamBindFacade {

    @Autowired
    private ParamBindService paramBindService;
    @Autowired
    private ParamBindValueService paramBindValueService;
  @Autowired
  private VersionService versionService;
  @Autowired
  private AppService appService;
  @Autowired
  private ParamBindHelper paramBindHelper;

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public void createParamBind(ParamBindCreateParam param) {
    // 验证参数
    Preconditions.checkArgument(null != param, "参数绑定不能为空");
    Preconditions.checkArgument(null != param.getEntityId() && null != param.getEntityType(),
        "参数绑定关联元素信息不能为空");
    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "未指定产品信息");
    AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
    Preconditions.checkArgument(null != appSimpleDTO, "该产品不存在");

    UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
    UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);

    // 检查版本是否存在
    // 判断是否存在同名版本
    Integer versionSize = versionService
        .getVersionSizeByEntityId(param.getEntityId(), param.getEntityType(), param.getVersion(),
            appId);
    Preconditions.checkArgument(versionSize <= 0, "该版本名称已存在");

    // 处理版本
    VersionSimpleDTO versionSimpleDTO = new VersionSimpleDTO();
    versionSimpleDTO.setAppId(appId)
        .setCreator(currentUser)
        .setUpdater(currentUser)
        .setCurrentUsing(false)
        .setPreset(false)
        .setVersionSource(VersionSourceEnum.MANUAL.getType())
        .setName(param.getVersion())
        .setEntityId(param.getEntityId())
        .setEntityType(param.getEntityType());

    Long versionId = versionService.createVersion(versionSimpleDTO);

    // 验证当前参数绑定是否已存在
    Integer existsParamBindSize = paramBindService
        .getParamBindSizeByEntityIds(Collections.singletonList(param.getEntityId()),
            Collections.singletonList(param.getEntityType()), versionId, appId);
    Preconditions.checkArgument(existsParamBindSize <= 0, "该参数绑定已存在，创建失败");

    // 插入记录
    paramBindHelper
        .createParamBind(param.getParamBinds(), appId, param.getEntityId(), param.getEntityType(),
            versionId, currentUser, currentUser);

  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public void updateParamBind(ParamBindUpdateParam param) {
    // 验证参数
    Preconditions.checkArgument(null != param, "参数绑定不能为空");
    Preconditions.checkArgument(null != param.getEntityId() && null != param.getEntityType(),
        "参数绑定关联元素信息不能为空");
    Preconditions
        .checkArgument(StringUtils.isNotBlank(param.getVersion()) && null != param.getVersionId(),
            "版本信息不能为空");

    // 验证当前参数绑定是否已存在
    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "未指定产品信息");

    UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
    UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);

    Long versionId = param.getVersionId();  // 版本ID
    Long entityId = param.getEntityId();    //  实体ID
    Integer entityType = param.getEntityType();  // 实体类型
    String versionName = param.getVersion();   // 版本名称

    // 检查版本是否存在
    // 判断是否存在同名版本
    List<VersionSimpleDTO> versions = versionService
        .getVersionByEntityId(entityId, entityType, versionName, appId);
    if (CollectionUtils.isNotEmpty(versions)) {
      for (VersionSimpleDTO version : versions) {
        if (!versionId.equals(version.getId())) {
          throw new ParamBindException(versionName+"已存在");
        }
      }
    }

    // 修改版本
    VersionSimpleDTO versionSimpleDTO = versionService.getVersionById(versionId);
    Preconditions.checkArgument(null != versionSimpleDTO, "当前版本不存在，绑定失败");
    versionSimpleDTO.setName(param.getVersion())
        .setUpdater(currentUser);
    versionService.updateVersion(versionSimpleDTO);

    // 找到之前参数绑定的创建人
    List<ParamBindSimpleDTO> paramBinds = paramBindService
        .getByEntityIds(Collections.singletonList(param.getEntityId()),
            Collections.singletonList(param.getEntityType()), param.getVersionId(), appId);
    UserSimpleDTO creator = currentUser;
    if (CollectionUtils.isNotEmpty(paramBinds)) {
      creator = paramBinds.get(0).getCreator();
    }

    // 删除旧的参数绑定
    paramBindHelper.deleteParamBinds(appId, Collections.singletonList(param.getEntityId()),
        param.getEntityType(), versionId);
    // 参数绑定
    if (CollectionUtils.isNotEmpty(param.getParamBinds())) {
      paramBindHelper.createParamBind(param.getParamBinds(), appId, param.getEntityId(),
              param.getEntityType(), versionId, creator, currentUser);
    }
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public Integer deleteParamBind(Long paramBindId) {
    Preconditions.checkArgument(null != paramBindId, "参数绑定ID不能为空");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "未指定产品信息");

    paramBindValueService.deleteByBindId(paramBindId);

    return paramBindService.deleteParamBind(paramBindId);
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public void copyParamBind(ParamBindCopyParam param) {
    Preconditions.checkArgument(null != param, "参数不能为空");
    Preconditions.checkArgument(null != param.getEntityId() && null != param.getEntityType(),
        "参数绑定的实体信息不能为空");
    Preconditions.checkArgument(null != param.getVersionId(), "参考的版本不能为空");
    Preconditions.checkArgument(StringUtils.isNotBlank(param.getName()), "当前版本不能为空");
    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "未指定产品信息");

    Long entityId = param.getEntityId();
    Integer entityType = param.getEntityType();
    Long oldVersionId = param.getVersionId();
    String newVersionName = param.getName();

    UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
    UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);

    // 检查版本是否存在
    // 判断是否存在同名版本
    Integer versionSize = versionService
        .getVersionSizeByEntityId(entityId, entityType, param.getName(),
            appId);
    Preconditions.checkArgument(versionSize <= 0, "该版本名称已存在");

    // 处理版本
    VersionSimpleDTO versionSimpleDTO = new VersionSimpleDTO();
    versionSimpleDTO.setAppId(appId)
        .setCreator(currentUser)
        .setUpdater(currentUser)
        .setCurrentUsing(false)
        .setPreset(false)
        .setVersionSource(VersionSourceEnum.MANUAL.getType())
        .setName(newVersionName)
        .setEntityId(entityId)
        .setEntityType(entityType);

    Long newVersionId = versionService.createVersion(versionSimpleDTO);

    // 验证当前参数绑定是否已存在
    Integer existsParamBindSize = paramBindService
        .getParamBindSizeByEntityIds(Collections.singletonList(param.getEntityId()),
            Collections.singletonList(param.getEntityType()), newVersionId, appId);
    Preconditions.checkArgument(existsParamBindSize <= 0, "该参数绑定已存在，创建失败");

    // 获取现有数据
    // 获取参数绑定集合
    List<ParamBindSimpleDTO> paramBindSimpleDTOS = paramBindService
        .getByEntityIds(Collections.singleton(entityId), Collections.singleton(entityType),
            oldVersionId, appId);
    Set<Long> paramBindIds = paramBindSimpleDTOS.stream()
            .map(ParamBindSimpleDTO::getId)
            .collect(Collectors.toSet());

    // 获取参数绑定值集合
    List<ParamBindValueSimpleDTO> paramBindValues = paramBindValueService
        .getByBindIds(paramBindIds);
    Map<Long, List<Long>> paramBindId2ValueIdMap = Maps.newHashMap();
    if (CollectionUtils.isNotEmpty(paramBindValues)) {
      for (ParamBindValueSimpleDTO paramBindValue : paramBindValues) {
        List<Long> tmpBindValues = paramBindId2ValueIdMap
            .computeIfAbsent(paramBindValue.getBindId(), k -> Lists.newArrayList());
        tmpBindValues.add(paramBindValue.getParamValueId());
      }
    }

    // 构建参数绑定接口
    List<ParamBindItermParam> paramBinds = Lists.newArrayList();
    if (CollectionUtils.isNotEmpty(paramBindSimpleDTOS)) {
      for (ParamBindSimpleDTO paramBindSimpleDTO : paramBindSimpleDTOS) {
        Long paramBindId = paramBindSimpleDTO.getId();
        ParamBindItermParam bindIterm = BeanConvertUtils
            .convert(paramBindSimpleDTO, ParamBindItermParam.class);
        if (bindIterm != null) {
          bindIterm.setValues(paramBindId2ValueIdMap.get(paramBindId));
          paramBinds.add(bindIterm);
        }
      }
    }

    // 插入记录
    paramBindHelper
        .createParamBind(paramBinds, appId, entityId, entityType, newVersionId, currentUser,
            currentUser);
  }

  public PagingResultDTO<ParamBindItemDTO> listParamBinds(Long entityId, Integer entityType,
      Long versionId,
      PagingSortDTO pagingSortDTO) {
    // 验证参数
    Preconditions.checkArgument(null != pagingSortDTO, "分页不能为空");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "未指定产品信息");

    // 获取大小
    Integer totalNum = paramBindService.searchParamBindSize(entityId, entityType, versionId, appId);
    List<ParamBindItemDTO> items = paramBindHelper
        .getParamBinds(appId, entityId, entityType, versionId);

    PagingResultDTO<ParamBindItemDTO> result = new PagingResultDTO<>();
    result.setTotalNum(totalNum)
        .setPageNum(pagingSortDTO.getCurrentPage())
        .setList(items);
    return result;
  }

  public List<ParamBindItemDTO> getParamBinds(Long entityId, Integer entityType,
      Long versionId) {
    Preconditions.checkArgument(null != entityId && null != entityType, "关联元素不能为空");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "未指定产品信息");

    return paramBindHelper.getParamBinds(appId, entityId, entityType, versionId);
  }

}
