package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.parampool.ParamPoolItemDTO;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.ParamPoolMapper;
import com.netease.hz.bdms.easyinsight.dao.model.ParamPoolItem;
import com.netease.hz.bdms.easyinsight.service.service.ParamPoolService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ParamPoolServiceImpl implements ParamPoolService {
    @Autowired
    private ParamPoolMapper paramPoolMapper;

    private static final Map<String, String> orderByMap = ImmutableMap
            .of("createTime", "create_time", "updateTime", "update_time");
    private static final Map<String, String> orderRuleMap = ImmutableMap
      .of("descend", "desc", "ascend", "asc");

  private ParamPoolItemDTO do2Dto(ParamPoolItem paramPoolItem) {
    ParamPoolItemDTO paramPoolItemDTO = BeanConvertUtils.convert(paramPoolItem, ParamPoolItemDTO.class);
    if(null != paramPoolItemDTO) {
      UserSimpleDTO updater = new UserSimpleDTO(paramPoolItem.getUpdateEmail(), paramPoolItem.getUpdateName());
      UserSimpleDTO creator = new UserSimpleDTO(paramPoolItem.getCreateEmail(), paramPoolItem.getCreateName());

      paramPoolItemDTO.setCreator(creator)
          .setUpdater(updater);
    }
    return paramPoolItemDTO;
  }

  private ParamPoolItem dto2Do(ParamPoolItemDTO paramPoolItemDTO) {
    ParamPoolItem paramPoolItem = BeanConvertUtils.convert(paramPoolItemDTO, ParamPoolItem.class);
    if (paramPoolItem != null) {
      UserSimpleDTO updater = paramPoolItemDTO.getUpdater();
      UserSimpleDTO creator = paramPoolItemDTO.getCreator();

      if (creator != null) {
        paramPoolItem.setCreateEmail(creator.getEmail())
            .setCreateName(creator.getUserName());
      }
      if (updater != null) {
        paramPoolItem.setUpdateEmail(updater.getEmail())
            .setUpdateName(updater.getUserName());
      }
    }
    return paramPoolItem;
  }

  @Override
  public List<ParamPoolItemDTO> getParamPoolItemByCode(String code, Long appId) {
    Preconditions.checkArgument(null != appId, "产品ID不能为空");
    Preconditions.checkArgument(StringUtils.isNotBlank(code), "参数名不能为空");

    List<ParamPoolItem> params  = paramPoolMapper.selectByCode(code, appId);
    return params.stream().map(this::do2Dto).collect(Collectors.toList());
  }

  @Override
  public Integer getParamPoolItemSizeByCode(String code, Long appId) {
    Preconditions.checkArgument(null != appId, "产品ID不能为空");
    Preconditions.checkArgument(StringUtils.isNotBlank(code), "参数名不能为空");
    return paramPoolMapper.selectSizeByCode(code, appId);
  }

  @Override
  public ParamPoolItemDTO getParamPoolItemById(Long paramId) {
    Preconditions.checkArgument(null != paramId, "参数ID不能为空");

    ParamPoolItem paramPoolItem  = paramPoolMapper.selectByPrimaryKey(paramId);
    return do2Dto(paramPoolItem);
  }

  @Override
  public Long createParamPoolItem(ParamPoolItemDTO paramPoolItemDTO) {
    ParamPoolItem paramPoolItem = dto2Do(paramPoolItemDTO);
    if (paramPoolItem == null) {
      throw new CommonException("参数对象不能为空");
    }
    paramPoolMapper.insert(paramPoolItem);
    return paramPoolItem.getId();
  }

  @Override
  public Integer updateParamPoolItem(ParamPoolItemDTO paramPoolItemDTO) {
    ParamPoolItem paramPoolItem = dto2Do(paramPoolItemDTO);
    Preconditions.checkArgument(null != paramPoolItem, "参数对象不能为空");

    return paramPoolMapper.update(paramPoolItem);
  }

  @Override
  public Integer deleteParamPoolItem(Long paramPoolItemId) {
    Preconditions.checkArgument(null != paramPoolItemId, "参数ID不能为空");
    return paramPoolMapper.delete(paramPoolItemId);
  }

  @Override
  public Integer searchParamPoolItemSize(String search, Long appId) {
    return paramPoolMapper.searchParamPoolItemSize(search, appId);
  }

  @Override
  public List<ParamPoolItemDTO> searchParamPoolItem(String search, Long appId, String orderBy,
      String orderRule, Integer offset, Integer pageSize) {
    String dbOrderBy = orderByMap.get(orderBy);
    String dbOrderRule = orderRuleMap.get(orderRule);

    List<ParamPoolItem> params = paramPoolMapper.searchParamPoolItems(search, appId, dbOrderBy, dbOrderRule, offset, pageSize);
    return params.stream().map(this::do2Dto).collect(Collectors.toList());
  }
}
