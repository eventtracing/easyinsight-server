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
    // ????????????
    Preconditions.checkArgument(null != param, "????????????????????????");
    Preconditions.checkArgument(null != param.getEntityId() && null != param.getEntityType(),
        "??????????????????????????????????????????");
    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "?????????????????????");
    AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
    Preconditions.checkArgument(null != appSimpleDTO, "??????????????????");

    UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
    UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);

    // ????????????????????????
    // ??????????????????????????????
    Integer versionSize = versionService
        .getVersionSizeByEntityId(param.getEntityId(), param.getEntityType(), param.getVersion(),
            appId);
    Preconditions.checkArgument(versionSize <= 0, "????????????????????????");

    // ????????????
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

    // ???????????????????????????????????????
    Integer existsParamBindSize = paramBindService
        .getParamBindSizeByEntityIds(Collections.singletonList(param.getEntityId()),
            Collections.singletonList(param.getEntityType()), versionId, appId);
    Preconditions.checkArgument(existsParamBindSize <= 0, "???????????????????????????????????????");

    // ????????????
    paramBindHelper
        .createParamBind(param.getParamBinds(), appId, param.getEntityId(), param.getEntityType(),
            versionId, currentUser, currentUser);

  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public void updateParamBind(ParamBindUpdateParam param) {
    // ????????????
    Preconditions.checkArgument(null != param, "????????????????????????");
    Preconditions.checkArgument(null != param.getEntityId() && null != param.getEntityType(),
        "??????????????????????????????????????????");
    Preconditions
        .checkArgument(StringUtils.isNotBlank(param.getVersion()) && null != param.getVersionId(),
            "????????????????????????");

    // ???????????????????????????????????????
    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "?????????????????????");

    UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
    UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);

    Long versionId = param.getVersionId();  // ??????ID
    Long entityId = param.getEntityId();    //  ??????ID
    Integer entityType = param.getEntityType();  // ????????????
    String versionName = param.getVersion();   // ????????????

    // ????????????????????????
    // ??????????????????????????????
    List<VersionSimpleDTO> versions = versionService
        .getVersionByEntityId(entityId, entityType, versionName, appId);
    if (CollectionUtils.isNotEmpty(versions)) {
      for (VersionSimpleDTO version : versions) {
        if (!versionId.equals(version.getId())) {
          throw new ParamBindException(versionName+"?????????");
        }
      }
    }

    // ????????????
    VersionSimpleDTO versionSimpleDTO = versionService.getVersionById(versionId);
    Preconditions.checkArgument(null != versionSimpleDTO, "????????????????????????????????????");
    versionSimpleDTO.setName(param.getVersion())
        .setUpdater(currentUser);
    versionService.updateVersion(versionSimpleDTO);

    // ????????????????????????????????????
    List<ParamBindSimpleDTO> paramBinds = paramBindService
        .getByEntityIds(Collections.singletonList(param.getEntityId()),
            Collections.singletonList(param.getEntityType()), param.getVersionId(), appId);
    UserSimpleDTO creator = currentUser;
    if (CollectionUtils.isNotEmpty(paramBinds)) {
      creator = paramBinds.get(0).getCreator();
    }

    // ????????????????????????
    paramBindHelper.deleteParamBinds(appId, Collections.singletonList(param.getEntityId()),
        param.getEntityType(), versionId);
    // ????????????
    if (CollectionUtils.isNotEmpty(param.getParamBinds())) {
      paramBindHelper.createParamBind(param.getParamBinds(), appId, param.getEntityId(),
              param.getEntityType(), versionId, creator, currentUser);
    }
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public Integer deleteParamBind(Long paramBindId) {
    Preconditions.checkArgument(null != paramBindId, "????????????ID????????????");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "?????????????????????");

    paramBindValueService.deleteByBindId(paramBindId);

    return paramBindService.deleteParamBind(paramBindId);
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public void copyParamBind(ParamBindCopyParam param) {
    Preconditions.checkArgument(null != param, "??????????????????");
    Preconditions.checkArgument(null != param.getEntityId() && null != param.getEntityType(),
        "???????????????????????????????????????");
    Preconditions.checkArgument(null != param.getVersionId(), "???????????????????????????");
    Preconditions.checkArgument(StringUtils.isNotBlank(param.getName()), "????????????????????????");
    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "?????????????????????");

    Long entityId = param.getEntityId();
    Integer entityType = param.getEntityType();
    Long oldVersionId = param.getVersionId();
    String newVersionName = param.getName();

    UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
    UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);

    // ????????????????????????
    // ??????????????????????????????
    Integer versionSize = versionService
        .getVersionSizeByEntityId(entityId, entityType, param.getName(),
            appId);
    Preconditions.checkArgument(versionSize <= 0, "????????????????????????");

    // ????????????
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

    // ???????????????????????????????????????
    Integer existsParamBindSize = paramBindService
        .getParamBindSizeByEntityIds(Collections.singletonList(param.getEntityId()),
            Collections.singletonList(param.getEntityType()), newVersionId, appId);
    Preconditions.checkArgument(existsParamBindSize <= 0, "???????????????????????????????????????");

    // ??????????????????
    // ????????????????????????
    List<ParamBindSimpleDTO> paramBindSimpleDTOS = paramBindService
        .getByEntityIds(Collections.singleton(entityId), Collections.singleton(entityType),
            oldVersionId, appId);
    Set<Long> paramBindIds = paramBindSimpleDTOS.stream()
            .map(ParamBindSimpleDTO::getId)
            .collect(Collectors.toSet());

    // ???????????????????????????
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

    // ????????????????????????
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

    // ????????????
    paramBindHelper
        .createParamBind(paramBinds, appId, entityId, entityType, newVersionId, currentUser,
            currentUser);
  }

  public PagingResultDTO<ParamBindItemDTO> listParamBinds(Long entityId, Integer entityType,
      Long versionId,
      PagingSortDTO pagingSortDTO) {
    // ????????????
    Preconditions.checkArgument(null != pagingSortDTO, "??????????????????");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "?????????????????????");

    // ????????????
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
    Preconditions.checkArgument(null != entityId && null != entityType, "????????????????????????");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "?????????????????????");

    return paramBindHelper.getParamBinds(appId, entityId, entityType, versionId);
  }

}
