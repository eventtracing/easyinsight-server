package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.logcheck.EisLogCheckUserMonitorItem;

import java.util.List;

public interface EisLogCheckUserMonitorItemMapper {

    Integer insert(EisLogCheckUserMonitorItem item);

    List<EisLogCheckUserMonitorItem> listOrderByCreateTimeTime(Long appId, String userEmail);

    EisLogCheckUserMonitorItem getByUserAndMonitorId(String userEmail, Long monitorId);

    void deleteAllByMonitorId(Long monitorId);

    void deleteById(long id);
}
