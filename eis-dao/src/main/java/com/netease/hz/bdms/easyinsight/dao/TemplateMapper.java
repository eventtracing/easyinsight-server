package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.Template;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateMapper {

  /**
   * 根据名称获取模板对象
   *
   * @param name  模板对象名称，必填参数
   * @param appId 产品ID，必填参数
   * @return 模板对象集合
   */
  List<Template> selectByName(@Param("name") String name, @Param("appId") Long appId);

  /**
   * 根据主键ID获取模板对象
   *
   * @param id 主键ID，必填参数
   * @return 模板对象，必填参数
   */
  Template selectByPrimaryKey(Long id);

  /**
   * 插入模板对象
   *
   * @param param 模板对象，必填参数
   */
  Integer insert(Template param);

  /**
   * 更新模板对象
   *
   * @param param 模板对象，必填参数
   */
  Integer update(Template param);

  /**
   * 删除模板对象
   *
   * @param id 模板对象ID，必填参数
   */
  Integer delete(Long id);


  Integer searchTemplateSize(@Param("search") String search,
      @Param("appId") Long appId);

  List<Template> searchTemplate(@Param("search") String search,
      @Param("appId") Long appId,
      @Param("orderBy") String orderBy, @Param("orderRule") String orderRule,
      @Param("offset") Integer offset, @Param("count") Integer count);

}
