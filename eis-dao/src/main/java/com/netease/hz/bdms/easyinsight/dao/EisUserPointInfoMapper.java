package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisUserPointInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EisUserPointInfoMapper {

  EisUserPointInfo getById(long id);

  Integer insertBatch(@Param("list") List<EisUserPointInfo> list);

  List<EisUserPointInfo> selectByReqId(Long reqId);

  void updateExtInfo(@Param("id") Long id, @Param("extInfo") String extInfo);

  Integer delete(Long id);

}
