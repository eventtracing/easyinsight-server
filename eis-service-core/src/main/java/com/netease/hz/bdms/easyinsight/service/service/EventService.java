package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.event.EventSimpleDTO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface EventService {

  List<EventSimpleDTO> getEventByCode(String code, Long appId);

  List<EventSimpleDTO> getEventByCodes(Set<String> codes, Long appId);

  EventSimpleDTO getEventById(Long eventId);

  Long createEvent(EventSimpleDTO eventSimpleDTO);

  Integer updateEvent(EventSimpleDTO eventSimpleDTO);

  Integer deleteEvent(Long eventId);

  Integer searchEventSize(String search,  Long appId);

  List<EventSimpleDTO> searchEvent(String search, Long appId,
                                   String orderBy, String orderRule,
                                   Integer offset, Integer pageSize);

  List<EventSimpleDTO> getEventByIds(Collection<Long> eventIds);

  List<String> getAllEventCodes(Long appId);
}
