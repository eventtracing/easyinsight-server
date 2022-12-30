package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.checkhistory.CheckHistoryAggreDTO;
import com.netease.hz.bdms.easyinsight.common.dto.checkhistory.CheckHistorySimpleDTO;

import java.util.Collection;
import java.util.List;

public interface SpmCheckHistoryService {

    List<CheckHistorySimpleDTO> getCheckHistory(Long trackerId, String spm, String eventCode, Integer result,
                                                String orderBy, String orderRule, Integer offset, Integer count);

    List<CheckHistorySimpleDTO> getByTrackerId(Long trackerId);

    Integer getCheckHistorySize(Long trackerId, String spm, String eventCode, Integer result);

    CheckHistoryAggreDTO aggregateCheckHistory(Long trackerId);

    Integer deleteCheckHistory(Long historyId);

    List<Long> createCheckHistory(Collection<CheckHistorySimpleDTO> checkHistoryList);
}
