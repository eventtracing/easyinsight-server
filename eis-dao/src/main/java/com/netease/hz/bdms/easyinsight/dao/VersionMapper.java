package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.Version;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VersionMapper {
//
//  /**
//   * 根据名称获取模板对象
//   *
//   * @param name  模板对象名称，必填参数
//   * @param appId 产品ID，必填参数
//   * @return 模板对象集合
//   */
//  List<Version> selectByName(@Param("name") String name, @Param("appId") Long appId);

  List<Version> selectByEntityId(@Param("entityId") Long entityId,
      @Param("entityType") Integer entityType,
      @Param("name") String name,
      @Param("appId") Long appId);


  Integer selectSizeByEntityId(@Param("entityId") Long entityId,
      @Param("entityType") Integer entityType,
      @Param("name") String name,
      @Param("appId") Long appId);

  /**
   * 根据主键ID获取版本对象
   *
   * @param id 主键ID，必填参数
   * @return 版本对象，必填参数
   */
  Version selectByPrimaryKey(Long id);

  /**
   * 插入版本对象
   *
   * @param param 版本对象，必填参数
   */
  Long insert(Version param);

  /**
   * 更新版本对象
   *
   * @param param 版本对象，必填参数
   */
  Integer update(Version param);

  /**
   * 删除版本对象
   *
   * @param id 版本对象ID，必填参数
   */
  Integer delete(Long id);

  Integer deleteByAppId(Long appId);

  Integer deleteVersion(@Param("entityId") Long entityId, @Param("entityType") Integer entityType,
      @Param("appId") Long appId);


  Integer searchSizeByEntityId(@Param("entityId") Long entityId,
      @Param("entityType") Integer entityType,
      @Param("search") String search,
      @Param("appId") Long appId);

  List<Version> searchByEntityId(@Param("entityId") Long entityId,
      @Param("entityType") Integer entityType,
      @Param("search") String search,
      @Param("appId") Long appId,
      @Param("orderBy") String orderBy, @Param("orderRule") String orderRule,
      @Param("offset") Integer offset, @Param("count") Integer count);

  void setVersion(@Param("appId") Long appId, @Param("entityId") Long entityId,
      @Param("entityType") Integer entityType, @Param("versionId") Long versionId,
      @Param("currentUsing") Boolean currentUsing);

  List<Version> selectVersionByEntityId(@Param("appId") Long appId,
      @Param("entityIds") Collection<Long> entityIds, @Param("entityTypes") Collection<Integer> entityTypes,
      @Param("currentUsing") Boolean currentUsing);

  List<Version> selectVersion(@Param("appId") Long appId, @Param("ids") Collection<Long> ids,
      Collection<Integer> entityTypes);
}
