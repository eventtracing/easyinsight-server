package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.template.TemplateSimpleDTO;

import java.util.List;

public interface TemplateService {

  TemplateSimpleDTO getTemplateByName(String name, Long appId);

  Long createTemplate(TemplateSimpleDTO templateSimpleDTO);

  Integer updateTemplate(TemplateSimpleDTO templateSimpleDTO);

  Integer deleteTemplate(Long templateId);

  Integer searchTemplateSize(String search, Long appId);

  List<TemplateSimpleDTO> searchTemplate(String search, Long appId, String orderBy, String orderRule,
      Integer offset, Integer pageSize);

  TemplateSimpleDTO getTemplateById(Long templateId);
}
