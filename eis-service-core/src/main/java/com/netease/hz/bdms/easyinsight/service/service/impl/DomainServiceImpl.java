package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.github.pagehelper.PageHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.domain.DomainSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.DomainMapper;
import com.netease.hz.bdms.easyinsight.dao.model.Domain;
import com.netease.hz.bdms.easyinsight.service.service.DomainService;
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
public class DomainServiceImpl implements DomainService {

    @Autowired
    private DomainMapper domainMapper;

    private static Map<String, String> orderByMap = ImmutableMap
            .of("createTime", "create_time", "updateTime", "update_time");
    private static Map<String, String> orderRuleMap = ImmutableMap.of("descend", "desc", "ascend", "asc");


    private DomainSimpleDTO do2SimpleDTO(Domain domain) {
        DomainSimpleDTO domainSimpleDTO = BeanConvertUtils.convert(domain, DomainSimpleDTO.class);
        if (domainSimpleDTO != null) {
            UserSimpleDTO creator = new UserSimpleDTO(domain.getCreateEmail(), domain.getCreateName());
            UserSimpleDTO updater = new UserSimpleDTO(domain.getUpdateEmail(), domain.getUpdateName());
            UserSimpleDTO owner = new UserSimpleDTO(domain.getOwnerEmail(), domain.getOwnerName());

            domainSimpleDTO.setCreator(creator)
                    .setUpdater(updater)
                    .setOwner(owner);
        }
        return domainSimpleDTO;
    }

    private Domain dto2Do(DomainSimpleDTO domainSimpleDTO) {
        Domain domain = BeanConvertUtils.convert(domainSimpleDTO, Domain.class);
        if (domain != null) {
            UserSimpleDTO creator = domainSimpleDTO.getCreator();
            UserSimpleDTO updater = domainSimpleDTO.getUpdater();
            if (null != creator) {
                domain.setCreateEmail(domainSimpleDTO.getCreator().getEmail())
                        .setCreateName(domainSimpleDTO.getCreator().getUserName());
            }
            if (null != updater) {
                domain.setUpdateEmail(domainSimpleDTO.getUpdater().getEmail())
                        .setUpdateName(domainSimpleDTO.getUpdater().getUserName());
            }
        }
        return domain;
    }

    @Override
    public DomainSimpleDTO getDomainByCode(String code) {
        if (StringUtils.isNotBlank(code)) {
            List<Domain> domains = domainMapper.selectByCode(code);
            if (CollectionUtils.isNotEmpty(domains)) {
                return do2SimpleDTO(domains.get(0));
            }
        }
        return null;
    }

    @Override
    public List<DomainSimpleDTO> searchDomain(String search,
                                              String orderBy,
                                              String orderRule,
                                              Integer currentPage,
                                              Integer pageSize) {

        String dbOrderBy = orderByMap.get(orderBy);
        String dbOrderRule = orderRuleMap.get(orderRule);

        // 设置分页参数
        PageHelper.startPage(currentPage, pageSize);

        List<Domain> domains = domainMapper.searchDomain(search, dbOrderBy, dbOrderRule);
        return domains.stream().map(this::do2SimpleDTO).collect(Collectors.toList());
    }

    @Override
    public DomainSimpleDTO getDomainById(Long domainId) {
        if (domainId != null) {
            return do2SimpleDTO(domainMapper.selectByPrimaryKey(domainId));
        }
        return null;
    }

    @Override
    public Long getDomainIdByCode(String code) {
        Preconditions.checkArgument(StringUtils.isNotBlank(code), "域ID不能为空");
        List<Domain> domains = domainMapper.selectByCode(code);
        if (CollectionUtils.isNotEmpty(domains)) {
            return domains.get(0).getId();
        }
        return null;
    }

    @Override
    public Long createDomain(DomainSimpleDTO domainSimpleDTO) {
        Domain domain = dto2Do(domainSimpleDTO);
        if (domain == null) {
            throw new CommonException("域对象不能为空");
        }
        domainMapper.insert(domain);
        return domain.getId();
    }

    @Override
    public Integer updateDomain(DomainSimpleDTO domainSimpleDTO) {
        Domain domain = dto2Do(domainSimpleDTO);
        Preconditions.checkArgument(null != domain && null != domain.getId(), "域对象不能为空");
        return domainMapper.update(domain);
    }

    @Override
    public Integer deleteDomain(Long domainId) {
        Preconditions.checkArgument(null != domainId, "域ID不能为空");
        return domainMapper.delete(domainId);
    }
}
