package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisBuildRuleResource;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EisBuildRuleResourceMapper {

  Integer insert(EisBuildRuleResource eisBuildRuleResource);

  /**
   * 批量插入
   * @param list
   */
  void insertBatch(@Param("list") List<EisBuildRuleResource> list);

  EisBuildRuleResource selectLastVerByBuildUUid(String buildUUid);

  List<EisBuildRuleResource> selectAllByBuildUUid(@Param("uuids") List<String> uuids);

  Integer delete(Long id);

}
