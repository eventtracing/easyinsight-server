package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.Domain;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DomainMapper {

  /**
   * 根据名称获取域对象
   * @param name 域名称
   * @return 域对象集合
   */
  List<Domain> selectByCode(String code);

  /**
   * 根据域ID获取域对象
   * @param id 域ID
   * @return 域对象
   */
  Domain selectByPrimaryKey(Long id);

  /**
   * 插入域记录
   * @param domain 域对象
   */
  Integer insert(Domain domain);

  /**
   * 修改域记录
   * @param domain 域对象
   */
  Integer update(Domain domain);

  /**
   * 删除域记录
   * @param id 域对象ID
   */
  Integer delete(Long id);

  List<Domain> searchDomain(@Param("search") String search,
                            @Param("orderBy") String orderBy,
                            @Param("orderRule")  String orderRule);
}
