package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.domain.DomainSimpleDTO;

import java.util.List;

public interface DomainService {

    /**
     * 根据域ID获取域对象 精确匹配
     *
     * @param code 域ID
     * @return 域对象
     */
    DomainSimpleDTO getDomainByCode(String code);

    DomainSimpleDTO getDomainById(Long domainId);

    List<DomainSimpleDTO> searchDomain(String search,
                                       String orderBy,
                                       String orderRule,
                                       Integer currentPage,
                                       Integer pageSize);


    /**
     * 根据域名称获取域对象ID
     *
     * @param domainName 域名称
     * @return 域对象ID
     */
    Long getDomainIdByCode(String domainName);

    /**
     * 插入域对象
     *
     * @param domainSimpleDTO 域对象
     * @return 插入记录数
     */
    Long createDomain(DomainSimpleDTO domainSimpleDTO);

    /**
     * 修改域对象
     *
     * @param domainSimpleDTO 域对象
     * @return 修改记录数
     */
    Integer updateDomain(DomainSimpleDTO domainSimpleDTO);

    /**
     * 删除域对象
     *
     * @param domainId 域对象ID
     * @return 删除记录数
     */
    Integer deleteDomain(Long domainId);

}
