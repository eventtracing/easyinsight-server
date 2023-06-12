package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.ObjTrackerEvent;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 对象埋点的事件信息
 */
@Repository
public interface ObjTrackerEventMapper {

  /**
   * 根据主键ID获取对象埋点的事件信息
   * @param id 对象埋点的事件信息主键ID
   * @return 对象埋点的事件信息
   */
  ObjTrackerEvent selectByPrimaryKey(Long id);

  /**
   * 插入对象埋点的事件信息
   * @param objTrackerEvent 对象埋点的事件信息
   */
  Integer insert(ObjTrackerEvent objTrackerEvent);

  /**
   * 修改对象埋点的事件信息
   * @param objTrackerEvent 对象埋点的事件信息
   */
  Integer update(ObjTrackerEvent objTrackerEvent);

  /**
   * 删除对象埋点的事件信息
   * @param id 对象埋点主键ID
   */
  Integer delete(Long id);

  Integer batchInsert(@Param("trackerEvents") Collection<ObjTrackerEvent> trackerEvents);

  List<ObjTrackerEvent> selectByTrackerId(@Param("trackerIds") Collection<Long> trackerIds);

  Integer deleteEventByTrackerId(@Param("trackerIds") Collection<Long> trackerIds);

  Integer deleteByTrackerIdAndEventId(@Param("trackerId") Long trackerId, @Param("eventId") Long eventId);

  Integer selectSizeByEventId(@Param("eventIds") Collection<Long> eventIds);
}
