package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.ParamBindValue;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ParamBindValueMapper {

  /**
   * 根据主键ID获取参数绑定的值对象
   * @param id 参数值ID
   * @return 参数值绑定对象
   */
  ParamBindValue selectByPrimaryKey(Long id);

  /**
   * 插入数绑定的值对象
   * @param param 参数绑定值对象
   */
  Long insert(ParamBindValue param);

  void batchInsert(@Param("paramBindValues")Collection<ParamBindValue> paramBindValues);


  /**
   * 更新参数绑定的值对象
   * @param param 参数绑定值对象
   */
  Integer update(ParamBindValue param);

  /**
   * 删除参数绑定的值对象
   * @param id 参数绑定值ID
   */
  Integer delete(Long id);

  Integer deleteByAppId(Long appId);

  Integer deleteByBindIds(@Param("bindIds") Collection<Long> bindIds);

  Integer deleteByBindId(Long bindId);

  List<ParamBindValue> selectByBindIds(@Param("bindIds") Collection<Long> bindIds);

  List<ParamBindValue> selectByParamValueIds(@Param("paramValueIds") Collection<Long> paramValueIds);
}
