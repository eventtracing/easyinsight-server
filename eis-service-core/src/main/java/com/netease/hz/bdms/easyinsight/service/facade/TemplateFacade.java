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
    // 验证参数
    Preconditions.checkArgument(null != param, "模板对象不能为空");
    Preconditions.checkArgument(StringUtils.isNotBlank(param.getName()), "模板名称不能为空");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "未指定产品信息");
    AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
    Preconditions.checkArgument(null != appSimpleDTO, "该产品不存在");

    // 验证当前模板是否已存在
    TemplateSimpleDTO existsTemplate = templateService.getTemplateByName(param.getName(), appId);
    Preconditions.checkArgument(null == existsTemplate, param.getName() + "已存在");

    // 插入记录
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
      throw new DomainException("该模板已存在，创建失败");
    }

    // 参数绑定
    paramBindHelper
        .createParamBind(param.getBinds(), appId, templateId, EntityTypeEnum.TEMPLATE.getType(),
            null, currentUser, currentUser);
    return templateId;
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public Integer updateTemplate(TerminalUpdateParam param) {
    // 验证参数
    Preconditions.checkArgument(null != param, "模板对象不能为空");
    Preconditions.checkArgument(null != param.getId(), "模板ID不能为空");
    Preconditions.checkArgument(StringUtils.isNotBlank(param.getName()), "模板名称不能为空");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "未指定产品信息");

    Long templateId = param.getId();
    TemplateSimpleDTO existsTemplate = templateService.getTemplateById(templateId);
    Preconditions.checkArgument(null != existsTemplate, "该模板不存在，修改失败");
    Preconditions.checkArgument(appId == existsTemplate.getAppId(), "未指定产品信息或该模板不在该产品下，，修改失败");

    // 插入记录
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
      throw new DomainException("该模板已存在，修改失败");
    }

    Integer entityType = EntityTypeEnum.TEMPLATE.getType();
    // 删除旧的参数绑定
    paramBindHelper.deleteParamBinds(appId, Collections.singleton(templateId), entityType, null);
    // 参数绑定
    if(CollectionUtils.isNotEmpty(param.getBinds())) {
      paramBindHelper
          .createParamBind(param.getBinds(), appId, templateId, EntityTypeEnum.TEMPLATE.getType(),
              null, existsTemplate.getCreator(), currentUser);
    }

    return updateNum;
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public Integer deleteTemplate(Long templateId) {
    Preconditions.checkArgument(null != templateId, "模板ID不能为空");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "未指定产品信息");

    paramBindHelper.deleteParamBinds(appId, Collections.singleton(templateId), EntityTypeEnum.TEMPLATE.getType(), null);
    return templateService.deleteTemplate(templateId);
  }

  public PagingResultDTO<TemplateSimpleDTO> listTemplates(String search,
      PagingSortDTO pagingSortDTO) {
    // 验证参数
    Preconditions.checkArgument(null != pagingSortDTO, "分页不能为空");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "未指定产品信息");

    // 获取大小
    Integer totalNum = templateService.searchTemplateSize(search, appId);
    // 获取分页明细
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
    // 验证参数
    Preconditions.checkArgument(null != id, "模板ID不能为空");
    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "未指定产品信息");

    // 获取数据
    TemplateSimpleDTO appSimpleDTO = templateService.getTemplateById(id);
    Preconditions.checkArgument(null != appSimpleDTO, "模板不存在");

    List<ParamBindItemDTO> paramBindItemDTOS = paramBindHelper
        .getParamBinds(appId, id, EntityTypeEnum.TEMPLATE.getType(), null);
    TemplateDTO appDTO = BeanConvertUtils.convert(appSimpleDTO, TemplateDTO.class);
    appDTO.setBinds(paramBindItemDTOS);
    return appDTO;
  }

  public TemplateDTO getDefaultTemplate(Long appId) {
    // 验证参数
    Preconditions.checkArgument(null != appId, "未指定产品信息");

    // 获取数据
    //TemplateSimpleDTO appSimpleDTO = templateService.getTemplateByAppId(appId);
    //Preconditions.checkArgument(null != appSimpleDTO, "模板不存在");

//    List<ParamBindItemDTO> paramBindItemDTOS = paramBindHelper
//            .getParamBinds(appId, id, EntityTypeEnum.TEMPLATE.getType(), null);
//    TemplateDTO appDTO = BeanConvertUtils.convert(appSimpleDTO, TemplateDTO.class);
//    appDTO.setBinds(paramBindItemDTOS);
    return null;
  }
}
