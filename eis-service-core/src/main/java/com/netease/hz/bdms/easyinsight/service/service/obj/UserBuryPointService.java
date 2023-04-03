package com.netease.hz.bdms.easyinsight.service.service.obj;

import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import com.netease.hz.bdms.easyinsight.common.param.obj.ObjectUserParam;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.EisUserPointInfoMapper;
import com.netease.hz.bdms.easyinsight.dao.model.EisUserPointInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserBuryPointService {

    @Autowired
    EisUserPointInfoMapper eisUserPointInfoMapper;

    public EisUserPointInfo getById(Long id){
        return eisUserPointInfoMapper.getById(id);
    }

    public List<EisUserPointInfo> searchWithReqId(Long reqId){
        return Optional.ofNullable(eisUserPointInfoMapper.selectByReqId(reqId)).orElse(new ArrayList<>());
    }

    public void updateExtById(Long id, String extInfo){
        eisUserPointInfoMapper.updateExtInfo(id, extInfo);
    }

    public void delById(Long id){
        try {
            eisUserPointInfoMapper.delete(id);
        }catch (Exception e){
            log.error("param={}, 埋点删除失败", id, e);
            throw new ServerException("埋点已删除！");
        }
    }

    public void userPointEntry(ObjectUserParam objectUserParam){
        List<EisUserPointInfo> list = objectUserParam.getPointParams().stream().map(param -> BeanConvertUtils.convert(param, EisUserPointInfo.class)).filter(Objects::nonNull).collect(Collectors.toList());
        // 公共信息
        UserDTO currUser = EtContext.get(ContextConstant.USER);
        if(null != currUser) {
            list.forEach(userPointInfo -> {
                userPointInfo.setCreator(currUser.getUserName());
                userPointInfo.setCreateTime(new Date());
                userPointInfo.setUpdateTime(new Date());
            });
        }
        try {
            eisUserPointInfoMapper.insertBatch(list);
        }catch (Exception e){
            log.error("param={}, 埋点录入失败", objectUserParam, e);
            throw new ServerException("参数错误！");
        }
    }

}
