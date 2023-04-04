package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisAuditResource;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface EisAuditResourceMetaMapper {

  Integer insert(EisAuditResource eisAuditResource);

  Integer insertWithId(EisAuditResource eisAuditResource);

  EisAuditResource selectById(Long id);

  List<EisAuditResource> selectBatchByIds(@Param("ids") Set<Long> ids);

  void updateBatch(List<EisAuditResource> list);

  Integer delete(Long id);

}
