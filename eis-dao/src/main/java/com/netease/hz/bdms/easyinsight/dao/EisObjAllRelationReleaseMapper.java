package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisObjAllRelationRelease;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface EisObjAllRelationReleaseMapper extends Mapper<EisObjAllRelationRelease> {

    void insertBatch(List<EisObjAllRelationRelease> list);

}