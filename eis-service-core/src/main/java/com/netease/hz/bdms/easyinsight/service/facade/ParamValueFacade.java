package com.netease.hz.bdms.easyinsight.service.facade;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.netease.hz.bdms.easyinsight.common.constant.ContextConstant;
import com.netease.hz.bdms.easyinsight.common.context.EtContext;
import com.netease.hz.bdms.easyinsight.common.dto.app.AppSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamValueSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import com.netease.hz.bdms.easyinsight.common.param.param.paramvalue.ParamValueItemParam;
import com.netease.hz.bdms.easyinsight.common.param.param.paramvalue.ParamValueUpdateParam;
import com.netease.hz.bdms.easyinsight.common.util.BeanConvertUtils;
import com.netease.hz.bdms.easyinsight.common.util.CollectionUtil;
import com.netease.hz.bdms.easyinsight.service.service.AppService;
import com.netease.hz.bdms.easyinsight.service.service.ParamService;
import com.netease.hz.bdms.easyinsight.service.service.ParamValueService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ParamValueFacade {
  @Autowired
  private ParamValueService paramValueService;
  @Autowired
  private ParamService paramService;
  @Autowired
  private AppService appService;

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public void editParamValue(ParamValueUpdateParam param) {
    // 检查输入参数
    Preconditions.checkArgument(null != param, "输入参数不能为空");
    Long paramId = param.getParamId();
    Preconditions
        .checkArgument(null != paramId, "输入参数不能为空");

    // 检查appId
    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "未指定产品信息");
    AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
    Preconditions.checkArgument(null != appSimpleDTO, "该产品不存在");


    // 检查是否参数值是否重复

    Set<String> codes = Sets.newHashSet();
    if(CollectionUtils.isNotEmpty(param.getValues())) {
      for (ParamValueItemParam itemParam : param.getValues()) {
        if(codes.contains(itemParam.getCode())) {
          throw new CommonException(itemParam.getCode()+"重复");
        }else {
          codes.add(itemParam.getCode());
        }
      }
    }


    // 检查参数是否存在
    ParamSimpleDTO paramSimpleDTO = paramService.getParamById(paramId);
    Preconditions.checkArgument(null != paramSimpleDTO, "该参数不存在");
    List<ParamValueSimpleDTO> paramValues = paramValueService.getById(paramId, null);
    Map<Long, ParamValueSimpleDTO> paramValuesMap = paramValues.stream()
        .collect(Collectors.toMap(ParamValueSimpleDTO::getId, Function.identity(), (k1, k2) -> k1));


    // 对输入参数进行分类：增删改三种类型
    List<ParamValueSimpleDTO> toAddValues = Lists.newArrayList();
    List<ParamValueSimpleDTO> toModifyValues = Lists.newArrayList();

    UserDTO currentUserDTO = EtContext.get(ContextConstant.USER);
    UserSimpleDTO currentUser = BeanConvertUtils.convert(currentUserDTO, UserSimpleDTO.class);

    // 删除场景
    List<Long> paramValuesIds = param.getValues().stream()
        .filter(itemParam -> null != itemParam.getId()).map(ParamValueItemParam::getId)
        .collect(Collectors.toList());
    Collection<Long> toDeleteIds = CollectionUtil
        .getDifference(paramValuesMap.keySet(), paramValuesIds);

    // 修改/新增两种场景
    if(CollectionUtils.isNotEmpty(param.getValues())) {
      for (ParamValueItemParam itemParam : param.getValues()) {
        Long paramValueId = itemParam.getId();

        ParamValueSimpleDTO paramValueSimpleDTO = BeanConvertUtils
            .convert(itemParam, ParamValueSimpleDTO.class);
        paramValueSimpleDTO.setAppId(appId)
            .setParamId(paramId)
            .setUpdater(currentUser);

        if (paramValueId != null) {
          // 修改
          if (paramValuesMap.containsKey(paramValueId)) {
            toModifyValues.add(paramValueSimpleDTO);
          } else {
            throw new CommonException("id=" + paramValueId + "的参数值不存在，修改失败");
          }
        } else {
          // 增加
          paramValueSimpleDTO.setCreator(currentUser);
          toAddValues.add(paramValueSimpleDTO);
        }
      }
    }
    // 处理
    paramValueService.deleteValue(toDeleteIds);
    paramValueService.updateValue(toModifyValues);
    paramValueService.addValue(toAddValues);
  }


  public List<ParamValueSimpleDTO> listParamValue(Long paramId, String search) {
    Preconditions.checkArgument(null !=  paramId, "参数ID不能为空");
    Long appId = EtContext.get(ContextConstant.APP_ID);
    Preconditions.checkArgument(null != appId, "未指定产品信息");
    AppSimpleDTO appSimpleDTO = appService.getAppById(appId);
    Preconditions.checkArgument(null != appSimpleDTO, "该产品不存在");

    return paramValueService.getById(paramId, search);
  }
}
