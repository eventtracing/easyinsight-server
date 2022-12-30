package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.netease.hz.bdms.easyinsight.common.dto.checkhistory.CheckHistorySimpleDTO;
import com.netease.hz.bdms.easyinsight.dao.CheckHistoryMapper;
import com.netease.hz.bdms.easyinsight.dao.model.CheckHistory;
import com.netease.hz.bdms.easyinsight.dao.model.CheckHistoryNoDetail;
import com.netease.hz.bdms.easyinsight.service.service.converter.CheckHistoryConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventCheckHistoryService {

    private static final Map<String, String> orderByMap = ImmutableMap
            .of("saveTime", "save_time", "updateTime", "update_time", "logServerTime", "log_server_time");
    private static final Map<String, String> orderRuleMap = ImmutableMap
            .of("descend", "desc", "ascend", "asc");

    @Resource
    private CheckHistoryMapper checkHistoryMapper;

    public Map<Long, List<CheckHistoryNoDetail>> getAllByBuryPointIds(Set<Long> buryPointIds) {
        if (CollectionUtils.isEmpty(buryPointIds)) {
            return new HashMap<>();
        }
        List<CheckHistory> checkHistories = checkHistoryMapper.selectByBuryPointIdsFromEventLogsNoDetail(buryPointIds);
        if (CollectionUtils.isEmpty(checkHistories)) {
            return new HashMap<>();
        }
        Map<Long, List<CheckHistoryNoDetail>> resultMap = new HashMap<>();
        checkHistories.forEach(checkHistory -> {
            Long buryPointId = checkHistory.getTrackerId();
            if (buryPointId == null) {
                return;
            }
            List<CheckHistoryNoDetail> histories = resultMap.computeIfAbsent(buryPointId, k -> new ArrayList<>(0));
            histories.add(new CheckHistoryNoDetail(checkHistory));
        });
        return resultMap;
    }

    public Integer deleteCheckHistory(Long historyId) {
        Preconditions.checkArgument(null != historyId, "取消保存的id为空");
        return checkHistoryMapper.delete(historyId);
    }

    public List<Long> createCheckHistory(Collection<CheckHistorySimpleDTO> checkHistorySimpleDTOS) {

        if (CollectionUtils.isEmpty(checkHistorySimpleDTOS)) {
            return new ArrayList<>(0);
        }
        // 数据转化
        List<CheckHistory> checkHistoryList = checkHistorySimpleDTOS.stream()
                .map(CheckHistoryConverter::dto2Do)
                .collect(Collectors.toList());
        checkHistoryList.forEach(c -> {
            // 事件测试记录SPM必须为""
            c.setSpm("");
        });
        // 批量插入 (批量插入并未对每一个字段信息进行检查，可能会报错)
        checkHistoryMapper.insertBatch(checkHistoryList);

        List<Long> idList = checkHistoryList.stream()
                .map(CheckHistory::getId)
                .collect(Collectors.toList());
        return idList;
    }

    public List<CheckHistorySimpleDTO> getCheckHistory(Long trackerId, String spm, String eventCode, Integer result, String orderBy, String orderRule, Integer offset,
                                                       Integer count) {
        Preconditions.checkArgument(null != trackerId, "埋点ID不能为空");

        String realOrderBy = orderByMap.get(orderBy);
        String realOrderRule = orderRuleMap.get(orderRule);
        List<CheckHistory> checkHistories = checkHistoryMapper.selectCheckHistoryFromEventLogs(trackerId, spm, eventCode, result, realOrderBy, realOrderRule, offset, count);
        return checkHistories.stream().map(CheckHistoryConverter::do2Dto).collect(Collectors.toList());
    }

}
