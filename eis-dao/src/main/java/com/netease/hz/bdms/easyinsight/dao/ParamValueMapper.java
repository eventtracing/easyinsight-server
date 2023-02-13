package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.ParamValue;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ParamValueMapper {
  /**
   * 根据主键ID获取参数值对象
   * @param id 参数值ID
   * @return 参数值对象
   */
  ParamValue selectByPrimaryKey(Long id);

  List<ParamValue> selectBatchByIds(@Param("ParamValueIds") Set<Long> ParamValueIds);

  List<ParamValue> selectParamValues(@Param("paramId") Long paramId, @Param("search") String search);

  Integer selectParamValueSizeByParamId(Long paramId);

  /**
   * 插入参数值对象
   * @param param 参数值对象
   */
  Integer insert(ParamValue param);

  /**
   * 更新参数值对象
   * @param param 参数值对象
   */
  Integer update(ParamValue param);

  /**
   * 删除参数值对象
   * @param id 参数值ID
   */
  Integer delete(Long id);

  Integer deleteByAppId(Long appId);

  Integer deleteByIds(@Param("ids")Collection<Long> ids);


  List<ParamValue> selectByParamIds(@Param("paramIds") Collection<Long> paramIds);

  Integer deleteByParamId(Long paramId);
}
