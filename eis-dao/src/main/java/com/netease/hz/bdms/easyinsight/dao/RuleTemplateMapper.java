package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.RuleTemplate;

import java.util.List;

public interface RuleTemplateMapper {

    Integer insert(RuleTemplate param);

    List<RuleTemplate> selectAll();

    Integer delete(Long id);
}
