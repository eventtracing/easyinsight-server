package com.netease.hz.bdms.easyinsight.service.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.dao.ParamMapper;
import com.netease.hz.bdms.easyinsight.dao.model.Param;
import com.netease.hz.bdms.easyinsight.service.service.ParamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ParamServiceImpl implements ParamService {

    private static final Map<String, String> orderByMap = ImmutableMap
            .of("createTime", "create_time", "updateTime", "update_time");
    private static final Map<String, String> orderRuleMap = ImmutableMap
            .of("descend", "desc", "ascend", "asc");
    @Autowired
  private ParamMapper paramMapper;

  private ParamSimpleDTO do2Dto(Param param) {
    ParamSimpleDTO paramSimpleDTO = BeanConvertUtils.convert(param, ParamSimpleDTO.class);
    if (null != paramSimpleDTO) {
      UserSimpleDTO updater = new UserSimpleDTO(param.getUpdateEmail(), param.getUpdateName());
      UserSimpleDTO creator = new UserSimpleDTO(param.getCreateEmail(), param.getCreateName());

      paramSimpleDTO.setCreator(creator)
          .setUpdater(updater);
    }
    return paramSimpleDTO;
  }

  private Param dto2Do(ParamSimpleDTO paramSimpleDTO) {
    Param param = BeanConvertUtils.convert(paramSimpleDTO, Param.class);
    if (param != null) {
      UserSimpleDTO updater = paramSimpleDTO.getUpdater();
      UserSimpleDTO creator = paramSimpleDTO.getCreator();

      if (creator != null) {
        param.setCreateEmail(creator.getEmail())
            .setCreateName(creator.getUserName());
      }
      if (updater != null) {
        param.setUpdateEmail(updater.getEmail())
            .setUpdateName(updater.getUserName());
      }
    }
    return param;
  }

  @Override
  public List<ParamSimpleDTO> getParamByCode(String code, Long appId) {
    Preconditions.checkArgument(null != appId, "??????ID????????????");
    Preconditions.checkArgument(StringUtils.isNotBlank(code), "?????????????????????");

    List<Param> params = paramMapper.selectByCode(code, appId);
    return params.stream().map(this::do2Dto).collect(Collectors.toList());
  }

  @Override
  public Integer getParamSizeByCode(String code, Long appId) {
    Preconditions.checkArgument(StringUtils.isNotBlank(code), "?????????????????????");
    Preconditions.checkArgument(null != appId, "??????ID????????????");

    return paramMapper.selectSizeByCode(code, appId);
  }

  @Override
  public ParamSimpleDTO getParamById(Long paramId) {
    Preconditions.checkArgument(null != paramId, "??????ID????????????");

    Param param = paramMapper.selectByPrimaryKey(paramId);
    return do2Dto(param);
  }

  @Override
  public List<ParamSimpleDTO> getByAppId(Long appId) {
    Preconditions.checkArgument(null != appId, "??????ID????????????");
    List<Param> params = paramMapper.selectByAppId(appId);
    return params.stream().map(this::do2Dto).collect(Collectors.toList());
  }

  @Override
  public Long createParam(ParamSimpleDTO paramSimpleDTO) {
    Param param = dto2Do(paramSimpleDTO);
    Preconditions.checkArgument(null != param, "????????????????????????");

    paramMapper.insert(param);
    return param.getId();  // ???useGeneratedKeys=true
  }

  @Override
  public Integer updateParam(ParamSimpleDTO paramSimpleDTO) {
    Param param = dto2Do(paramSimpleDTO);
    Preconditions.checkArgument(null != param, "????????????????????????");
    Preconditions.checkArgument(null != param.getId(), "??????ID????????????");

    return paramMapper.update(param);
  }

  @Override
  public Integer deleteParam(Long paramId) {
    Preconditions.checkArgument(null != paramId, "??????ID????????????");
    return paramMapper.delete(paramId);
  }

  /**
   * ?????????????????????????????????
   *
   * @param search    ?????????????????????????????????
   * @param paramTypes ??????????????????????????????
   * @param appId     ??????ID???????????????
   * @return ????????????
   */
  @Override
  public Integer searchParamSize(String search, List<Integer> paramTypes, List<String> createEmails, List<Integer> valueTypes, String code, Long appId, Collection<Long> ids) {
    return paramMapper.searchParamSize(search, paramTypes, createEmails, valueTypes, code, appId, ids);
  }

  @Override
  public List<Long> searchParamIdsByName(String search, List<Integer> paramTypes, List<String> createEmails, List<Integer> valueTypes, String code, Long appId, Collection<Long> ids) {
    return paramMapper.searchParamIdsByName(search, paramTypes, createEmails, valueTypes, code, appId, ids);
  }

  /**
   * ?????????????????????????????????
   *
   * @param search    ?????????????????????????????????
   * @param paramTypes ??????????????????????????????
   * @param appId     ??????ID???????????????
   * @param orderBy   ????????????
   * @param orderRule ???????????????asc,desc
   * @param offset    ???????????????
   * @param pageSize  ????????????
   * @return ????????????
   */
  @Override
  public List<ParamSimpleDTO> searchParam(String search, List<Integer> paramTypes, List<String> createEmails, List<Integer> valueTypes, String code, Long appId,
      Collection<Long> ids, String orderBy,
      String orderRule, Integer offset, Integer pageSize) {
    String dbOrderBy = orderByMap.get(orderBy);
    String dbOrderRule = orderRuleMap.get(orderRule);

    List<Param> params = paramMapper
        .searchParams(search, paramTypes, createEmails, valueTypes, code, appId, ids,  dbOrderBy, dbOrderRule, offset, pageSize);
    return params.stream().map(this::do2Dto).collect(Collectors.toList());
  }

  @Override
  public List<ParamSimpleDTO> listAllByAppIdAndCodes(List<String> codes, Long appId, Integer paramType) {
    List<Param> params = paramMapper.listByAppIdAndCodes(appId, paramType, codes);
    if (CollectionUtils.isEmpty(params)) {
      return new ArrayList<>(0);
    }
    return params.stream().map(this::do2Dto).collect(Collectors.toList());
  }

  @Override
  public List<ParamSimpleDTO> getParamByIds(Collection<Long> paramIds) {
    if (CollectionUtils.isNotEmpty(paramIds)) {
      List<Param> params = paramMapper.selectByIds(paramIds);
      return params.stream().map(this::do2Dto).collect(Collectors.toList());
    }
    return Lists.newArrayList();
  }

  @Override
  public List<UserSimpleDTO> getCreators(Long appId, Integer paramType) {
    Preconditions.checkArgument(null != appId, "??????ID????????????");
    Preconditions.checkArgument(null != paramType, "????????????????????????");

    return paramMapper.getCreators(appId, paramType);
  }

  @Override
  public  Integer updateCode(Long appId, Integer paramType, String oldCode, String newCode) {
    Preconditions.checkArgument(null != appId, "appId????????????");
    Preconditions.checkArgument(null != paramType, "????????????????????????");
    Preconditions.checkArgument(StringUtils.isNotBlank(oldCode) && StringUtils.isNotBlank(newCode), "???????????????code????????????");

    return paramMapper.updateCode(appId, paramType, oldCode, newCode);
  }
}
