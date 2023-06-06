package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.obj.tracker.event.ObjTrackerEventSimpleDTO;

import java.util.Collection;
import java.util.List;

public interface ObjTrackerEventService {

  void createTrackerEvents(List<ObjTrackerEventSimpleDTO> trackerEvents);

  Integer getSizeByEventId(Collection<Long> eventIds);

  List<ObjTrackerEventSimpleDTO> getByTrackerId(Collection<Long> trackerIds);

  Integer deleteEventByTrackerId(Collection<Long> trackerIds);

  Integer deleteByTrackerIdAndEventId(Long trackerId, Long eventId);

}
