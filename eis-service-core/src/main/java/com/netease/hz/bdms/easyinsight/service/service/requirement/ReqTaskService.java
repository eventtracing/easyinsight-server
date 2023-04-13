package com.netease.hz.bdms.easyinsight.service.service.requirement;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import com.netease.hz.bdms.easyinsight.common.query.TaskPageQuery;
import com.netease.hz.bdms.easyinsight.dao.EisReqTaskMapper;
import com.netease.hz.bdms.easyinsight.dao.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class ReqTaskService {

    private static final FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");

    @Autowired
    EisReqTaskMapper reqTaskMapper;

    public List<EisReqTask> search(EisReqTask query){
        return Optional.ofNullable(reqTaskMapper.select(query)).orElse(new ArrayList<>());
    }

    public EisReqTask getById(Long id){
        return reqTaskMapper.selectByPrimaryKey(id);
    }

    public PageInfo queryPagingList(TaskPageQuery pageQuery){
        PageHelper.startPage(pageQuery.getCurrentPage(), pageQuery.getPageSize());
        List<EisReqTask> tasks = reqTaskMapper.queryPagingList(pageQuery);
        PageInfo pageInfo = new PageInfo(tasks);
        return pageInfo;
    }

    public List<EisReqTask> getByIds(Set<Long> ids){
        if(CollectionUtils.isNotEmpty(ids)){
            return reqTaskMapper.selectBatchByIds(ids);
        }
        return Lists.newArrayList();
    }

    /**
     * @param terminalId
     * @param startTime inclusive
     * @param endTime exclusive
     * @return
     */
    public List<EisReqTask> listAllByTerminalId(long terminalId, long startTime, long endTime){
        if (terminalId < 1L || endTime <= startTime) {
            return new ArrayList<>(0);
        }
        return reqTaskMapper.listAllByTerminalId(terminalId, fdf.format(startTime), fdf.format(endTime));
    }

    public List<EisReqTask> getByReqIds(Set<Long> reqIds){
        if(CollectionUtils.isNotEmpty(reqIds)){
            return reqTaskMapper.selectBatchByReqIds(reqIds);
        }
        return Lists.newArrayList();
    }

    public void updateById(EisReqTask updateEntity){
        if(updateEntity.getId() == null){
            throw new ServerException("更新任务失败，缺少主键");
        }
        // 公共信息填充
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        if(null != currUser){
            updateEntity.setUpdateName(currUser.getUserName());
            updateEntity.setUpdateEmail(currUser.getEmail());
        }
        reqTaskMapper.updateByPrimaryKeySelective(updateEntity);
    }

    public void insertBatch(List<EisReqTask> list){
        if(CollectionUtils.isEmpty(list)) {
            return;
        }
        // 公共信息填充
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        if(null != currUser){
            list.forEach(eisReqTask -> {
                eisReqTask.setCreateEmail(currUser.getEmail());
                eisReqTask.setCreateName(currUser.getUserName());
                eisReqTask.setUpdateEmail(currUser.getEmail());
                eisReqTask.setUpdateName(currUser.getUserName());
                eisReqTask.setCreateTime(new Date());
                eisReqTask.setUpdateTime(new Date());
            });
        }
        reqTaskMapper.insertBatch(list);
    }

    public void updateBatch(List<EisReqTask> list){
        if(CollectionUtils.isEmpty(list)) {
            return;
        }
        // 公共信息填充
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        if(null != currUser){
            list.forEach(eisReqTask -> {
                eisReqTask.setUpdateEmail(currUser.getEmail());
                eisReqTask.setUpdateName(currUser.getUserName());
            });
        }
        reqTaskMapper.updateBatch(list);
    }

    public void deleteById(Long id){
        reqTaskMapper.deleteByPrimaryKey(id);
    }

    public void deleteByIds(Set<Long> ids){
        if(CollectionUtils.isEmpty(ids))  {
            return;
        }
        reqTaskMapper.deleteByIds(ids);

    }

}

