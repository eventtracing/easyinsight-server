package com.netease.hz.bdms.easyinsight.common.dto.obj.param;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;

@Data
@Accessors(chain = true)
public class ParamWithValueItemDTO {

  /**
   * 参数ID
   */
  public Long id;
  /**
   * 参数key
   */
  private String code;
  /**
   * 参数名称
   */
  private String name;

  /**
   * 参数取值描述
   */
  private String description;
  /**
   * 参数值
   */
  private List<String> values;
  /**
   * 参数类型
   * @see com.netease.hz.bdms.easyinsight.common.enums.ParamTypeEnum
   */
  private Integer paramType;
  /**
   * 参数值类型
   * @see com.netease.hz.bdms.easyinsight.common.enums.ParamValueTypeEnum
   */
  private Integer valueType;
  /**
   * 绑定实体ID
   */
  private Long entityId;
  /**
   * 绑定实体类型
   */
  private Integer entityType;
  /**
   * 版本Id
   */
  private Long versionId;
  /**
   * 参数变更类型
   * @see com.netease.hz.bdms.easyinsight.common.enums.ChangeTypeEnum
   */
  private Integer paramChangeType;

  /**
   * 是否必须传
   */
  private Boolean must = true;
  /**
   * 是否非空
   */
  private Boolean notEmpty;
  /**
   * 是否需要测试
   */
  private Boolean needTest;

  /**
   * 参数对应的上报日志是否使用urlEncode编码
   */
  private Boolean isEncode;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ParamWithValueItemDTO that = (ParamWithValueItemDTO) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(values, that.values) &&
        Objects.equals(paramType, that.paramType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, values, paramType);
  }
}
