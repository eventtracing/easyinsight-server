package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.dao.model.Param;
import java.util.Collection;
import java.util.List;


public interface ParamMapper {

  /**
   * 根据参数名在指定产品中获取参数对象
   *
   * @param code  参数名
   * @param appId 产品ID
   * @return 参数对象集合
   */
  List<Param> selectByCode(@org.apache.ibatis.annotations.Param("code") String code,
      @org.apache.ibatis.annotations.Param("appId") Long appId);

  List<Param> selectByAppId(@org.apache.ibatis.annotations.Param("appId") Long appId);

  /**
   * 根据参数名在指定产品中获取参数对象
   *
   * @param code  参数名
   * @param appId 产品ID
   * @return 参数对象集合
   */
  Integer selectSizeByCode(@org.apache.ibatis.annotations.Param("code") String code,
      @org.apache.ibatis.annotations.Param("appId") Long appId);

  /**
   * 根据主键ID获取参数
   *
   * @param id 参数ID
   * @return 参数对象
   */
  Param selectByPrimaryKey(Long id);

  List<Param> selectByIds(@org.apache.ibatis.annotations.Param("ids") Collection<Long> ids);

  List<Param> listByAppIdAndCodes(@org.apache.ibatis.annotations.Param("appId") Long appId, @org.apache.ibatis.annotations.Param("paramType") Integer paramType, @org.apache.ibatis.annotations.Param("codes") Collection<String> codes);

  /**
   * 插入参数对象
   *
   * @param param 参数对象
   */
  Integer insert(Param param);

  /**
   * 更新参数对象
   *
   * @param param 参数对象
   */
  Integer update(Param param);

  /**
   * 删除参数对象
   *
   * @param id 参数对象ID
   */
  Integer delete(Long id);

  Integer deleteByAppId(Long appId);

  Integer searchParamSize(@org.apache.ibatis.annotations.Param("search") String search,
      @org.apache.ibatis.annotations.Param("paramTypes") Collection<Integer> paramTypes,
      @org.apache.ibatis.annotations.Param("createEmails") Collection<String> createEmails,
      @org.apache.ibatis.annotations.Param("valueTypes") Collection<Integer> valueTypes,
      @org.apache.ibatis.annotations.Param("code") String code,
      @org.apache.ibatis.annotations.Param("appId") Long appId,
      @org.apache.ibatis.annotations.Param("ids") Collection<Long> ids
      );

  List<Long> searchParamIdsByName(@org.apache.ibatis.annotations.Param("search") String search,
      @org.apache.ibatis.annotations.Param("paramTypes") Collection<Integer> paramTypes,
      @org.apache.ibatis.annotations.Param("createEmails") Collection<String> createEmails,
      @org.apache.ibatis.annotations.Param("valueTypes") Collection<Integer> valueTypes,
      @org.apache.ibatis.annotations.Param("code") String code,
      @org.apache.ibatis.annotations.Param("appId") Long appId,
      @org.apache.ibatis.annotations.Param("ids") Collection<Long> ids
  );

  List<Param> searchParams(@org.apache.ibatis.annotations.Param("search") String search,
      @org.apache.ibatis.annotations.Param("paramTypes") Collection<Integer> paramTypes,
      @org.apache.ibatis.annotations.Param("createEmails") Collection<String> createEmails,
      @org.apache.ibatis.annotations.Param("valueTypes") Collection<Integer> valueTypes,
      @org.apache.ibatis.annotations.Param("code") String code,
      @org.apache.ibatis.annotations.Param("appId") Long appId,
      @org.apache.ibatis.annotations.Param("ids") Collection<Long> ids,
      @org.apache.ibatis.annotations.Param("orderBy") String orderBy,
      @org.apache.ibatis.annotations.Param("orderRule") String orderRule,
      @org.apache.ibatis.annotations.Param("offset") Integer offset,
      @org.apache.ibatis.annotations.Param("count") Integer count);


  List<UserSimpleDTO> getCreators(@org.apache.ibatis.annotations.Param("appId") Long appId,
      @org.apache.ibatis.annotations.Param("paramType") Integer paramType);

  Integer updateCode(@org.apache.ibatis.annotations.Param("appId") Long appId,
      @org.apache.ibatis.annotations.Param("paramType") Integer paramType,
      @org.apache.ibatis.annotations.Param("oldCode") String oldCode,
      @org.apache.ibatis.annotations.Param("newCode") String newCode);
}
