package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisAuditPackageInfo;
import com.netease.hz.bdms.easyinsight.dao.model.TestHistoryRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface EisAuditPackageInfoMapper {

  Integer insert(EisAuditPackageInfo eisAuditPackageInfo);

  Integer updateAuditId(EisAuditPackageInfo eisAuditPackageInfo);

  void updatePackageType(EisAuditPackageInfo eisAuditPackageInfo);

  void updateExt(@Param("id") long id, @Param("userInfo") String userInfo, @Param("ext") String ext);

  List<EisAuditPackageInfo> selectPackageInfos(@Param("appId") Long appId, @Param("terminalId") Long terminalId,
                                               @Param("versionId") String versionId, @Param("packageType") Integer packageType,
                                               @Param("buildUUIDList") Collection<String> buildUUIDList, @Param("userInfo") String userInfo,
                                               @Param("orderBy") String orderBy, @Param("orderRule") String orderRule,
                                               @Param("offset") Integer offset, @Param("count") Integer count);

  List<EisAuditPackageInfo> listLatestPackage(@Param("appId") Long appId, @Param("terminalId") Long terminalId, @Param("packageType") Integer packageType,
                                              @Param("versionId") String versionId, @Param("userInfo") String userInfo);

  EisAuditPackageInfo selectByBuildUUid(@Param("appId") Long appId, @Param("terminalId") Long terminalId, @Param("buildUUID") String buildUUID);

  List<EisAuditPackageInfo> selectByBuildUUids(@Param("appId") Long appId, @Param("buildUUIDs") List<String> buildUUIDs);

  List<EisAuditPackageInfo> selectByReq(@Param("relatedReq") String relatedReq);

  List<EisAuditPackageInfo> scan(@Param("offset") long offset);

  Integer delete(Long id);
}
