package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.spm.ArtificialSpmInfoDTO;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.ArtificialSpmInforMapper;
import com.netease.hz.bdms.easyinsight.dao.model.ArtificialSpmInfo;
import com.netease.hz.bdms.easyinsight.service.service.ArtificialSpmInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: xumengqiang
 * @date: 2021/11/9 14:25
 */

@Slf4j
@Service
public class ArtificialSpmInfoServiceImpl implements ArtificialSpmInfoService {
    @Autowired
    ArtificialSpmInforMapper spmMapInfoMapper;

    private ArtificialSpmInfoDTO do2Dto(ArtificialSpmInfo spmInfo) {
        ArtificialSpmInfoDTO spmInfoDTO = BeanConvertUtils
                .convert(spmInfo, ArtificialSpmInfoDTO.class);
        return spmInfoDTO;
    }

    private ArtificialSpmInfo dto2Do(ArtificialSpmInfoDTO spmInfoDTO) {
        ArtificialSpmInfo spmInfo = BeanConvertUtils
                .convert(spmInfoDTO, ArtificialSpmInfo.class);
        return spmInfo;
    }

    @Override
    public List<Long> create(Collection<ArtificialSpmInfoDTO> spmInfoDTOCollection) {
        if(CollectionUtils.isEmpty(spmInfoDTOCollection)){
            return Lists.newArrayList();
        }
        // 数据转化
        List<ArtificialSpmInfo> spmInfoList = spmInfoDTOCollection.stream()
                .map(this::dto2Do)
                .collect(Collectors.toList());
        // 公共信息填入
        UserDTO currUser= EtContext.get(ContextConstant.USER);
        if(null != currUser){
            spmInfoList.forEach(spmInfo -> {
                spmInfo.setCreateEmail(currUser.getEmail())
                        .setCreateName(currUser.getUserName())
                        .setUpdateEmail(currUser.getEmail())
                        .setUpdateName(currUser.getUserName());
            });
        }
        // 批量插入
        spmMapInfoMapper.insert(spmInfoList);
        List<Long> result = spmInfoList.stream()
                .map(ArtificialSpmInfo::getId)
                .collect(Collectors.toList());
        return result;

    }

    @Override
    public List<ArtificialSpmInfoDTO> getBySpm(Collection spmCollection, Long appId) {
        // 参数检查
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(spmCollection), "spm不能为空");
        Preconditions.checkArgument(null != appId, "产品信息不能为空");
        // 查询
        List<ArtificialSpmInfo> spmInfoList = spmMapInfoMapper.selectBySpm(spmCollection, appId);
        // 数据转化
        List<ArtificialSpmInfoDTO> result = spmInfoList.stream()
                .map(this::do2Dto)
                .collect(Collectors.toList());
        return result;
    }

//    @Override
//    public List<SpmInfoDTO> search(Long appId, SpmInfo query) {
//        // 参数检查
//        Preconditions.checkArgument(null != query, "查询条件不能为空");
//        Preconditions.checkArgument(null != appId, "appId不能为空");
//        query.setAppId(appId);  // 必传字段
//        List<SpmInfo> spmInfoList = spmMapInfoMapper.select(query);
//        if (CollectionUtils.isEmpty(spmInfoList)) {
//            return new ArrayList<>(0);
//        }
//        // 数据转化
//        return spmInfoList.stream()
//                .map(this::do2Dto)
//                .collect(Collectors.toList());
//    }
//
    @Override
    public List<ArtificialSpmInfoDTO> listAll() {
        List<ArtificialSpmInfo> spmInfoList = spmMapInfoMapper.listAll();
        if (CollectionUtils.isEmpty(spmInfoList)) {
            return new ArrayList<>(0);
        }
        // 数据转化
        return spmInfoList.stream()
                .map(this::do2Dto)
                .collect(Collectors.toList());
    }
//
//
//    @Override
//    public Integer deleteBySpm(Collection<String> spmCollection, Long appId) {
//        // 参数检查
//        Preconditions.checkArgument(null != appId, "产品信息不能为空");
//        // 删除
//        if(CollectionUtils.isNotEmpty(spmCollection)) {
//            Integer result = spmMapInfoMapper.deleteBySpm(spmCollection, appId);
//            return result;
//        }
//        return 0; // todo 返回 0 值还是 null 值？
//    }
//
    @Override
    public Integer deleteBySource(int source) {

        return spmMapInfoMapper.deleteBySource(source);

    }


    @Override
    public void update(List<ArtificialSpmInfoDTO> spmInfoDTOS) {

        List<ArtificialSpmInfo> spmInfoList = spmInfoDTOS.stream()
                .map(this::dto2Do)
                .collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(spmInfoList)) {
            spmMapInfoMapper.update(spmInfoList);
        }
    }

    @Override
    public void updateById(ArtificialSpmInfoDTO spmInfoDTO) {

        ArtificialSpmInfo spmInfo = dto2Do(spmInfoDTO);
        if(spmInfo != null) {
            spmMapInfoMapper.updateById(spmInfo);
        }
    }

}
