package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.dto.obj.tracker.event.ObjTrackerEventSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.ObjTrackerEventMapper;
import com.netease.hz.bdms.easyinsight.dao.model.ObjTrackerEvent;
import com.netease.hz.bdms.easyinsight.service.service.ObjTrackerEventService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ObjTrackerEventServiceImpl implements ObjTrackerEventService {

    @Autowired
    private ObjTrackerEventMapper objTrackerEventMapper;


    private ObjTrackerEventSimpleDTO do2Dto(ObjTrackerEvent objTrackerEvent) {
    ObjTrackerEventSimpleDTO objTrackerEventSimpleDTO = BeanConvertUtils
        .convert(objTrackerEvent, ObjTrackerEventSimpleDTO.class);
    return objTrackerEventSimpleDTO;
  }

  private ObjTrackerEvent dto2Do(ObjTrackerEventSimpleDTO objTrackerEventSimpleDT0O) {
    ObjTrackerEvent objTrackerEvent = BeanConvertUtils
        .convert(objTrackerEventSimpleDT0O, ObjTrackerEvent.class);
    return objTrackerEvent;
  }


  @Override
  public void createTrackerEvents(List<ObjTrackerEventSimpleDTO> objTrackerEventSimpleDTOS) {
    if (CollectionUtils.isNotEmpty(objTrackerEventSimpleDTOS)) {
      List<ObjTrackerEvent> trackerEvents = objTrackerEventSimpleDTOS.stream().map(this::dto2Do)
          .collect(Collectors.toList());
      objTrackerEventMapper.batchInsert(trackerEvents);
    }
  }

  @Override
  public Integer getSizeByEventId(Collection<Long> eventIds) {
    if(CollectionUtils.isNotEmpty(eventIds)) {
      return objTrackerEventMapper.selectSizeByEventId(eventIds);
    }
    return 0;
  }

  @Override
  public List<ObjTrackerEventSimpleDTO> getByTrackerId(Collection<Long> trackerIds) {
    List<ObjTrackerEventSimpleDTO> result = Lists.newArrayList();
    if(CollectionUtils.isNotEmpty(trackerIds)) {
      List<ObjTrackerEvent> trackerEvents = objTrackerEventMapper.selectByTrackerId(trackerIds);
      return trackerEvents.stream().map(this::do2Dto).collect(Collectors.toList());
    }
    return result;
  }

  @Override
  public Integer deleteEventByTrackerId(Collection<Long> trackerIds) {
    if(CollectionUtils.isNotEmpty(trackerIds)) {
      return objTrackerEventMapper.deleteEventByTrackerId(trackerIds);
    }
    return 0;
  }

}
