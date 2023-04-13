package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.dto.spm.SpmMapInfoDTO;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.SpmMapInfoMapper;
import com.netease.hz.bdms.easyinsight.dao.model.SpmMapInfo;
import com.netease.hz.bdms.easyinsight.service.service.SpmMapInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class SpmMapInfoServiceImpl implements SpmMapInfoService {
    @Autowired
    SpmMapInfoMapper spmMapMapper;

    private SpmMapInfoDTO do2Dto(SpmMapInfo spmMapInfo) {
        SpmMapInfoDTO spmMapInfoDTO = BeanConvertUtils
                .convert(spmMapInfo, SpmMapInfoDTO.class);
        return spmMapInfoDTO;
    }

    private SpmMapInfo dto2Do(SpmMapInfoDTO spmMapInfoDTO) {
        SpmMapInfo spmMapInfo = BeanConvertUtils.convert(spmMapInfoDTO, SpmMapInfo.class);
        if(spmMapInfo != null && spmMapInfo.getCreateTime() == null){
            spmMapInfo.setCreateTime(new Date());
        }
        if(spmMapInfo != null && spmMapInfo.getUpdateTime() == null){
            spmMapInfo.setUpdateTime(new Date());
        }
        return spmMapInfo;
    }

    @Override
    public List<Long> create(Collection<SpmMapInfoDTO> spmMapInfoDTOList) {

        // 数据转化
        List<SpmMapInfo> spmMapInfoList = spmMapInfoDTOList.stream()
                .map(this::dto2Do)
                .collect(Collectors.toList());
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(spmMapInfoList),
                "spmMapRelationInfoList不能为空");
        // 批量写入数据库, 未防止批量插入数据超出限制, 对集合进行拆分
        List<List<SpmMapInfo>> subLists = Lists.partition(spmMapInfoList, 1000);
        for(List<SpmMapInfo> subSpmMapInfoList : subLists){
            spmMapMapper.insertBatch(subSpmMapInfoList);
        }
        // 返回插入数据的自增Ids
        List<Long> idList = spmMapInfoList.stream()
                .map(SpmMapInfo::getId)
                .collect(Collectors.toList());
        return idList;
    }

    @Override
    public SpmMapInfoDTO getByPrimaryKey(Long spmInfoId) {
        // 数据校验
        Preconditions.checkArgument(null != spmInfoId, "spmMapRelationInfoId不能为空");
        // 读取数据
        SpmMapInfo spmMapInfo = spmMapMapper.selectByPrimaryKey(spmInfoId);
        // 数据转化
        return do2Dto(spmMapInfo);
    }

    @Override
    public Integer delete(SpmMapInfoDTO spmMapInfoDTO) {
        // 参数检查
        Preconditions.checkArgument(null != spmMapInfoDTO);
        // 数据转化
        SpmMapInfo spmMapInfo = dto2Do(spmMapInfoDTO);
        // 依据给定的条件删除
        Integer result = spmMapMapper.delete(spmMapInfo);
        return result;
    }

}
