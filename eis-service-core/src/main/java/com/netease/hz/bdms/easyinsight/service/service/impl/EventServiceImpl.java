package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.netease.eis.adapters.CacheAdapter;
import com.netease.hz.bdms.easyinsight.common.dto.ListHolder;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.event.EventSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.common.util.CacheUtils;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import com.netease.hz.bdms.easyinsight.dao.EventMapper;
import com.netease.hz.bdms.easyinsight.dao.model.Event;
import com.netease.hz.bdms.easyinsight.service.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventServiceImpl implements EventService {

    private static final Map<String, String> orderByMap = ImmutableMap
            .of("createTime", "create_time", "updateTime", "update_time");
    private static final Map<String, String> orderRuleMap = ImmutableMap
            .of("descend", "desc", "ascend", "asc");
    @Autowired
    private EventMapper eventMapper;

    @Resource
    private CacheAdapter cacheAdapter;

    private EventSimpleDTO do2Dto(Event event) {
        EventSimpleDTO eventSimpleDTO = BeanConvertUtils
                .convert(event, EventSimpleDTO.class);
        if (null != eventSimpleDTO) {
            UserSimpleDTO updater = new UserSimpleDTO(event.getUpdateEmail(),
                    event.getUpdateName());
            UserSimpleDTO creator = new UserSimpleDTO(event.getCreateEmail(),
                    event.getCreateName());

            List<Integer> applicableObjTypes = Lists.newArrayList();
            if (StringUtils.isNotBlank(event.getApplicableObjTypes())) {
                applicableObjTypes = JsonUtils.parseList(event.getApplicableObjTypes(), Integer.class);
            }

            eventSimpleDTO.setCreator(creator)
                    .setUpdater(updater)
                    .setApplicableObjTypes(applicableObjTypes);
        }
        return eventSimpleDTO;
    }

    private Event dto2Do(EventSimpleDTO eventSimpleDTO) {
        Event event = BeanConvertUtils.convert(eventSimpleDTO, Event.class);
        if (event != null) {
            UserSimpleDTO updater = eventSimpleDTO.getUpdater();
            UserSimpleDTO creator = eventSimpleDTO.getCreator();
            if (CollectionUtils.isNotEmpty(eventSimpleDTO.getApplicableObjTypes())) {
                String applicableObjTypes = JsonUtils.toJson(eventSimpleDTO.getApplicableObjTypes());
                event.setApplicableObjTypes(applicableObjTypes);
            }

            if (creator != null) {
                event.setCreateEmail(creator.getEmail())
                        .setCreateName(creator.getUserName());
            }
            if (updater != null) {
                event.setUpdateEmail(updater.getEmail())
                        .setUpdateName(updater.getUserName());
            }
        }
        return event;
    }

    @Override
    public List<EventSimpleDTO> getEventByCode(String code, Long appId) {
        Preconditions.checkArgument(null != appId, "产品ID不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(code), "事件类型ID不能为空");

        List<Event> events = eventMapper.selectByCode(code, appId);
        return events.stream().map(this::do2Dto).collect(Collectors.toList());
    }

    @Override
    public List<EventSimpleDTO> getEventByCodes(Set<String> codes, Long appId) {
        if (appId == null) {
            return new ArrayList<>(0);
        }
        if (CollectionUtils.isEmpty(codes)) {
            return new ArrayList<>(0);
        }

        List<Event> events = eventMapper.selectByCodes(codes, appId);
        return events.stream().map(this::do2Dto).collect(Collectors.toList());
    }

    @Override
    public EventSimpleDTO getEventById(Long eventId) {
        Preconditions.checkArgument(null != eventId, "事件类型主键ID不能为空");
        Event event = eventMapper.selectByPrimaryKey(eventId);
        return do2Dto(event);
    }

    @Override
    public Long createEvent(EventSimpleDTO eventSimpleDTO) {
        Event event = dto2Do(eventSimpleDTO);
        Preconditions.checkArgument(null != event, "事件类型对象不能为空");

        if(event.getCreateTime() == null){
            event.setCreateTime(new Timestamp(System.currentTimeMillis()));
        }
        if(event.getUpdateTime() == null){
            event.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        }
        eventMapper.insert(event);
        return event.getId();
    }

    @Override
    public Integer updateEvent(EventSimpleDTO eventSimpleDTO) {
        Event event = dto2Do(eventSimpleDTO);
        if (event == null || event.getId() == null) {
            throw new CommonException("事件类型对象或其ID不能为空");
        }
        return eventMapper.update(event);
    }

    @Override
    public Integer deleteEvent(Long eventId) {
        Preconditions.checkArgument(null != eventId, "事件类型ID不能为空");
        return eventMapper.delete(eventId);
    }

    @Override
    public Integer searchEventSize(String search, Long appId) {
        return eventMapper.searchEventSize(search, appId);
    }

    @Override
    public List<EventSimpleDTO> searchEvent(String search, Long appId, String orderBy,
                                            String orderRule, Integer offset, Integer pageSize) {
        String dbOrderBy = orderByMap.get(orderBy);
        String dbOrderRule = orderRuleMap.get(orderRule);

        List<Event> events = eventMapper.searchEvent(search, appId, dbOrderBy, dbOrderRule, offset, pageSize);
        return events.stream().map(this::do2Dto).collect(Collectors.toList());
    }

    @Override
    public List<EventSimpleDTO> getEventByIds(Collection<Long> eventIds) {
        if (CollectionUtils.isNotEmpty(eventIds)) {
            List<Event> terminals = eventMapper.selectByIds(eventIds);
            return terminals.stream().map(this::do2Dto).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    public List<String> getAllEventCodes(Long appId) {
        ListHolder listHolder = CacheUtils.getAndSetIfAbsent(() -> "getAllEventCodes_" + appId,
                () -> doGetAllEventCodes(appId),
                (key) -> cacheAdapter.get(key),
                (key, value) -> cacheAdapter.setWithExpireTime(key, value, 120),
                ListHolder.class);
        return listHolder == null ? new ArrayList<>(0) : listHolder.getList();
    }

    public ListHolder doGetAllEventCodes(Long appId) {
        List<Event> events = eventMapper.selectAllByAppId(appId);
        if (CollectionUtils.isEmpty(events)) {
            return new ListHolder().setList(new ArrayList<>(0));
        }
        return new ListHolder().setList(events.stream().map(Event::getCode).distinct().collect(Collectors.toList()));
    }
}
