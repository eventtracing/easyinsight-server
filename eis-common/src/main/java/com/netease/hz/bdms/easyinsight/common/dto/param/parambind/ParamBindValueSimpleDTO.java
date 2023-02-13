package com.netease.hz.bdms.easyinsight.common.dto.param.parambind;

import com.netease.hz.bdms.easyinsight.common.dto.common.UserSimpleDTO;
import java.sql.Timestamp;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ParamBindValueSimpleDTO {

  /**
   * 自增id
   */
  private Long id;
  /**
   * 参数绑定ID
   */
  private Long bindId;
  /**
   * 参数值ID
   */
  private Long paramValueId;
  /**
   * 产品ID
   */
  private Long appId;
  /**
   * 描述
   */
  private String description;
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
  private Timestamp createTime;
  /**
   * 更新时间
   */
  private Timestamp updateTime;
}
