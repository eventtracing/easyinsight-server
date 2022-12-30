package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.ParamPoolItem;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ParamPoolMapper {

  /**
   * 根据名称获取参数池item对象 目前主要用于存储对象业务私参
   *
   * @param code  参数名
   * @param appId 产品ID
   * @return 参数池item集合
   */
  List<ParamPoolItem> selectByCode(@Param("code") String code, @Param("appId") Long appId);

  List<ParamPoolItem> selectByAppId(@Param("appId") Long appId);

  Integer selectSizeByCode(@Param("code") String code, @Param("appId") Long appId);

  /**
   * 根据主键ID获取参数池item对象 目前主要用于存储对象业务私参
   *
   * @param id 主键ID
   * @return 参数池item对象
   */
  ParamPoolItem selectByPrimaryKey(Long id);

  /**
   * 插入参数池item对象
   *
   * @param param 参数池item对象
   */
  Integer insert(ParamPoolItem param);

  /**
   * 更新参数池item对象
   *
   * @param param 参数池item对象
   */
  Integer update(ParamPoolItem param);

  /**
   * 删除参数池item对象
   *
   * @param id 主键ID
   */
  Integer delete(Long id);


  Integer searchParamPoolItemSize(@Param("search") String search,
      @Param("appId") Long appId);

  List<ParamPoolItem> searchParamPoolItems(@Param("search") String search,
      @Param("appId") Long appId,
      @Param("orderBy") String orderBy, @Param("orderRule") String orderRule,
      @Param("offset") Integer offset, @Param("count") Integer count);

}
