package com.netease.hz.bdms.easyinsight.dao;

import com.netease.hz.bdms.easyinsight.dao.model.EisTaskProcess;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Set;

public interface EisTaskProcessMapper extends Mapper<EisTaskProcess> {

    List<EisTaskProcess> selectBatchByIds(@Param("ids") Set<Long> ids);

    List<EisTaskProcess> selectBatchByTaskIds(@Param("taskIds") Set<Long> taskIds);

    List<EisTaskProcess> selectBatchBySpmBjObjIds(@Param("spmByObjIds") Set<String> spmByObjIds, @Param("status") Integer status);

    void insertBatch(@Param("list") List<EisTaskProcess> list);

    void updateBatch(@Param("list") List<EisTaskProcess> list);

    void updateVerifier(@Param("ids") List<Long> ids, @Param("verifierName") String verifierName, @Param("verifierEmail") String verifierEmail,
                        @Param("updateName") String updateName, @Param("updateEmail") String updateEmail);

    void updateOwner(@Param("ids") List<Long> ids, @Param("ownerName") String ownerName, @Param("ownerEmail") String ownerEmail,
                     @Param("updateName") String updateName, @Param("updateEmail") String updateEmail);

    void deleteByIds(@Param("ids") Set<Long> ids);

    void deleteByInfos(@Param("reqPoolId") Long reqPoolId, @Param("taskId") Long taskId, @Param("spmByObjId") String spmByObjId);

    List<EisTaskProcess> getByReqPoolEntityIds(@Param("reqPoolType") Integer reqPoolType, @Param("reqPoolEntityIds") Set<Long> reqPoolEntityIds);

}