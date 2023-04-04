package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.ParamRuleAudit;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParamRuleAuditMapper {

  Integer insertBatch(@Param("list") List<ParamRuleAudit> list);

  List<ParamRuleAudit> selectByObjId(Long objId);

  void updateBatch(@Param("list") List<ParamRuleAudit> list);

  Integer delete(Long id);

}
