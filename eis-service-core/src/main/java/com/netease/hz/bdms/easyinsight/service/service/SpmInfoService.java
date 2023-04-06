package com.netease.hz.bdms.easyinsight.service.service;

import com.netease.hz.bdms.easyinsight.common.dto.spm.SpmInfoDTO;
import com.netease.hz.bdms.easyinsight.dao.model.SpmInfo;

import java.util.Collection;
import java.util.List;

/**
 * @author: xumengqiang
 * @date: 2021/11/9 14:22
 */
public interface SpmInfoService {
    // 批量插入
    List<Long> create(Collection<SpmInfoDTO> spmInfoDTOCollection);

    // 查询
    List<SpmInfoDTO> getBySpm(Collection<String> spmCollection, Long appId);

    // 搜索
    List<SpmInfoDTO> search(Long appId, SpmInfo query);


    // 搜索最近的
    List<SpmInfoDTO> searchLast(Long appId, SpmInfo query);

    List<SpmInfoDTO> selectByNameOrCode(Long appId, SpmInfo query,boolean isSerarch);

    // 列出全部
    List<SpmInfoDTO> listAll();

    // 删除
    Integer deleteBySpm(Collection<String> spmList, Long appId);

    //
    Integer deleteByIds(Collection<Long> ids);

    Integer deleteBySource(int source);

    // 更新
    void update(List<SpmInfoDTO> spmInfoDTOS);

    // 更新单个记录
    void updateById(SpmInfoDTO spmInfoDTOS);

}
