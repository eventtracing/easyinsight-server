package com.netease.hz.bdms.easyinsight.service.facade;

import com.google.common.base.Preconditions;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.app.AppSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingResultDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parambind.ParamBindItemDTO;
import com.netease.hz.bdms.easyinsight.common.dto.template.TemplateDTO;
import com.netease.hz.bdms.easyinsight.common.dto.template.TemplateSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.EntityTypeEnum;
import com.netease.hz.bdms.easyinsight.common.exception.DomainException;
import com.netease.hz.bdms.easyinsight.common.param.template.TemplateCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.template.TerminalUpdateParam;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.service.service.AppService;
import com.netease.hz.bdms.easyinsight.service.service.TemplateService;
import com.netease.hz.bdms.easyinsight.service.helper.ParamBindHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;


@Slf4j
@Component
public class TemplateFacade {

    @Autowired
    private TemplateService templateService;
    @Autowired
    private AppService appService;
  @Autowired
  private ParamBindHelper paramBindHelper;

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public Long createTemplate(TemplateCreateParam param) {
    // ????????????
    Preconditions.checkArgument(null != param, "????????????????????????");
    Preconditions.checkArgument(StringUtils.isNotBlank(param.getName()), "????????????????????????");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "?????????????????????");
    AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
    Preconditions.checkArgument(null != appSimpleDTO, "??????????????????");

    // ?????????????????????????????????
    TemplateSimpleDTO existsTemplate = templateService.getTemplateByName(param.getName(), appId);
    Preconditions.checkArgument(null == existsTemplate, param.getName() + "?????????");

    // ????????????
    TemplateSimpleDTO templateSimpleDTO = BeanConvertUtils.convert(param, TemplateSimpleDTO.class);
    templateSimpleDTO.setAppId(appId);

    UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
    UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
    templateSimpleDTO.setCreator(currentUser)
        .setUpdater(currentUser);

    Long templateId = null;
    try {
      templateId = templateService.createTemplate(templateSimpleDTO);
    } catch (DuplicateKeyException e) {
      log.debug("", e);
      throw new DomainException("?????????????????????????????????");
    }

    // ????????????
    paramBindHelper
        .createParamBind(param.getBinds(), appId, templateId, EntityTypeEnum.TEMPLATE.getType(),
            null, currentUser, currentUser);
    return templateId;
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public Integer updateTemplate(TerminalUpdateParam param) {
    // ????????????
    Preconditions.checkArgument(null != param, "????????????????????????");
    Preconditions.checkArgument(null != param.getId(), "??????ID????????????");
    Preconditions.checkArgument(StringUtils.isNotBlank(param.getName()), "????????????????????????");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "?????????????????????");

    Long templateId = param.getId();
    TemplateSimpleDTO existsTemplate = templateService.getTemplateById(templateId);
    Preconditions.checkArgument(null != existsTemplate, "?????????????????????????????????");
    Preconditions.checkArgument(appId == existsTemplate.getAppId(), "?????????????????????????????????????????????????????????????????????");

    // ????????????
    TemplateSimpleDTO templateSimpleDTO = BeanConvertUtils.convert(param, TemplateSimpleDTO.class);
    templateSimpleDTO.setAppId(appId);

    UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
    UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
    templateSimpleDTO.setUpdater(currentUser);

    Integer updateNum = 0;
    try {
      updateNum = templateService.updateTemplate(templateSimpleDTO);
    } catch (DuplicateKeyException e) {
      log.debug("", e);
      throw new DomainException("?????????????????????????????????");
    }

    Integer entityType = EntityTypeEnum.TEMPLATE.getType();
    // ????????????????????????
    paramBindHelper.deleteParamBinds(appId, Collections.singleton(templateId), entityType, null);
    // ????????????
    if(CollectionUtils.isNotEmpty(param.getBinds())) {
      paramBindHelper
          .createParamBind(param.getBinds(), appId, templateId, EntityTypeEnum.TEMPLATE.getType(),
              null, existsTemplate.getCreator(), currentUser);
    }

    return updateNum;
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public Integer deleteTemplate(Long templateId) {
    Preconditions.checkArgument(null != templateId, "??????ID????????????");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "?????????????????????");

    paramBindHelper.deleteParamBinds(appId, Collections.singleton(templateId), EntityTypeEnum.TEMPLATE.getType(), null);
    return templateService.deleteTemplate(templateId);
  }

  public PagingResultDTO<TemplateSimpleDTO> listTemplates(String search,
      PagingSortDTO pagingSortDTO) {
    // ????????????
    Preconditions.checkArgument(null != pagingSortDTO, "??????????????????");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "?????????????????????");

    // ????????????
    Integer totalNum = templateService.searchTemplateSize(search, appId);
    // ??????????????????
    List<TemplateSimpleDTO> apps = templateService
        .searchTemplate(search, appId, pagingSortDTO.getOrderBy(), pagingSortDTO.getOrderRule(),
            pagingSortDTO.getOffset(), pagingSortDTO.getPageSize());

    PagingResultDTO<TemplateSimpleDTO> result = new PagingResultDTO<>();
    result.setTotalNum(totalNum)
        .setPageNum(pagingSortDTO.getCurrentPage())
        .setList(apps);
    return result;
  }

  public TemplateDTO getTemplate(Long id) {
    // ????????????
    Preconditions.checkArgument(null != id, "??????ID????????????");
    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "?????????????????????");

    // ????????????
    TemplateSimpleDTO appSimpleDTO = templateService.getTemplateById(id);
    Preconditions.checkArgument(null != appSimpleDTO, "???????????????");

    List<ParamBindItemDTO> paramBindItemDTOS = paramBindHelper
        .getParamBinds(appId, id, EntityTypeEnum.TEMPLATE.getType(), null);
    TemplateDTO appDTO = BeanConvertUtils.convert(appSimpleDTO, TemplateDTO.class);
    appDTO.setBinds(paramBindItemDTOS);
    return appDTO;
  }

}
