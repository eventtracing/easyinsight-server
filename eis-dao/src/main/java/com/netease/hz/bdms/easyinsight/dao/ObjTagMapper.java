package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.ObjTag;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ObjTagMapper {

  List<ObjTag> selectObjTagsByObjIdAndTagId(@Param("tagId") Long tagId, @Param("objId") Long objId);

  Integer batchInsert(@Param("objTags") Collection<ObjTag> objTags);

  List<ObjTag> selectObjTagsByHistoryIds(@Param("historyIds") Collection<Long> historyIds);

  List<ObjTag> selectObjTagsByObjIds(@Param("objIds") Collection<Long> objIds);

  List<ObjTag> selectObjTagsByTagIds(@Param("tagIds") Collection<Long> tagIds);

  Integer deleteObjTag(@Param("objIds") Collection<Long> objIds);

  List<ObjTag> select(@Param("appId") Long appId, @Param("tagIds") Collection<Long> tagIds,
      @Param("historyIds") Collection<Long> historyIds,
      @Param("objIds") Collection<Long> objIds);

  List<ObjTag> listByOffset(@Param("idOffset") long idOffset, @Param("limit") int limit);
}
