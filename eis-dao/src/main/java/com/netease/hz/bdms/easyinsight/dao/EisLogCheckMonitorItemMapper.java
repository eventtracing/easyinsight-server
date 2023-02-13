package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.logcheck.EisLogCheckMonitorItem;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface EisLogCheckMonitorItemMapper {

    Integer insert(EisLogCheckMonitorItem item);

    EisLogCheckMonitorItem getById(long id);

    void updateItem(EisLogCheckMonitorItem item);

    List<EisLogCheckMonitorItem> batchGetById(@Param("ids") Collection<Long> ids);

    List<EisLogCheckMonitorItem> searchByName(Long appId, String search);

    List<EisLogCheckMonitorItem> listAll(Long appId);

    void deleteById(long id);
}
