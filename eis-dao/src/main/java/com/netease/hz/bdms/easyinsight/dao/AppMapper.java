package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.App;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface AppMapper {


  /**
   * 根据主键ID获取产品对象
   *
   * @param id 主键ID
   * @return 域对象
   */
  App selectByPrimaryKey(Long id);

  /**
   * 根据域主键ID获取对应的产品对象集合
   * @param domainId 域主键ID
   * @return 产品对象集合
   */
  List<App> selectByDomainId(Long domainId);

  /**
   * 根据域主键ID获取对应的产品数目
   * @param domainId 域主键ID
   * @return 产品数目
   */
  Integer selectSizeByDomainId(Long domainId);

  List<App> selectByIds(@Param("ids") Set<Long> ids);

  Integer searchAppSize(@Param("search") String search, @Param("domainId") Long domainId);

  List<App> searchApp(@Param("search") String search, @Param("domainId") Long domainId,
      @Param("orderBy") String orderBy, @Param("orderRule") String orderRule,
      @Param("offset") Integer offset, @Param("count") Integer count);

  /**
   * 插入产品记录
   * @param app 产品对象
   */
  Integer insert(App app);

  /**
   * 修改产品记录
   * @param app 产品对象
   */
  Integer update(App app);

  /**
   * 删除产品
   * @param id 主键ID
   */
  Integer delete(Long id);
}
