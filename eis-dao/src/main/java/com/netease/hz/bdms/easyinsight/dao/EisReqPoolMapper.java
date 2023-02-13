package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.common.query.ReqPoolPageQuery;
import com.netease.hz.bdms.easyinsight.dao.model.EisReqPool;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.Collection;
import java.util.List;

public interface EisReqPoolMapper extends Mapper<EisReqPool> {

    List<EisReqPool> queryForPage(ReqPoolPageQuery query);

    List<EisReqPool> selectByIds(@Param("ids") Collection<Long> ids);

}