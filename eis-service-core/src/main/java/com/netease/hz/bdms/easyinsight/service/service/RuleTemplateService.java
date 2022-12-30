package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.param.paramvalue.RuleTemplateSimpleDTO;

import java.util.List;

public interface RuleTemplateService {


  List<RuleTemplateSimpleDTO> getAllRuleTemplate();

  void add(RuleTemplateSimpleDTO dto);

  void delete(Long id);
}
