package com.netease.hz.bdms.easyinsight.common.param.event;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class EventUpdateParam {
  /**
   * 终端主键ID
   */
  @NotNull(message = "事件类型主键ID不能为空")
  private Long id;

  /**
   * 事件类型ID
   */
  @NotBlank(message = "事件类型ID不能为空")
  private String code;
  /**
   * 终端名称
   */
  @NotBlank(message = "事件类型名称不能为空")
  private String name;

  /**
   * 描述
   */
  private String description;

  /**
   * 适用的对象类型， 格式为[1,2]
   * 其中的objType
   * @see com.netease.hz.bdms.easyinsight.common.enums.ObjTypeEnum
   */
  @NotEmpty(message = "适用的对象类型不能为空")
  private List<Integer> applicableObjTypes;
  /**
   * 是否默认选中, 作用于对象新建
   */
  private Boolean selectedByDefault;
}
