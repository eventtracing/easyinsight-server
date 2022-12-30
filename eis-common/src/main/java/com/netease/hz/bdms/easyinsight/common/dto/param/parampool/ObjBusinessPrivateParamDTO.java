package com.netease.hz.bdms.easyinsight.common.dto.param.parampool;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.param.ParamValueSimpleDTO;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ObjBusinessPrivateParamDTO {
  /**
   * 参数ID
   */
  private Long id;

  /**
   * 参数名
   */
  private String code;

  /**
   * 参数中文名称
   */
  private String name;

  /**
   * 参数类型
   */
  private Integer paramType;

  /**
   * 参数值类型
   * @see com.netease.hz.bdms.easyinsight.common.enums.ParamValueTypeEnum
   */
  private Integer valueType;


  /**
   * 产品ID
   */
  private Long appId;

  /**
   * 描述
   */
  private Integer description;

  /**
   * 创建人
   */
  private UserSimpleDTO creator;

  /**
   * 最近更新人
   */
  private UserSimpleDTO updater;

  /**
   * 创建时间
   */
  private Long createTime;

  /**
   * 最近更新时间
   */
  private Long updateTime;

  /**
   * 取值
   */
  private List<ParamValueSimpleDTO> values;
//  /**
//   * 绑定的对象
//   */
//  private List<ObjBasicSimpleDTO> binds;
}
