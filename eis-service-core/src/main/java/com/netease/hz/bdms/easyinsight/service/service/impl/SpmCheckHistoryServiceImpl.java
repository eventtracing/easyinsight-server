package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.dto.checkhistory.CheckHistoryAggreDTO;
import com.netease.hz.bdms.easyinsight.common.dto.checkhistory.CheckHistorySimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.CommonAggregateDTO;
import com.netease.hz.bdms.easyinsight.dao.CheckHistoryMapper;
import com.netease.hz.bdms.easyinsight.dao.model.CheckHistory;
import com.netease.hz.bdms.easyinsight.dao.model.Event;
import com.netease.hz.bdms.easyinsight.service.service.ObjectBasicService;
import com.netease.hz.bdms.easyinsight.service.service.SpmCheckHistoryService;
import com.netease.hz.bdms.easyinsight.service.service.converter.CheckHistoryConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SpmCheckHistoryServiceImpl implements SpmCheckHistoryService {
    private static final Map<String, String> orderByMap = ImmutableMap
            .of("failedNum", "failedNum", "createTime", "createTime", "update_time", "update_time");
    private static final Map<String, String> orderRuleMap = ImmutableMap
            .of("descend", "desc", "ascend", "asc");

    @Resource
    private CheckHistoryMapper checkHistoryMapper;

    @Resource
    private ObjectBasicService objectBasicService;

    @Override
    public List<CheckHistorySimpleDTO> getCheckHistory(Long trackerId, String spm, String eventCode, Integer result, String orderBy, String orderRule, Integer offset,
                                                       Integer count) {
        Preconditions.checkArgument(null != trackerId, "埋点ID不能为空");

        String realOrderBy = orderByMap.get(orderBy);
        String realOrderRule = orderRuleMap.get(orderRule);
        List<CheckHistory> checkHistories = checkHistoryMapper.selectCheckHistoryFromSpmLogs(trackerId, spm, eventCode, result, realOrderBy, realOrderRule, offset, count);
        return checkHistories.stream().map(CheckHistoryConverter::do2Dto).collect(Collectors.toList());
    }

    @Override
    public List<CheckHistorySimpleDTO> getByTrackerId(Long trackerId) {
        List<CheckHistory> checkHistories = checkHistoryMapper.selectByTrackerIdsFromSpmLogs(Lists.newArrayList(trackerId));
        List<CheckHistorySimpleDTO> checkHistorySimpleDTOS = checkHistories.stream()
                .map(CheckHistoryConverter::do2Dto)
                .collect(Collectors.toList());
        return Optional.ofNullable(checkHistorySimpleDTOS).orElse(new ArrayList<>());
    }

    @Override
    public Integer getCheckHistorySize(Long trackerId, String spm, String eventCode, Integer result) {
        Preconditions.checkArgument(null != trackerId, "埋点ID不能为空");
        return checkHistoryMapper.selectSizeFromSpmLogs(trackerId, spm, eventCode, result);
    }

    @Override
    public CheckHistoryAggreDTO aggregateCheckHistory(Long trackerId) {
        Preconditions.checkArgument(null != trackerId, "埋点ID不能为空");
        List<String> spmsWithPos = checkHistoryMapper.selectSpmsFromSpmLogs(trackerId);
        List<Event> events = checkHistoryMapper.selectEventsFromSpmLogs(trackerId);

        List<CommonAggregateDTO> spmAggres = Lists.newArrayList();
        List<CommonAggregateDTO> eventAggres = Lists.newArrayList();

        if (CollectionUtils.isNotEmpty(spmsWithPos)) {
            for (String spmWithPos : spmsWithPos) {
                CommonAggregateDTO tmpAggre = new CommonAggregateDTO();
                String spmWithOutPos = removePos(spmWithPos);
                tmpAggre.setKey(spmWithOutPos)
                        .setValue(spmWithOutPos);
                spmAggres.add(tmpAggre);
            }
        }
        if (CollectionUtils.isNotEmpty(events)) {
            for (Event event : events) {
                String value = StringUtils.isNotBlank(event.getName()) ? event.getCode() + "(" + event.getName() + ")" : event.getCode();

                CommonAggregateDTO tmpAggre = new CommonAggregateDTO();
                tmpAggre.setKey(event.getCode())
                        .setValue(value);
                eventAggres.add(tmpAggre);
            }
        }

        CheckHistoryAggreDTO result = new CheckHistoryAggreDTO();
        result.setSpms(spmAggres)
                .setEvents(eventAggres);
        return result;
    }

    @Override
    public Integer deleteCheckHistory(Long historyId) {
        Preconditions.checkArgument(null != historyId, "取消保存的id为空");
        return checkHistoryMapper.delete(historyId);
    }

    @Override
    public List<Long> createCheckHistory(Collection<CheckHistorySimpleDTO> checkHistorySimpleDTOS) {

        if (CollectionUtils.isEmpty(checkHistorySimpleDTOS)) {
            return Lists.newArrayList();
        }
        // 数据转化
        List<CheckHistory> checkHistoryList = checkHistorySimpleDTOS.stream()
                .map(CheckHistoryConverter::dto2Do)
                .filter(c -> StringUtils.isNotBlank(c.getSpm()))
                .collect(Collectors.toList());
        // 批量插入 (批量插入并未对每一个字段信息进行检查，可能会报错)
        checkHistoryMapper.insertBatch(checkHistoryList);

        List<Long> idList = checkHistoryList.stream()
                .map(CheckHistory::getId)
                .collect(Collectors.toList());
        return idList;
    }

    private static String removePos(String oidWithPos) {
        if (StringUtils.isNotBlank(oidWithPos)) {
            String pattern = "(:[0-9]*)?";
            return oidWithPos.replaceAll(pattern, "");
        }
        return null;
    }

    public String getSpmByObjId(String spm,Map<String,Long> oidToObjIdMap){
        List<String> oidList = Lists.newArrayList(spm.split("\\|"))
                .stream().collect(Collectors.toList());
        List<String> oidIdStrList = new ArrayList<>();
        for (String oid : oidList) {
            if(!oidToObjIdMap.containsKey(oid)){
                return null;
            }
            oidIdStrList.add(oidToObjIdMap.get(oid).toString());
        }
        String spmByObjId = String.join("|",oidIdStrList);
        return spmByObjId;
    }
}
