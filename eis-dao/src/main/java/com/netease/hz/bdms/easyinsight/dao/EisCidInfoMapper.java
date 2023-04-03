package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.CidInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EisCidInfoMapper {

    List<CidInfo> listAll(String bindType, String target, Long appId);

    CidInfo get(String bindType, String target, Long appId, String cid);

    Integer insert(CidInfo cidInfo);

    Integer batchInsert(@Param("cidInfos") List<CidInfo> cidInfos);

    Integer deleteByIds(@Param("ids") Collection<Long> ids);

    List<CidInfo> selectCidTagInfosByOid(@Param("bindType") String bindType,
                                         @Param("oid") String oid,
                                         @Param("appId") Long appId);
}
