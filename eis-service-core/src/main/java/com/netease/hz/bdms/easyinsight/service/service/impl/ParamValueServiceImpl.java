package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamValueSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.ParamValueMapper;
import com.netease.hz.bdms.easyinsight.dao.model.ParamValue;
import com.netease.hz.bdms.easyinsight.service.service.ParamValueService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ParamValueServiceImpl implements ParamValueService {

    @Autowired
    private ParamValueMapper paramValueMapper;

    private ParamValueSimpleDTO do2Dto(ParamValue paramValue) {
        ParamValueSimpleDTO paramValueSimpleDTO = BeanConvertUtils
        .convert(paramValue, ParamValueSimpleDTO.class);
    if (paramValueSimpleDTO != null) {
      UserSimpleDTO updater = new UserSimpleDTO(paramValue.getUpdateEmail(),
          paramValue.getUpdateName());
      UserSimpleDTO creator = new UserSimpleDTO(paramValue.getCreateEmail(),
          paramValue.getCreateName());

      paramValueSimpleDTO.setCreator(creator)
          .setUpdater(updater);
    }
    return paramValueSimpleDTO;
  }

  private ParamValue dto2Do(ParamValueSimpleDTO paramValueSimpleDTO) {
    ParamValue paramValue = BeanConvertUtils.convert(paramValueSimpleDTO, ParamValue.class);
    if (paramValue != null) {
      UserSimpleDTO updater = paramValueSimpleDTO.getUpdater();
      UserSimpleDTO creator = paramValueSimpleDTO.getCreator();

      if (creator != null) {
        paramValue.setCreateEmail(creator.getEmail())
            .setCreateName(creator.getUserName());
      }
      if (updater != null) {
        paramValue.setUpdateEmail(updater.getEmail())
            .setUpdateName(updater.getUserName());
      }
    }
    return paramValue;
  }

  /**
   * 查询指定参数的参数值
   *
   * @param paramId 参数ID，必填参数
   * @param search  搜索关键字，非必填参数
   * @return 参数值列表
   */
  @Override
  public List<ParamValueSimpleDTO> getById(Long paramId, String search) {
    Preconditions.checkArgument(null != paramId, "参数ID不能为空");

    List<ParamValue> paramValues = paramValueMapper.selectParamValues(paramId, search);
    return paramValues.stream().map(this::do2Dto).collect(Collectors.toList());
  }

  @Override
  public List<ParamValueSimpleDTO> getByIds(Set<Long> paramValueIds) {
    List<ParamValue> paramValues =  paramValueMapper.selectBatchByIds(paramValueIds);
    return paramValues.stream().map(this::do2Dto).collect(Collectors.toList());
  }

  @Override
  public Integer getSizeById(Long paramId) {
    Preconditions.checkArgument(null != paramId, "参数ID不能为空");

    return paramValueMapper.selectParamValueSizeByParamId(paramId);
  }


  @Override
  public Integer deleteValue(Collection<Long> paramValueIds) {
    if (CollectionUtils.isNotEmpty(paramValueIds)) {
      return paramValueMapper.deleteByIds(paramValueIds);
    }

    return 0;
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  @Override
  public void updateValue(List<ParamValueSimpleDTO> updateValues) {
    if (CollectionUtils.isNotEmpty(updateValues)) {
      List<ParamValue> paramValues = updateValues.stream().map(this::dto2Do)
          .collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(paramValues)) {
        for (ParamValue paramValue : paramValues) {
          paramValueMapper.update(paramValue);
        }
      }
    }
  }

  @Override
  public void addValue(List<ParamValueSimpleDTO> addValues) {
    if (CollectionUtils.isNotEmpty(addValues)) {
      List<ParamValue> paramValues = addValues.stream().map(this::dto2Do)
          .collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(paramValues)) {
        for (ParamValue paramValue : paramValues) {
          paramValueMapper.insert(paramValue);
        }
      }
    }
  }

  @Override
  public List<ParamValueSimpleDTO> getByParamIds(Set<Long> paramIds) {
    if (CollectionUtils.isNotEmpty(paramIds)) {
      List<ParamValue> paramValues = paramValueMapper.selectByParamIds(paramIds);
      return paramValues.stream().map(this::do2Dto).collect(Collectors.toList());
    }
    return Lists.newArrayList();
  }

  @Override
  public Integer deleteByParamId(Long paramId) {
    Preconditions.checkArgument(null != paramId, "参数ID不能为空");

    return paramValueMapper.deleteByParamId(paramId);
  }


}
