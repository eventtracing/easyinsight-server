package com.netease.hz.bdms.easyinsight.common.enums;

import com.google.common.collect.Lists;
import com.netease.hz.bdms.easyinsight.common.exception.ServerException;
import com.netease.hz.bdms.easyinsight.common.util.CollectionUtil;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;

@AllArgsConstructor
@Getter
public enum EntityTypeEnum {
  /**
   * 终端
   */
  TERMINAL(1, Lists.newArrayList(ParamTypeEnum.GLOBAL_PUBLIC_PARAM.getType())),
  /**
   * 事件类型
   */
  EVENT(2, Lists.newArrayList(ParamTypeEnum.EVENT_PUBLIC_PARAM.getType())),
  /**
   * 对象埋点
   */
  OBJTRACKER(3, Lists.newArrayList(ParamTypeEnum.OBJ_BUSINESS_PRIVATE_PARAM.getType(),
      ParamTypeEnum.OBJ_NORMAL_PARAM.getType())),
  /**
   * 模板
   */
  TEMPLATE(4, Lists.newArrayList(
      ParamTypeEnum.OBJ_BUSINESS_PRIVATE_PARAM.getType(),
      ParamTypeEnum.OBJ_NORMAL_PARAM.getType())),
  /**
   * 对象
   */
  OBJHISTORY(5, Lists.newArrayList()),
  ;
  private final Integer type;
  /**
   * 允许参数的类型
   * @see ParamTypeEnum
   */
  private final List<Integer> allowParamTypes;

  public static EntityTypeEnum fromType(Integer type) {
    for (EntityTypeEnum entityTypeEnum : values()) {
      if (entityTypeEnum.getType().equals(type)) {
        return entityTypeEnum;
      }
    }
    throw new ServerException(type + "不能转换为EntityTypeEnum");
  }

  /**
   * 检查当前的参数类型对于当前Entity是否是合法的
   *
   * @param paramTypes 参数类型集合
   * @return 判断结果：true表示合法，false表示非法
   */
  public Boolean checkLegalParamType(Collection<Integer> paramTypes) {
    if (CollectionUtils.isEmpty(paramTypes)) {
      return true;
    }
    Collection<Integer> restParamTypes = CollectionUtil.getDifference(paramTypes, allowParamTypes);
    return CollectionUtils.isEmpty(restParamTypes);
  }
}
