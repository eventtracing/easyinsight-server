package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisRealtimeBranchIgnore;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EisRealtimeBranchIgnoreMapper {

  List<EisRealtimeBranchIgnore> listAll(@Param("conversationId") String conversationId);

  Integer batchInsert(@Param("ignoreList") List<EisRealtimeBranchIgnore> ignoreList);

  Integer removeAll(@Param("conversationId") String conversationId);

}
