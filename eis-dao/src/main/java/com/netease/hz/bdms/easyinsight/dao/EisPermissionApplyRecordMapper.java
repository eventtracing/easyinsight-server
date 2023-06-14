package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisPermissionApplyRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EisPermissionApplyRecordMapper {

  Integer insert(EisPermissionApplyRecord eisPermissionApplyRecord);

  EisPermissionApplyRecord getById(@Param("id") long id);

  void updateRecordStatus(@Param("id") long id, @Param("status") Integer status, @Param("auditUser") String auditUser);

  List<EisPermissionApplyRecord> listApplyRecords(@Param("appId") Long appId, @Param("status") Integer status);

  Integer delete(Long id);
}
