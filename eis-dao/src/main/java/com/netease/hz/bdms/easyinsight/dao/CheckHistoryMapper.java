package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.CheckHistory;
import com.netease.hz.bdms.easyinsight.dao.model.Event;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckHistoryMapper {

  List<String> selectSpmsFromSpmLogs(Long trackerId);

  List<Event> selectEventsFromSpmLogs(Long trackerId);

  List<CheckHistory> selectByTrackerIdsFromSpmLogs(@Param("trackerIds") Collection<Long> trackerIds);

  List<CheckHistory> selectByTrackerIdsFromSpmLogsWithoutDetail(@Param("trackerIds") Collection<Long> trackerIds);

  List<CheckHistory> selectCheckHistoryFromSpmLogs(@Param("trackerId") Long trackerId, @Param("spm") String spm,
                                                   @Param("eventCode") String eventCode, @Param("result") Integer result,
                                                   @Param("orderBy") String orderBy, @Param("orderRule") String orderRule,
                                                   @Param("offset") Integer offset, @Param("count") Integer count);

  List<CheckHistory> selectCheckHistoryFromEventLogs(@Param("trackerId") Long trackerId, @Param("spm") String spm,
                                                   @Param("eventCode") String eventCode, @Param("result") Integer result,
                                                   @Param("orderBy") String orderBy, @Param("orderRule") String orderRule,
                                                   @Param("offset") Integer offset, @Param("count") Integer count);

  Integer selectSizeFromSpmLogs(@Param("trackerId") Long trackerId, @Param("spm") String spm,
                                @Param("eventCode") String eventCode, @Param("result") Integer result);


  List<CheckHistory> selectByBuryPointIdsFromEventLogsNoDetail(@Param("buryPointIds") Collection<Long> trackerIds);

  Integer insert(CheckHistory checkHistory);

  Integer insertBatch(@Param("checkHistoryList")Collection<CheckHistory> checkHistoryList);

  /**
   * 删除测试记录
   *
   * @param id 主键ID
   */
  Integer delete(Long id);
}
