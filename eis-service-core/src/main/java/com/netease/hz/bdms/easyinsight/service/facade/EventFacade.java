package com.netease.hz.bdms.easyinsight.service.facade;

import com.google.common.base.Preconditions;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.app.AppSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingResultDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.PagingSortDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.event.EventSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.enums.EntityTypeEnum;
import com.netease.hz.bdms.easyinsight.common.exception.DomainException;
import com.netease.hz.bdms.easyinsight.common.param.event.EventCreateParam;
import com.netease.hz.bdms.easyinsight.common.param.event.EventUpdateParam;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.service.service.AppService;
import com.netease.hz.bdms.easyinsight.service.service.EventService;
import com.netease.hz.bdms.easyinsight.service.service.ObjTrackerEventService;
import com.netease.hz.bdms.easyinsight.service.service.VersionService;
import com.netease.hz.bdms.easyinsight.service.helper.ParamBindHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;


@Slf4j
@Component
public class EventFacade {
  @Autowired
  EventService eventService;

  @Autowired
  AppService appService;

  @Autowired
  VersionService versionService;

  @Autowired
  ObjTrackerEventService objTrackerEventService;

  @Autowired
  ParamBindHelper paramBindHelper;

  public Long createEvent(EventCreateParam param) {
    // 验证参数
    Preconditions.checkArgument(null != param, "事件类型参数不能为空");
    Preconditions.checkArgument(StringUtils.isNotBlank(param.getName()), "事件类型名称不能为空");
    Preconditions.checkArgument(StringUtils.isNotBlank(param.getCode()), "事件类型code不能为空");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null !=  appId, "未指定产品信息");
    AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
    Preconditions.checkArgument(null != appSimpleDTO, "该产品不存在");

    // 验证当前事件是否已存在
    List<EventSimpleDTO> existsEvents = eventService
        .getEventByCode(param.getCode(), appId);
    Preconditions.checkArgument(CollectionUtils.isEmpty(existsEvents), "该事件类型已存在，创建失败");

    // 插入记录
    EventSimpleDTO eventSimpleDTO = BeanConvertUtils.convert(param, EventSimpleDTO.class);
    eventSimpleDTO.setAppId(appId);

    UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
    UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
    eventSimpleDTO.setCreator(currentUser)
        .setUpdater(currentUser);
    Long eventId = null;
    try {
      eventId = eventService.createEvent(eventSimpleDTO);
    } catch (DuplicateKeyException e) {
      log.debug("", e);
      throw new DomainException("该事件类型已存在，创建失败");
    }

    // 新建预置版本
    // versionService.presetVersion(appId, eventId, EntityTypeEnum.EVENT.getType(), currentUser);

    return eventId;
  }

  public Integer updateEvent(EventUpdateParam param) {
    // 验证参数
    Preconditions.checkArgument(null != param, "事件类型参数不能为空");
    Preconditions.checkArgument(null != param.getId(), "事件类型主键ID不能为空");
    Preconditions.checkArgument(StringUtils.isNotBlank(param.getName()), "事件类型名称不能为空");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null !=  appId, "未指定产品信息");

    EventSimpleDTO existsEvent = eventService.getEventById(param.getId());
    Preconditions.checkArgument(null != existsEvent, "该事件类型不存在，修改失败");
    Preconditions.checkArgument(appId == existsEvent.getAppId(), "未指定产品信息或事件类型不在该产品下，，修改失败");

    // 插入记录
    EventSimpleDTO eventSimpleDTO = BeanConvertUtils.convert(param, EventSimpleDTO.class);
    eventSimpleDTO.setAppId(appId);

    UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
    UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);
    eventSimpleDTO.setUpdater(currentUser);
    try {
      return eventService.updateEvent(eventSimpleDTO);
    } catch (DuplicateKeyException e) {
      log.debug("", e);
      throw new DomainException("该事件类型已存在，修改失败");
    }
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public Integer deleteEvent(Long eventId) {
    Preconditions.checkArgument(null != eventId, "事件类型ID不能为空");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null !=  appId, "未指定产品信息");

    Integer objTrackerEventSize = objTrackerEventService.getSizeByEventId(Collections.singletonList(eventId));
    Preconditions.checkArgument(objTrackerEventSize <= 0,  "该事件类型已被使用");

    // 删除参数绑定信息
    paramBindHelper.deleteParamBinds(appId, Collections.singleton(eventId), EntityTypeEnum.EVENT
        .getType(), null);

    // 删除现有的版本
    versionService.deleteVersion(eventId, EntityTypeEnum.EVENT.getType(),  appId);

    return eventService.deleteEvent(eventId);
  }

  public PagingResultDTO<EventSimpleDTO> listEvents(String search, PagingSortDTO pagingSortDTO) {
    // 验证参数
    Preconditions.checkArgument(null != pagingSortDTO, "分页不能为空");

    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null !=  appId, "未指定产品信息");

    // 获取大小
    Integer totalNum = eventService.searchEventSize(search, appId);
    // 获取分页明细
    List<EventSimpleDTO> events = eventService
        .searchEvent(search, appId, pagingSortDTO.getOrderBy(),
            pagingSortDTO.getOrderRule(), pagingSortDTO.getOffset(), pagingSortDTO.getPageSize());

    PagingResultDTO<EventSimpleDTO> result = new PagingResultDTO<>();
    result.setTotalNum(totalNum)
        .setPageNum(pagingSortDTO.getCurrentPage())
        .setList(events);
    return result;
  }

  public EventSimpleDTO getEvent(Long eventId) {
    Preconditions.checkArgument(null != eventId, "事件类型ID不能为空");

    return eventService.getEventById(eventId);
  }

}
