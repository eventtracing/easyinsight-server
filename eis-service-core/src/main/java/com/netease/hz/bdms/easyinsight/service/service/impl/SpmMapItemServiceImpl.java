package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.spm.SpmMapItemDTO;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.SpmMapItemMapper;
import com.netease.hz.bdms.easyinsight.dao.model.SpmMapItem;
import com.netease.hz.bdms.easyinsight.service.service.SpmMapItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: xumengqiang
 * @date: 2021/11/10 19:14
 */

@Slf4j
@Service
public class SpmMapItemServiceImpl implements SpmMapItemService {
    @Autowired
    SpmMapItemMapper spmMapItemMapper;

    private SpmMapItemDTO do2Dto(SpmMapItem spmMapItem){
        SpmMapItemDTO spmMapItemDTO = BeanConvertUtils.convert(spmMapItem, SpmMapItemDTO.class);
        return  spmMapItemDTO;
    }

    private SpmMapItem dto2Do(SpmMapItemDTO spmMapItemDTO){
        SpmMapItem spmMapItem = BeanConvertUtils.convert(spmMapItemDTO, SpmMapItem.class);
        return spmMapItem;
    }

    @Override
    public List<Long> create(Collection<SpmMapItemDTO> spmMapItemDTOCollection) {
        if(CollectionUtils.isEmpty(spmMapItemDTOCollection)){
            return Lists.newArrayList();
        }
        // 数据转化
        List<SpmMapItem> spmMapItemList = spmMapItemDTOCollection.stream()
                .map(this::dto2Do)
                .collect(Collectors.toList());
        // 公共信息填充
        UserDTO currUser= EtContext.get(ContextConstant.USER);
        if(null != currUser){
            spmMapItemList.forEach(spmMapItem -> {
                spmMapItem.setCreateEmail(currUser.getEmail())
                        .setCreateName(currUser.getUserName())
                        .setUpdateEmail(currUser.getEmail())
                        .setUpdateName(currUser.getUserName())
                        .setCreateTime(new Date())
                        .setUpdateTime(new Date());
            });
        }
        // 批量插入
        spmMapItemMapper.insert(spmMapItemList);
        List<Long> res = spmMapItemList.stream()
                .map(SpmMapItem::getId)
                .collect(Collectors.toList());
        return  res;
    }

    @Override
    public Integer deleteBySpmId(Collection<Long> spmIds) {
        // 参数检查
        if(CollectionUtils.isNotEmpty(spmIds)){
            // 删除
            return spmMapItemMapper.deleteBySpmId(spmIds);
        }
        return 0;
    }

    @Override
    public List<SpmMapItemDTO> getBySpmIds(Collection<Long> spmIds) {
        // 参数检查
        if(CollectionUtils.isNotEmpty(spmIds)) {
            // 查询
            List<SpmMapItem> spmMapItemList = spmMapItemMapper.selectBySpmIds(spmIds);
            List<SpmMapItemDTO> result = spmMapItemList.stream()
                    .map(this::do2Dto)
                    .collect(Collectors.toList());
            return result;
        }
        return Lists.newArrayList();
    }
}
