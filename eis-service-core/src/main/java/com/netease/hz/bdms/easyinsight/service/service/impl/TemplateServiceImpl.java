package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.template.TemplateSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.TemplateMapper;
import com.netease.hz.bdms.easyinsight.dao.model.Template;
import com.netease.hz.bdms.easyinsight.service.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TemplateServiceImpl implements TemplateService {
    @Autowired
    private TemplateMapper templateMapper;

    private static final Map<String, String> orderByMap = ImmutableMap
            .of("createTime", "create_time", "updateTime", "update_time");
    private static final Map<String, String> orderRuleMap = ImmutableMap
      .of("descend", "desc", "ascend", "asc");

  private TemplateSimpleDTO do2Dto(Template template) {
    TemplateSimpleDTO templateSimpleDTO = BeanConvertUtils.convert(template, TemplateSimpleDTO.class);
    if(null != templateSimpleDTO) {
      UserSimpleDTO updater = new UserSimpleDTO(template.getUpdateEmail(), template.getUpdateName());
      UserSimpleDTO creator = new UserSimpleDTO(template.getCreateEmail(), template.getCreateName());

      templateSimpleDTO.setCreator(creator)
          .setUpdater(updater);
    }
    return templateSimpleDTO;
  }

  private Template dto2Do(TemplateSimpleDTO templateSimpleDTO) {
    Template template = BeanConvertUtils.convert(templateSimpleDTO, Template.class);
    if (template != null) {
      UserSimpleDTO updater = templateSimpleDTO.getUpdater();
      UserSimpleDTO creator = templateSimpleDTO.getCreator();

      if (creator != null) {
        template.setCreateEmail(creator.getEmail())
            .setCreateName(creator.getUserName());
      }
      if (updater != null) {
        template.setUpdateEmail(updater.getEmail())
            .setUpdateName(updater.getUserName());
      }
    }
    return template;
  }

  @Override
  public TemplateSimpleDTO getTemplateByName(String name, Long appId) {
    Preconditions.checkArgument(null != appId, "??????ID????????????");
    Preconditions.checkArgument(StringUtils.isNotBlank(name), "?????????????????????");

    // ?????????appId???????????????????????????
    List<Template> templates  = templateMapper.selectByName(name, appId);
    if(CollectionUtils.isNotEmpty(templates)) {
      return do2Dto(templates.get(0));
    }
    return null;
  }

  @Override
  public Long createTemplate(TemplateSimpleDTO templateSimpleDTO) {
    Template template = dto2Do(templateSimpleDTO);
    Preconditions.checkArgument(null != template, "????????????????????????");

    templateMapper.insert(template);
    return template.getId();
  }

  @Override
  public Integer updateTemplate(TemplateSimpleDTO templateSimpleDTO) {
    Template template = dto2Do(templateSimpleDTO);
    Preconditions.checkArgument(null != template, "????????????????????????");
    Preconditions.checkArgument(null != template.getId(), "??????ID????????????");

    return templateMapper.update(template);
  }

  @Override
  public Integer deleteTemplate(Long templateId) {
    Preconditions.checkArgument(null != templateId, "??????ID????????????");
    return templateMapper.delete(templateId);
  }

  @Override
  public Integer searchTemplateSize(String search, Long appId) {
    return templateMapper.searchTemplateSize(search, appId);
  }

  @Override
  public List<TemplateSimpleDTO> searchTemplate(String search, Long appId, String orderBy,
      String orderRule, Integer offset, Integer pageSize) {
    String dbOrderBy = orderByMap.get(orderBy);
    String dbOrderRule = orderRuleMap.get(orderRule);

    List<Template> templates = templateMapper.searchTemplate(search, appId, dbOrderBy, dbOrderRule, offset, pageSize);
    return templates.stream().map(this::do2Dto).collect(Collectors.toList());
  }

  @Override
  public TemplateSimpleDTO getTemplateById(Long templateId) {
    Preconditions.checkArgument(null != templateId, "??????ID????????????");

    Template template  = templateMapper.selectByPrimaryKey(templateId);
    return do2Dto(template);
  }
}
