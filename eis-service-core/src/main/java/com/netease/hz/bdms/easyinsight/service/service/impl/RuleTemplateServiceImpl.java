package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.paramvalue.RuleTemplateSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.RuleTemplateMapper;
import com.netease.hz.bdms.easyinsight.dao.model.RuleTemplate;
import com.netease.hz.bdms.easyinsight.service.service.RuleTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RuleTemplateServiceImpl implements RuleTemplateService {
    @Autowired
    private RuleTemplateMapper ruleTemplateMapper;


    private RuleTemplateSimpleDTO do2Dto(RuleTemplate ruleTemplate) {
        RuleTemplateSimpleDTO ruleTemplateSimpleDTO = BeanConvertUtils.convert(ruleTemplate, RuleTemplateSimpleDTO.class);
        if (null != ruleTemplateSimpleDTO) {
            UserSimpleDTO updater = new UserSimpleDTO(ruleTemplate.getUpdateEmail(), ruleTemplate.getUpdateName());
            UserSimpleDTO creator = new UserSimpleDTO(ruleTemplate.getCreateEmail(), ruleTemplate.getCreateName());
            Long createTime = ruleTemplate.getCreateTime() != null ? ruleTemplate.getCreateTime().getTime() : null;
            Long updateTime = ruleTemplate.getUpdateTime() != null ? ruleTemplate.getUpdateTime().getTime() : null;
            ruleTemplateSimpleDTO.setCreator(creator)
                    .setUpdater(updater)
                    .setCreateTime(createTime)
                    .setUpdateTime(updateTime);
        }
        return ruleTemplateSimpleDTO;
    }

    private RuleTemplate dto2Do(RuleTemplateSimpleDTO ruleTemplateSimpleDTO) {
        RuleTemplate ruleTemplate = BeanConvertUtils.convert(ruleTemplateSimpleDTO, RuleTemplate.class);
        if (ruleTemplate != null) {
            UserSimpleDTO updater = ruleTemplateSimpleDTO.getUpdater();
            UserSimpleDTO creator = ruleTemplateSimpleDTO.getCreator();
            Timestamp createTime = ruleTemplateSimpleDTO.getCreateTime() != null ? new Timestamp(ruleTemplateSimpleDTO.getCreateTime()) : new Timestamp(System.currentTimeMillis());
            Timestamp updateTime = ruleTemplateSimpleDTO.getUpdateTime() != null ? new Timestamp(ruleTemplateSimpleDTO.getUpdateTime()) : new Timestamp(System.currentTimeMillis());
            if (creator != null) {
                ruleTemplate.setCreateEmail(creator.getEmail())
                        .setCreateName(creator.getUserName());
            }
            if (updater != null) {
                ruleTemplate.setUpdateEmail(updater.getEmail())
                        .setUpdateName(updater.getUserName());
            }
            ruleTemplate.setCreateTime(createTime)
                    .setUpdateTime(updateTime);
        }
        return ruleTemplate;
    }


    @Override
    public List<RuleTemplateSimpleDTO> getAllRuleTemplate() {
        List<RuleTemplate> ruleTemplates = ruleTemplateMapper.selectAll();
        return ruleTemplates.stream().map(this::do2Dto).collect(Collectors.toList());
    }

    @Override
    public void add(RuleTemplateSimpleDTO dto) {
        if (dto == null) {
            return;
        }
        ruleTemplateMapper.insert(dto2Do(dto));
    }

    @Override
    public void delete(Long id) {
        ruleTemplateMapper.delete(id);
    }
}
