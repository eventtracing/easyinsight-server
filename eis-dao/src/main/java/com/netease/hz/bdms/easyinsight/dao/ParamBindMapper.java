package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.ParamBind;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ParamBindMapper {
  /**
   * 根据给定条件进行查询
   * @param query 查询条件
   * @return 参数绑定信息
   */
  List<ParamBind> select(ParamBind query);

  /**
   * 查看参数绑定信息
   * @param entityIds 关联元素ID集合，必填参数
   * @param entityTypes 关联元素类型集合，必填参数
   * @param versionId 版本ID，非必填参数
   * @param appId 产品ID，必填参数
   * @return 参数绑定信息
   */
  List<ParamBind> selectByEntityId(@Param("entityIds") Collection<Long> entityIds,
                                   @Param("entityTypes") Collection<Integer> entityTypes,
                                   @Param("versionId") Long versionId,
                                   @Param("appId") Long appId);

  /**
   * 查看参数绑定信息
   * @param entityIds 关联元素ID集合，必填参数
   * @param entityTypes 关联元素类型集合，必填参数
   * @param versionId 版本ID，非必填参数
   * @param appId 产品ID，必填参数
   * @return 参数绑定信息
   */
  Integer selectSizeByEntityId(@Param("entityIds") Collection<Long> entityIds,
                               @Param("entityTypes") Collection<Integer> entityTypes,
                               @Param("versionId") Long versionId,
                               @Param("appId") Long appId);


  /**
   * 查看参数绑定信息
   * @param entityIds 关联元素ID集合，必填参数
   * @param entityType 关联元素类型，必填参数
   * @param versionId 版本ID，非必填参数
   * @param appId 产品ID，必填参数
   * @return 参数绑定信息
   */
  List<Long> selectIdByEntityId(@Param("entityIds") Collection<Long> entityIds,
                                @Param("entityType") Integer entityType,
                                @Param("versionId") Long versionId,
                                @Param("appId") Long appId);

  List<Long> selectParamIdByEntityId(@Param("entityIds") Collection<Long> entityIds,
                                     @Param("entityTypes") Collection<Integer> entityTypes,
                                     @Param("versionId") Long versionId,
                                     @Param("appId") Long appId);

  List<Long> selectIdsByEntityId(@Param("entityId") Long entityId,
                                 @Param("entityType") Integer entityType,
                                 @Param("versionId") Long versionId, @Param("appId") Long appId);

  /**
   * 根据主键ID获取参数绑定
   *
   * @param id 主键ID
   * @return 参数绑定对象
   */
  ParamBind selectByPrimaryKey(Long id);


  Integer selectSizeByParamId(@Param("paramIds") Collection<Long> paramIds,
                              @Param("entityType") Integer entityType);

  List<ParamBind> selectParamBindByParamId(@Param("paramIds") Collection<Long> paramIds,
                                           @Param("entityType") Integer entityType);

  /**
   * 插入参数绑定对象
   *
   * @param param 参数绑定对象
   */
  Integer insert(ParamBind param);

  /**
   * 更新参数绑定对象
   *
   * @param param 参数绑定对象
   */
  Integer update(ParamBind param);

  /**
   * 删除参数绑定对象
   *
   * @param id 参数绑定对象ID
   */
  Integer delete(Long id);

  Integer deleteByAppId(Long appId);

  Integer deleteByEntityId(@Param("entityId") Long entityId,
                           @Param("entityType") Integer entityType,
                           @Param("versionId") Long versionId,
                           @Param("appId") Long appId);

  Integer searchParamBindSize(@Param("entityId") Long entityId,
                              @Param("entityType") Integer entityType,
                              @Param("versionId") Long versionId,
                              @Param("appId") Long appId);


  List<ParamBind> searchParamBind(@Param("entityId") Long entityId, @Param("entityType") Integer entityType,
                                  @Param("versionId") Long versionId, @Param("appId") Long appId,
                                  @Param("orderBy") String orderBy, @Param("orderRule") String orderRule,
                                  @Param("offset") Integer offset, @Param("count") Integer count);

  Integer deleteByIds(@Param("ids") Collection<Long> ids);
}
