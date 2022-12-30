package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.logcheck.EisLogCheckAlarmRule;

import java.util.List;

public interface EisLogCheckAlarmRuleMapper {

    Integer insert(EisLogCheckAlarmRule item);

    void updateContentById(EisLogCheckAlarmRule item);

    List<EisLogCheckAlarmRule> listByOffset(long offsetId, int limit);

    EisLogCheckAlarmRule getByMonitorId(long monitorItemId);

    void deleteById(long id);
}
